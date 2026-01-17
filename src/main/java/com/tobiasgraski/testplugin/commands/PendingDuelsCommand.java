package com.tobiasgraski.testplugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tobiasgraski.testplugin.pages.PendingDuelsPage;

public class PendingDuelsCommand extends AbstractPlayerCommand {

    public PendingDuelsCommand() {
        super("pendingduels", "Open the Pending duels UI", false);
    }

    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
        var player = store.getComponent(ref, Player.getComponentType());
        var page = new PendingDuelsPage(playerRef);

        player.getPageManager().openCustomPage(ref, store, page);
    }
}