package com.tobiasgraski.testplugin;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.plugin.*;
import com.tobiasgraski.testplugin.commands.DuelCommand;
import com.tobiasgraski.testplugin.commands.HelloCommand;

public class Main extends JavaPlugin {

    public Main(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        registerHandlers();
        registerHello();
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    private void registerHandlers() {
        var plugin = PluginManager.get().getPlugin(new PluginIdentifier("Test", "TestPlugin"));
    }

    private void registerHello() {
        getCommandRegistry().registerCommand(new HelloCommand());
        getCommandRegistry().registerCommand(new DuelCommand());
    }
}
