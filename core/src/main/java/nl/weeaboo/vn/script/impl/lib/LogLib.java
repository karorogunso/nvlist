package nl.weeaboo.vn.script.impl.lib;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import nl.weeaboo.lua2.LuaUtil;
import nl.weeaboo.lua2.luajava.CoerceLuaToJava;
import nl.weeaboo.lua2.vm.LuaConstants;
import nl.weeaboo.lua2.vm.Varargs;
import nl.weeaboo.vn.script.ScriptFunction;

public class LogLib extends LuaLib {

    private static final long serialVersionUID = 1L;

    public LogLib() {
        super("Log");
    }

    private Logger getLogger() {
        return getLogger(LuaUtil.getLuaStack());
    }
    protected Logger getLogger(List<String> luaStackTrace) {
        String luaScriptFile = Iterables.getFirst(luaStackTrace, "unknown");

        return LoggerFactory.getLogger("lua." + luaScriptFile);
    }

    private String getLogFormat(Varargs args) {
        return args.tojstring(1);
    }
    private Object[] getLogArgs(Varargs args) {
        Object[] result = new Object[args.narg() - 1];
        for (int n = 0; n < result.length; n++) {
            result[n] = CoerceLuaToJava.coerceArg(args.arg(2 + n), Object.class);
        }
        return result;
    }

    /**
     * @param args
     *        <ol>
     *        <li>format string
     *        <li>any number of format args
     *        </ol>
     */
    @ScriptFunction
    public Varargs trace(Varargs args) {
        getLogger().trace(getLogFormat(args), getLogArgs(args));
        return LuaConstants.NONE;
    }

    /**
     * @param args
     *        <ol>
     *        <li>format string
     *        <li>any number of format args
     *        </ol>
     */
    @ScriptFunction
    public Varargs debug(Varargs args) {
        getLogger().debug(getLogFormat(args), getLogArgs(args));
        return LuaConstants.NONE;
    }

    /**
     * @param args
     *        <ol>
     *        <li>format string
     *        <li>any number of format args
     *        </ol>
     */
    @ScriptFunction
    public Varargs info(Varargs args) {
        getLogger().info(getLogFormat(args), getLogArgs(args));
        return LuaConstants.NONE;
    }

    /**
     * @param args
     *        <ol>
     *        <li>format string
     *        <li>any number of format args
     *        </ol>
     */
    @ScriptFunction
    public Varargs warn(Varargs args) {
        getLogger().warn(getLogFormat(args), getLogArgs(args));
        return LuaConstants.NONE;
    }

    /**
     * @param args
     *        <ol>
     *        <li>format string
     *        <li>any number of format args
     *        </ol>
     */
    @ScriptFunction
    public Varargs error(Varargs args) {
        getLogger().error(getLogFormat(args), getLogArgs(args));
        return LuaConstants.NONE;
    }

}
