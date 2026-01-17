package com.tobiasgraski.testplugin.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tobiasgraski.testplugin.utils.ActiveDuels;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class DuelOnlyPvPSystem extends DamageEventSystem {

    @Override
    @Nullable
    public SystemGroup getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    @Nonnull
    public Query getQuery() {
        return PlayerRef.getComponentType();
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk archetypeChunk,
            @Nonnull Store store,
            @Nonnull CommandBuffer commandBuffer,
            @Nonnull Damage damage
    ) {
        if (damage.isCancelled()) return;

        // Victim is the entity at this chunk index
        Ref<EntityStore> victimRef = (Ref<EntityStore>) archetypeChunk.getReferenceTo(index);
        Player victim = (Player) commandBuffer.getComponent(victimRef, Player.getComponentType());
        if (victim == null) return;

        UUID victimId = victim.getUuid();
        if (victimId == null) return;

        // 1) If victim is NOT in a duel, cancel ALL damage (fire, fall, mobs, pvp, everything)
        if (!ActiveDuels.isInDuel(victimId)) {
            damage.setCancelled(true);
            return;
        }

        // 2) If this damage is from a player attacker, enforce "only opponent can hurt you"
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> attackerRef = (Ref<EntityStore>) entitySource.getRef();
            if (attackerRef == null || !attackerRef.isValid()) return;

            Player attacker = (Player) commandBuffer.getComponent(attackerRef, Player.getComponentType());
            if (attacker == null) return; // not player attacker => allow (mob, projectile entity, etc.)

            UUID attackerId = attacker.getUuid();
            if (attackerId == null) return;

            // Only allow PvP if they are opponents
            if (!ActiveDuels.areOpponents(attackerId, victimId)) {
                damage.setCancelled(true);
            }
        }
    }
}
