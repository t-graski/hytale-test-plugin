package com.tobiasgraski.testplugin;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.plugin.*;
import com.tobiasgraski.testplugin.commands.DuelCommand;
import com.tobiasgraski.testplugin.commands.HelloCommand;
import com.tobiasgraski.testplugin.commands.PendingDuelsCommand;
import com.tobiasgraski.testplugin.listeners.BlockBreakSystem;
import com.tobiasgraski.testplugin.listeners.BlockPlaceSystem;
import com.tobiasgraski.testplugin.utils.DuelRequests;
import com.tobiasgraski.testplugin.listeners.DuelDisconnectListener;

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
    protected void start() {
        getEntityStoreRegistry().registerSystem(new BlockBreakSystem());
        getEntityStoreRegistry().registerSystem(new BlockPlaceSystem());
        getEntityStoreRegistry().registerSystem((ISystem) new DuelDeathSystem());
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, DuelDisconnectListener::onPlayerDisconnect);
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
        getCommandRegistry().registerCommand(new PendingDuelsCommand());
    }
}
