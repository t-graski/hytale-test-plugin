package com.tobiasgraski.testplugin.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;

import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;

import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tobiasgraski.testplugin.utils.ActiveDuels;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DuelDeathSystem extends EntityTickingSystem<EntityStore> {

    // “run once per death” guard
    private final ConcurrentHashMap<UUID, Long> handled = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                DeathComponent.getComponentType(),
                TransformComponent.getComponentType(),
                UUIDComponent.getComponentType()
        );
    }

    @Override
    public void tick(float dt, int index, ArchetypeChunk<EntityStore> chunk,
                     Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {

        UUIDComponent uuidComp = (UUIDComponent) chunk.getComponent(index, UUIDComponent.getComponentType());
        TransformComponent transform = (TransformComponent) chunk.getComponent(index, TransformComponent.getComponentType());

        if (uuidComp == null || transform == null) return;

        UUID playerId = uuidComp.getUuid();

        long now = System.currentTimeMillis();
        Long prev = handled.putIfAbsent(playerId, now);
        if (prev != null && (now - prev) < 5_000) return;

        
        ActiveDuels.end(playerId, ActiveDuels.EndReason.DEATH);
        
        handled.put(playerId, now);
    }
}
