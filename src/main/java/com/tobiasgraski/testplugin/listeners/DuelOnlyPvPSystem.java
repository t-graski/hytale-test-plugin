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
import com.tobiasgraski.testplugin.utils.DuelStatsUtil;

import java.util.UUID;

public class DuelOnlyPvPSystem extends DamageEventSystem {

    @Override
    public SystemGroup getGroup() {
        // same group as PreventDamageSystem: cancels damage early
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public Query getQuery() {
        return PlayerRef.getComponentType();
    }

    @Override
    public void handle(
            int index,
            ArchetypeChunk archetypeChunk,
            Store store,
            CommandBuffer commandBuffer,
            Damage damage
    ) {
        if (damage.isCancelled()) return;

        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> attackerRef = (Ref<EntityStore>) entitySource.getRef();
        if (!attackerRef.isValid()) return;

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

        var victimHealth = DuelStatsUtil.getCurrentHealth(victim);
        // Check if player would die and cancel event & end duel
        if (victimHealth > 0 && victimHealth - damage.getAmount() <= 0) {
            damage.setCancelled(true);
            ActiveDuels.end(attackerId, ActiveDuels.EndReason.DEATH);
            return;
        }

        // Only allow damage if they are opponents in an active duel
        if (!ActiveDuels.areOpponents(attackerId, victimId)) {
            damage.setCancelled(true);
        }
    }
}
