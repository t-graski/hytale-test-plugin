package com.tobiasgraski.testplugin.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

import java.util.UUID;

import com.tobiasgraski.testplugin.utils.ActiveDuels;

public final class DuelDisconnectListener {

    public DuelDisconnectListener() {}

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        if (event == null || event.getPlayerRef() == null) return;

        UUID playerUuid = event.getPlayerRef().getUuid();
        if (playerUuid == null) return;

        if (ActiveDuels.isInDuel(playerUuid)) {
            ActiveDuels.end(playerUuid, ActiveDuels.EndReason.FORFEIT);
        }
    }
}
