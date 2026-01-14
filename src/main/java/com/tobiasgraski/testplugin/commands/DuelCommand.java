package com.tobiasgraski.testplugin.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tobiasgraski.testplugin.utils.DuelLoadouts;
import com.tobiasgraski.testplugin.utils.DuelRequests;
import com.tobiasgraski.testplugin.utils.TeleportUtil;

import java.awt.*;

public class DuelCommand extends CommandBase {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<PlayerRef> playerArg;

    public DuelCommand() {
        super("Duel", "Duel commands: /duel request <target>, /duel accept <sender>");
//        this.setPermissionGroup(GameMode.Adventure);

        actionArg = withRequiredArg("action", "request | accept", ArgTypes.STRING);
        playerArg = withRequiredArg("player", "target (request) or sender (accept)", ArgTypes.PLAYER_REF);
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        if (!ctx.isPlayer()) return;

        Player executor = (Player) ctx.sender();
        PlayerRef executorRef = Universe.get().getPlayerByUsername(executor.getDisplayName(), NameMatching.EXACT);

        PlayerRef otherRef = ctx.get(playerArg);
        String action = ctx.get(actionArg).toLowerCase();

        switch (action) {
            case "request" -> handleRequest(executorRef, otherRef);
            case "accept" -> handleAccept(executorRef, otherRef);
            default -> executor.sendMessage(
                    Message.raw("Usage: ").color(Color.RED)
                            .insert("/duel request <target>").bold(true).color(Color.YELLOW)
                            .insert(" or ").color(Color.RED)
                            .insert("/duel accept <sender>").bold(true).color(Color.YELLOW)
            );
        }
    }

    private Player resolveOnlinePlayer(PlayerRef ref) {
        if (ref == null || ref.getWorldUuid() == null) return null;
        return (Player) Universe.get().getWorld(ref.getWorldUuid()).getEntity(ref.getUuid());
    }

    private void handleRequest(PlayerRef sender, PlayerRef target) {

// COMMENTED BELOW TO ALLOW FOR ONE-PLAYER TESTING
// UNCOMMENT IN PROD OR TWO-PLAYER TESTING
//        if (targetRef.getUuid().equals(sender.getUuid())) {
//            sender.sendMessage(Message.raw("You cannot duel yourself.").bold(true).color(Color.RED));
//            return;
//        }

//        Player targetPlayer = (Player) sender.getWorld().getEntity(targetRef.getUuid());
        if (target == null) {
            sender.sendMessage(Message.raw("That player isn't available right now.").color(Color.RED));
            return;
        }

        DuelRequests.put(target.getUuid(), sender.getUuid(), sender.getUsername());

        sender.sendMessage(
                Message.raw("You sent a duel request to ").color(Color.RED)
                        .insert(target.getUsername()).bold(true).color(Color.GREEN)
        );

        target.sendMessage(
                Message.raw(sender.getUsername()).bold(true).color(Color.GREEN)
                        .insert(" sent you a duel request. ").color(Color.RED)
                        .insert("Type ").color(Color.GRAY)
                        .insert("/duel accept " + sender.getUsername()).bold(true).color(Color.YELLOW)
        );
    }

    private void handleAccept(PlayerRef target, PlayerRef sender) {
        DuelRequests.PendingDuel pending = DuelRequests.consume(target.getUuid());
        if (pending == null) {
            target.sendMessage(Message.raw("You have no pending duel requests.").color(Color.RED));
            return;
        }

        boolean matches =
                pending.senderUuid.equals(sender.getUuid()) ||
                        pending.senderName.equalsIgnoreCase(sender.getUsername());

        if (!matches) {
            DuelRequests.put(target.getUuid(), pending.senderUuid, pending.senderName);

            target.sendMessage(
                    Message.raw("Your pending duel request is from ").color(Color.RED)
                            .insert(pending.senderName).bold(true).color(Color.GREEN)
                            .insert(". Use /duel accept " + pending.senderName).color(Color.GRAY)
            );
            return;
        }

//        if (senderPlayer == null) {
//            target.sendMessage(Message.raw("That duel request is no longer available.").color(Color.RED));
//            return;
//        }

        // UNCOMMENT IN PROD OR TWO-PLAYER TESTING
        Player senderPlayer = resolveOnlinePlayer(sender);
        Player targetPlayer = resolveOnlinePlayer(target);
        DuelLoadouts.applyBasicDuelKit(senderPlayer);
        //DuelLoadouts.applyBasicDuelKit(target);

        TeleportUtil.teleport(senderPlayer, 10.0, 200.0, -20.0);
        //TeleportUtil.teleport(target,       10.0, 200.0,  20.0);

        senderPlayer.sendMessage(
                Message.raw(target.getUsername()).bold(true).color(Color.GREEN)
                        .insert(" accepted your duel request!").color(Color.RED)
        );

        target.sendMessage(
                Message.raw("You accepted ").color(Color.RED)
                        .insert(senderPlayer.getDisplayName()).bold(true).color(Color.GREEN)
                        .insert("'s duel request!").color(Color.RED)
        );
    }
}
