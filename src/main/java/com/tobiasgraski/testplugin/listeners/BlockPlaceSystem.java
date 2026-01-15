package com.tobiasgraski.testplugin.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class BlockPlaceSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public BlockPlaceSystem() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    @Override
    public void handle(int idx, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, PlaceBlockEvent event) {
        Ref<EntityStore> ref = chunk.getReferenceTo(idx);
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;
        event.setCancelled(player.getGameMode() == GameMode.Adventure);
    }
}
