package com.tobiasgraski.testplugin.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.tobiasgraski.testplugin.utils.TeleportUtil;

import java.awt.*;

public class DuelCommand extends CommandBase {

    private final RequiredArg<PlayerRef> opponentArg;

    public DuelCommand() {
        super("Duel", "Duel another player.");
        this.setPermissionGroup(GameMode.Adventure);

        opponentArg = withRequiredArg("opponent", "opponent", ArgTypes.PLAYER_REF);
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        var opponent = ctx.get(opponentArg);

        if (ctx.isPlayer()) {
            Player player = (Player) ctx.sender();

//            if (opponent.getUuid().equals(ctx.sender().getUuid())) {
//                player.sendMessage(Message.raw("You cannot duel yourself").bold(true).color(Color.RED));
//                return;
//            }

            player.sendMessage(Message.raw("You sent a duel request to the player").color(Color.RED)
                    .insert(" " + opponent.getUsername()).bold(true).color(Color.GREEN));

            TeleportUtil.teleport(player, 10.0, 200.0, -20.0);

            opponent.sendMessage(Message.raw(player.getDisplayName()).bold(true).color(Color.GREEN)
                    .insert(" " + "sent you a duel request.").color(Color.RED));
        }
    }
}
