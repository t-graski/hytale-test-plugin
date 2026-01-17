package com.tobiasgraski.testplugin.listeners;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent.PlayerRequest;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.tobiasgraski.testplugin.utils.ActiveDuels;

public final class DuelNoDropSystem extends EntityEventSystem<EntityStore, PlayerRequest> {

    private final ComponentType<EntityStore, PlayerRef> playerRefType = PlayerRef.getComponentType();

    public DuelNoDropSystem() {
        super(PlayerRequest.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(playerRefType);
    }

    @Override
    public void handle(
            int index,
            ArchetypeChunk<EntityStore> chunk,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer,
            PlayerRequest event
    ) {
        PlayerRef playerRef = chunk.getComponent(index, playerRefType);
        if (playerRef == null) return;

        // Only block drops while player is in an active duel
        if (!ActiveDuels.isInDuel(playerRef.getUuid())) return;

        event.setCancelled(true);

    }
}
