package nl.weeaboo.vn.script.lua;

import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.LuaTable;

import nl.weeaboo.common.Checks;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.vn.script.IScriptEnv;
import nl.weeaboo.vn.script.IScriptLoader;

public class LuaScriptEnv implements IScriptEnv {

	private final LuaRunState runState;
	private final LuaScriptLoader loader;
	private final List<ILuaScriptEnvInitializer> initializers = new ArrayList<ILuaScriptEnvInitializer>();

	private boolean initialized;

	public LuaScriptEnv(LuaRunState runState, LuaScriptLoader loader) {
	    this.runState = runState;
		this.loader = loader;
	}

	public void initEnv() throws LuaException {
	    initialized = true;

	    registerOnThread();
        loader.initEnv();

	    for (ILuaScriptEnvInitializer init : initializers) {
	        init.initEnv(this);
	    }
	}

	public void registerOnThread() {
	    runState.registerOnThread();
	}

	public void addInitializer(ILuaScriptEnvInitializer init) {
	    Checks.checkState(!initialized, "Can't change initializers after initEnv() has been called.");

	    initializers.add(init);
	}

	public LuaRunState getRunState() {
	    return runState;
	}

	public LuaTable getGlobals() {
	    return runState.getGlobalEnvironment();
	}

	@Override
	public IScriptLoader getScriptLoader() {
		return loader;
	}

}