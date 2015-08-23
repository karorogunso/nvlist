package nl.weeaboo.vn.core.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.core.IContextManager;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.IModule;
import nl.weeaboo.vn.core.INovel;
import nl.weeaboo.vn.core.InitException;
import nl.weeaboo.vn.save.ISaveModule;

public abstract class AbstractNovel implements INovel {

    // --- Note: This class uses manual serialization ---
    private EnvironmentFactory envFactory;
    private IEnvironment env;
    // --- Note: This class uses manual serialization ---

    public AbstractNovel(EnvironmentFactory envFactory) {
        this.envFactory = Checks.checkNotNull(envFactory);
    }

    @Override
    public void readAttributes(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (env != null) {
            env.destroy();
        }

        env = (IEnvironment)in.readObject();
    }

    @Override
    public void writeAttributes(ObjectOutput out) throws IOException {
        out.writeObject(env);
    }

    @Override
    public void start(String mainFuncName) throws InitException {
        env = envFactory.build();
    }

    @Override
    public void stop() {
    }

    @Override
    public void update() {
        for (IModule module : env.getModules()) {
            module.update();
        }

        getContextManager().update();
    }

    @Override
    public IEnvironment getEnv() {
        return env;
    }

    protected IContextManager getContextManager() {
        return env.getContextManager();
    }

    protected ISaveModule getSaveModule() {
        return env.getSaveModule();
    }

}