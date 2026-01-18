package com.tobiasgraski.testplugin.commands;

import java.awt.Color;
import java.util.UUID;

import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tobiasgraski.testplugin.utils.ActiveDuels;
import com.tobiasgraski.testplugin.utils.TeleportUtil;

public class LobbyCommand extends CommandBase {
    public LobbyCommand() {
        super("Lobby", "Return to the server lobby.");
        this.setPermissionGroup(GameMode.Adventure);
        this.addAliases(new String[] { "l", "leave", "hub", "spawn" });
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        if (!ctx.isPlayer()) return;

        Player executor = (Player) ctx.sender();
        PlayerRef executorRef = Universe.get().getPlayerByUsername(executor.getDisplayName(), NameMatching.EXACT);
        UUID execId = executorRef.getUuid();

        // If they are in a duel, forfeit it (opponent becomes winner) and free the arena.
        if (ActiveDuels.isInDuel(execId)) {
            ActiveDuels.end(execId, ActiveDuels.EndReason.FORFEIT);

            executor.sendMessage(Message.raw("You forfeited the duel! Returning to the lobby...").bold(true).color(Color.YELLOW));
            return;
        }

        // Normal lobby behavior (not in duel)
        float pitch = 0.0f;
        float yawRad = (float) Math.toRadians(-90.0);   // pick any constant you like
        float roll = 0.0f;

        Vector3f rot = new Vector3f(pitch, yawRad, roll);
        TeleportUtil.teleport(executor, 651, 112, 252, rot);
        executor.sendMessage(Message.raw("Returning to the lobby...").bold(true).color(Color.GREEN));
    }
}
