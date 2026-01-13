package com.tobiasgraski.testplugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.awt.*;

public class DuelCommand extends CommandBase {

    private final RequiredArg<PlayerRef> opponentArg;

    public DuelCommand() {
        super("Duel", "Duel another player.");

        opponentArg = withRequiredArg("opponent", "opponent", ArgTypes.PLAYER_REF);
    }

    @Override
    protected void executeSync(CommandContext ctx) {
        var opponent = ctx.get(opponentArg);

        if (ctx.isPlayer()) {
            var player = (Player) ctx.sender();

//            if (opponent.getUuid().equals(ctx.sender().getUuid())) {
//                player.sendMessage(Message.raw("You cannot duel yourself").bold(true).color(Color.RED));
//                return;
//            }

            player.sendMessage(Message.raw("You have dueled the player").color(Color.RED)
                    .insert(" " + opponent.getUsername()).bold(true).color(Color.GREEN));

            if (player.getReference() != null && player.getReference().isValid()) {
                var world = player.getWorld();

                if (world != null) {
                    world.execute(() -> {
                        var store = player.getReference().getStore();
                        player.moveTo(player.getReference(), 25d, 10d, 25d, store);
                    });
                }
            }

            opponent.sendMessage(Message.raw(player.getDisplayName()).bold(true).color(Color.GREEN)
                    .insert(" " + "has dueled you.").color(Color.RED));
        }
    }
}
