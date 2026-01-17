package com.tobiasgraski.testplugin.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tobiasgraski.testplugin.utils.ActiveDuels;

import java.util.UUID;

public class LobbyInfiniteStaminaSystem extends EntityTickingSystem<EntityStore> {

    private final Query<EntityStore> query;

    public LobbyInfiniteStaminaSystem() {
        this.query = Query.and(
                (Query) Player.getComponentType(),
                (Query) EntityStatMap.getComponentType()
        );
    }

    @Override
    public void tick(
            float dt,
            int index,
            ArchetypeChunk<EntityStore> archetypeChunk,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer
    ) {
        Player player = (Player) archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        UUID id = player.getUuid();
        if (id == null) return;

        // Only clamp stamina in lobby / when not in a duel
        if (ActiveDuels.isInDuel(id)) return;

        ComponentType<EntityStore, EntityStatMap> statType =
                (ComponentType<EntityStore, EntityStatMap>) EntityStatMap.getComponentType();
        if (statType == null) return;

        EntityStatMap statMap = (EntityStatMap) archetypeChunk.getComponent(index, statType);
        if (statMap == null) return;

        int staminaIndex = DefaultEntityStatTypes.getStamina();
        EntityStatValue staminaStat = statMap.get(staminaIndex);
        if (staminaStat == null) return;

        float maxStamina = staminaStat.getMax();
        statMap.setStatValue(staminaIndex, maxStamina);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }
}
