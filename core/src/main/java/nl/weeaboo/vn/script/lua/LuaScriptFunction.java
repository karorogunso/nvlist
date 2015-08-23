package nl.weeaboo.vn.script.lua;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.Varargs;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.lua2.link.LuaFunctionLink;
import nl.weeaboo.lua2.link.LuaLink;
import nl.weeaboo.vn.script.IScriptFunction;
import nl.weeaboo.vn.script.ScriptException;

class LuaScriptFunction implements IScriptFunction {

    private static final long serialVersionUID = LuaImpl.serialVersionUID;

    private final LuaClosure func;
    private final Varargs args;

    public LuaScriptFunction(LuaClosure func, Varargs args) {
        this.func = func;
        this.args = args;
    }

    @Override
    public void call() throws ScriptException {
        LuaRunState runState = LuaImpl.getRunState();
        call(runState.getCurrentLink());
    }

    void call(LuaLink currentLink) throws ScriptException {
        LuaRunState runState = LuaImpl.getRunState();
        if (runState == null || currentLink == null) {
            throw new ScriptException("Unable to call Lua function -- no thread is current");
        }

        LuaLink oldLink = runState.getCurrentLink();
        try {
            runState.setCurrentLink(currentLink);
            currentLink.call(func, args);
        } catch (LuaException e) {
            throw LuaScriptUtil.toScriptException("Error calling function: " + this, e);
        } finally {
            runState.setCurrentLink(oldLink);
        }
    }

    LuaScriptThread callInNewThread() throws ScriptException {
        LuaRunState runState = LuaImpl.getRunState();
        LuaFunctionLink link = new LuaFunctionLink(runState, func, args);
        return new LuaScriptThread(link);
    }

    protected Varargs getArgs() {
        return args;
    }

}