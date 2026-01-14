package com.tobiasgraski.testplugin;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.*;
import com.tobiasgraski.testplugin.commands.DuelCommand;
import com.tobiasgraski.testplugin.commands.HelloCommand;
import com.tobiasgraski.testplugin.utils.DuelRequests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {

    public Main(JavaPluginInit init) {
        super(init);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setup() {
        registerHandlers();
        registerHello();

        ScheduledFuture<Void> checkExpiredTask = (ScheduledFuture<Void>) HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(DuelRequests::checkExpiredRequests, 0, 3, TimeUnit.SECONDS);
        getTaskRegistry().registerTask(checkExpiredTask);
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
