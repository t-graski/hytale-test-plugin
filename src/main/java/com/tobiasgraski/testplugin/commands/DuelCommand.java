package com.tobiasgraski.testplugin.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.tobiasgraski.testplugin.utils.ActiveDuels;   // <-- ADD
import com.tobiasgraski.testplugin.utils.DuelLoadouts;
import com.tobiasgraski.testplugin.utils.DuelRequests;

import java.awt.*;
import java.util.Optional;

public class DuelCommand extends CommandBase {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<PlayerRef> playerArg;
    private final OptionalArg<String> kitArg;

    public DuelCommand() {
        super("Duel", "Duel commands: /duel request <target> [kit], /duel accept <sender>");
        this.setPermissionGroup(GameMode.Adventure);

        actionArg = withRequiredArg("action", "request | accept", ArgTypes.STRING);
        playerArg = withRequiredArg("player", "target (request) or sender (accept)", ArgTypes.PLAYER_REF);
        kitArg = withOptionalArg("kit", "The kit to be played with", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        if (!ctx.isPlayer()) return;

        Player executor = (Player) ctx.sender();
        PlayerRef executorRef = Universe.get().getPlayerByUsername(executor.getDisplayName(), NameMatching.EXACT);

        PlayerRef otherRef = ctx.get(playerArg);
        String action = ctx.get(actionArg).toLowerCase();

        switch (action) {
            case "request" -> handleRequest(executorRef, otherRef, Optional.of(ctx.get(kitArg)));
            case "accept" -> handleAccept(ctx, executorRef, otherRef);
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

    private static float yawToFace(double fromX, double fromZ, double toX, double toZ) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;

        return (float) Math.atan2(-dx, -dz); // radians
    }

    private void handleRequest(PlayerRef sender, PlayerRef target, Optional<String> kit) {
        if (target == null) {
            sender.sendMessage(Message.raw("That player isn't available right now").color(Color.RED));
            return;
        }

        if (target.getUuid().equals(sender.getUuid())) {
            sender.sendMessage(Message.raw("You cannot duel yourself").bold(true).color(Color.RED));
            return;
        }

        // NEW: block if either is already in an active duel
        if (ActiveDuels.isInDuel(sender.getUuid())) {
            sender.sendMessage(Message.raw("You are already in an active duel").bold(true).color(Color.RED));
            return;
        }
        if (ActiveDuels.isInDuel(target.getUuid())) {
            sender.sendMessage(Message.raw(target.getUsername() + " is already in an active duel")
                    .bold(true).color(Color.RED));
            return;
        }

        DuelRequests.put(target.getUuid(), sender.getUuid(), sender.getUsername(), kit);

        sender.sendMessage(
                Message.raw("You sent a duel request to ").color(Color.RED)
                        .insert(target.getUsername()).bold(true).color(Color.GREEN)
        );

        target.sendMessage(
                Message.raw(sender.getUsername()).bold(true).color(Color.GREEN)
                        .insert(" sent you a duel request. ").color(Color.RED)
                        .insert("Type ").color(Color.GRAY)
                        .insert("/duel accept " + sender.getUsername()).bold(true).color(Color.YELLOW)
                        .insert(" to accept. This request expires in " + DuelRequests.EXPIRATION_TIME_MS / 1000 + " seconds")
                        .bold(true).color(Color.GREEN)
        );
    }

    private void handleAccept(CommandContext ctx, PlayerRef target, PlayerRef sender) {
        DuelRequests.PendingDuel pending = DuelRequests.consume(target.getUuid());
        if (pending == null) {
            target.sendMessage(Message.raw("You have no pending duel requests.").color(Color.RED));
            return;
        }

        boolean matches =
                pending.senderUuid.equals(sender.getUuid()) ||
                        pending.senderName.equalsIgnoreCase(sender.getUsername());

        if (!matches) {
            DuelRequests.put(target.getUuid(), pending.senderUuid, pending.senderName, pending.kit.describeConstable());

            target.sendMessage(
                    Message.raw("Your pending duel request is from ").color(Color.RED)
                            .insert(pending.senderName).bold(true).color(Color.GREEN)
                            .insert(". Use /duel accept " + pending.senderName).color(Color.GRAY)
            );
            return;
        }

        // NEW: resolve real players and ensure both are online before starting duel
        Player senderPlayer = resolveOnlinePlayer(sender);
        Player targetPlayer = resolveOnlinePlayer(target);
        if (senderPlayer == null || targetPlayer == null) {
            target.sendMessage(Message.raw("That duel request is no longer available.").color(Color.RED));
            return;
        }

        // NEW: block if either is already in an active duel
        if (ActiveDuels.isInDuel(sender.getUuid()) || ActiveDuels.isInDuel(target.getUuid())) {
            target.sendMessage(Message.raw("A duel could not be started because one of you is already in an active duel")
                    .bold(true).color(Color.RED));

            if (!ActiveDuels.isInDuel(target.getUuid())) {
                // If the acceptor isn't in a duel, tell them who is (nice UX)
                target.sendMessage(Message.raw(sender.getUsername() + " is already in an active duel")
                        .color(Color.RED));
            }
            return;
        }

        ActiveDuels.start(target.getUuid(), sender.getUuid());

        DuelLoadouts.applyBasicDuelKit(senderPlayer);
        DuelLoadouts.applyBasicDuelKit(targetPlayer);

        // keep pitch 0 so they look level; roll unused
//        TeleportUtil.teleport(senderPlayer, sx, sy, sz, new com.hypixel.hytale.math.vector.Vector3f(0.0f, senderYaw, 0.0f));
//        TeleportUtil.teleport(targetPlayer, tx, ty, tz, new com.hypixel.hytale.math.vector.Vector3f(0.0f, targetYaw, 0.0f));

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
