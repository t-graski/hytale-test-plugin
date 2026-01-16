package com.tobiasgraski.testplugin;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.tobiasgraski.testplugin.commands.DuelCommand;
import com.tobiasgraski.testplugin.commands.HelloCommand;
import com.tobiasgraski.testplugin.commands.PendingDuelsCommand;
import com.tobiasgraski.testplugin.listeners.BlockBreakSystem;
import com.tobiasgraski.testplugin.listeners.BlockPlaceSystem;
import com.tobiasgraski.testplugin.listeners.DuelDeathSystem;
import com.tobiasgraski.testplugin.listeners.DuelDisconnectListener;
import com.tobiasgraski.testplugin.utils.ActiveDuels;
import com.tobiasgraski.testplugin.utils.DuelRequests;

public class Main extends JavaPlugin {

    public Main(JavaPluginInit init) {
        super(init);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setup() {
        registerHandlers();
        registerHello();

    ScheduledFuture<Void> checkExpiredTask =
        (ScheduledFuture<Void>) HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            DuelRequests.checkExpiredRequests();
            ActiveDuels.checkExpiredDuels();
        }, 0, 3, TimeUnit.SECONDS);
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
