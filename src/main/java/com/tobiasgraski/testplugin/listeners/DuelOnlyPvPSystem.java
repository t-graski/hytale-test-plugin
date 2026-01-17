package com.tobiasgraski.testplugin.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
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
        // same group as PreventDamageSystem: cancels damage early
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

        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource)) return;

        Damage.EntitySource entitySource = (Damage.EntitySource) source;

        Ref<EntityStore> attackerRef = (Ref<EntityStore>) entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid()) return;

        // Attacker must be a player
        Player attacker = (Player) commandBuffer.getComponent(attackerRef, Player.getComponentType());
        if (attacker == null) return;

        // Victim must be a player (this system iterates players, but be safe)
        Ref<EntityStore> victimRef = (Ref<EntityStore>) archetypeChunk.getReferenceTo(index);
        Player victim = (Player) commandBuffer.getComponent(victimRef, Player.getComponentType());
        if (victim == null) return;

        UUID attackerId = attacker.getUuid();
        UUID victimId = victim.getUuid();
        if (attackerId == null || victimId == null) return;

        // Only allow damage if they are opponents in an active duel
        if (!ActiveDuels.areOpponents(attackerId, victimId)) {
            damage.setCancelled(true);
        }
    }
}
