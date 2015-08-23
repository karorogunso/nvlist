package nl.weeaboo.vn.script.lua;

import static org.luaj.vm2.LuaValue.valueOf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.ResourceFinder.Resource;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filesystem.FileSystemView;
import nl.weeaboo.io.CustomSerializable;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.NovelPrefs;
import nl.weeaboo.vn.core.impl.ResourceLoader;
import nl.weeaboo.vn.script.IScriptLoader;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.lvn.ICompiledLvnFile;
import nl.weeaboo.vn.script.lvn.ILvnParser;
import nl.weeaboo.vn.script.lvn.LvnParseException;
import nl.weeaboo.vn.script.lvn.LvnParserFactory;

@CustomSerializable
public class LuaScriptLoader implements IScriptLoader {

    private static final long serialVersionUID = LuaImpl.serialVersionUID;

    private static final int INPUT_BUFFER_SIZE = 4096;
    private static final LuaString PATH = valueOf("path");

    private final LuaScriptResourceLoader resourceLoader;
    private final String engineTargetVersion;

    private transient ILvnParser lvnParser;

    private LuaScriptLoader(LuaScriptResourceLoader resourceLoader, String engineTargetVersion) {
        this.resourceLoader = Checks.checkNotNull(resourceLoader);
        this.engineTargetVersion = Checks.checkNotNull(engineTargetVersion);

        initTransients();
    }

    public static LuaScriptLoader newInstance(IEnvironment env) {
        LuaScriptLoader scriptLoader = new LuaScriptLoader(new LuaScriptResourceLoader(env),
                env.getPref(NovelPrefs.ENGINE_TARGET_VERSION));
        scriptLoader.initEnv();
        return scriptLoader;
    }

    private void initTransients() {
        lvnParser = LvnParserFactory.getParser(engineTargetVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        initTransients();
    }

    void initEnv() {
        PackageLib.getCurrent().setLuaPath("?.lvn;?.lua");

        BaseLib.FINDER = new ResourceFinder() {
            @Override
            public Resource findResource(String filename) {
                try {
                    return luaOpenScript(filename);
                } catch (LvnParseException e) {
                    throw new LuaError(e);
                } catch (FileNotFoundException e) {
                    return null;
                } catch (IOException e) {
                    throw new LuaError(e);
                }
            }
        };
    }

    @Override
    public String findScriptFile(String name) {
        PackageLib packageLib = PackageLib.getCurrent();
        LuaTable packageTable = packageLib.PACKAGE;
        String path = packageTable.get(PATH).tojstring();

        if (getScriptExists(name)) {
            return name;
        }

        for (String pattern : path.split(";")) {
            String filename = pattern.replaceFirst("\\?", name);
            if (getScriptExists(filename)) {
                return filename;
            }
        }
        return name;
    }

    protected boolean getScriptExists(String filename) {
        return resourceLoader.isValidFilename(filename);
    }

    public boolean isBuiltInScript(String filename) {
        return LuaScriptResourceLoader.isBuiltInScript(filename);
    }

    @Override
    public InputStream openScript(String filename) throws IOException {
        return resourceLoader.newInputStream(filename);
    }

    protected long getScriptModificationTime(String filename) throws IOException {
        return resourceLoader.getModifiedTime(filename);
    }

    @Override
    public Collection<String> getScriptFiles(String folder) {
        return resourceLoader.getMediaFiles(folder);
    }

    public ICompiledLvnFile compileScript(String normalizedFilename, InputStream in)
            throws LvnParseException, IOException {

        ICompiledLvnFile file = lvnParser.parseFile(normalizedFilename, in);

//TODO Re-enable analytics
//        IAnalytics analytics = getAnalytics();
//        if (analytics != null) {
//            long modTime = getScriptModificationTime(normalizedFilename);
//            analytics.logScriptCompile(filename, modificationTime);
//        }

//TODO Re-enable seen logging
//        ISeenLog seenLog = getSeenLog();
//        if (seenLog != null) {
//            seenLog.registerScriptFile(filename, file.countTextLines(false));
//        }

        return file;
    }

    Resource luaOpenScript(String filename) throws LvnParseException, IOException {
        filename = findScriptFile(filename);

        InputStream in = openScript(filename);

        if (!LuaScriptUtil.isLvnFile(filename)) {
            //Read as a normal file
            if (INPUT_BUFFER_SIZE > 0) {
                in = new BufferedInputStream(in, INPUT_BUFFER_SIZE);
            }
        } else {
            ICompiledLvnFile file;
            try {
                file = compileScript(filename, in);
            } finally {
                in.close();
            }
            String contents = file.getCompiledContents();
            in = new ByteArrayInputStream(StringUtil.toUTF8(contents));
        }

        return new Resource(filename, in);
    }

    @Override
    public void loadScript(IScriptThread thread, String filename) throws IOException, ScriptException {
        filename = findScriptFile(filename);

        LuaScriptThread luaThread = (LuaScriptThread)thread;

        Varargs loadResult = BaseLib.loadFile(filename);
        if (!loadResult.arg1().isclosure()) {
            throw new ScriptException("Error loading script, " + filename + ": " + loadResult.arg(2));
        }

        luaThread.call(loadResult.checkclosure(1));
    }

    private static class LuaScriptResourceLoader extends ResourceLoader {

        private static final long serialVersionUID = 1L;

        private final IEnvironment env;

        private transient FileSystemView cachedFileSystemView;

        public LuaScriptResourceLoader(IEnvironment env) {
            super(env.getResourceLoadLog());

            this.env = env;
        }

        protected final FileSystemView getFileSystem() {
            if (cachedFileSystemView == null) {
                cachedFileSystemView = new FileSystemView(env.getFileSystem(), "script/");
            }
            return cachedFileSystemView;
        }

        public static boolean isBuiltInScript(String filename) {
            return filename.startsWith("builtin/");
        }

        public static URL getClassResource(String filename) {
            return LuaScriptLoader.class.getResource("/script/" + filename);
        }

        @Override
        protected boolean isValidFilename(String filename) {
            if (isBuiltInScript(filename)) {
                return getClassResource(filename) != null;
            }
            return getFileSystem().getFileExists(filename);
        }

        public InputStream newInputStream(String filename) throws IOException {
            if (isBuiltInScript(filename)) {
                URL url = LuaScriptResourceLoader.getClassResource(filename);
                if (url == null) {
                    throw new FileNotFoundException(filename);
                }
                return url.openStream();
            }

            return getFileSystem().openInputStream(filename);
        }

        public long getModifiedTime(String filename) throws IOException {
            if (isBuiltInScript(filename)) {
                return 0L;
            }
            return getFileSystem().getFileModifiedTime(filename);
        }

        @Override
        protected List<String> getFiles(String folder) throws IOException {
            List<String> out = new ArrayList<String>();
            getFileSystem().getFiles(out, folder, true);
            return out;
        }

    }

}