package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class DuelStatsUtil {
    private DuelStatsUtil() {
    }

    public static boolean healToFull(Player player) {
        return setStatToMax(player, "Health");
    }

    public static boolean staminaToFull(Player player) {
        return setStatToMax(player, "Stamina");
    }

    public static void healAndStaminaToFull(Player player) {
        setStatToMax(player, "Health");
        setStatToMax(player, "Stamina");
    }

    /**
     * Sets current Health to an integer value (clamped to [min,max]).
     */
    public static boolean setHealth(Player player, int hp) {
        return setStat(player, "Health", (float) hp, true);
    }

    /**
     * Sets current Stamina to an integer value (clamped to [min,max]).
     */
    public static boolean setStamina(Player player, int stamina) {
        return setStat(player, "Stamina", (float) stamina, true);
    }

    /**
     * Sets a named stat to its max value.
     * Returns true if it successfully queued the update, false otherwise.
     * <p>
     * No CommandContext required (no chat output).
     */
    public static boolean setStatToMax(Player player, String statName) {
        if (player == null || statName == null) return false;

        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) return false;

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return false;

        final int statIndex = EntityStatType.getAssetMap().getIndex(statName);
        if (statIndex == Integer.MIN_VALUE) return false;

        world.execute(() -> {
            Store<EntityStore> s = ref.getStore();

            EntityStatMap statMap = (EntityStatMap) s.getComponent(
                    ref,
                    EntityStatsModule.get().getEntityStatMapComponentType()
            );
            if (statMap == null) return;

            EntityStatValue statValue = statMap.get(statIndex);
            if (statValue == null) return;

            statMap.setStatValue(statIndex, statValue.getMax());
        });

        return true;
    }

    /**
     * Sets a named stat to a specific value.
     * If clamp=true, clamps to [min,max] from EntityStatValue.
     */
    public static boolean setStat(Player player, String statName, float value, boolean clamp) {
        if (player == null || statName == null) return false;

        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) return false;

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return false;

        final int statIndex = EntityStatType.getAssetMap().getIndex(statName);
        if (statIndex == Integer.MIN_VALUE) return false;

        world.execute(() -> {
            Store<EntityStore> s = ref.getStore();

            EntityStatMap statMap = (EntityStatMap) s.getComponent(
                    ref,
                    EntityStatsModule.get().getEntityStatMapComponentType()
            );
            if (statMap == null) return;

            EntityStatValue statValue = statMap.get(statIndex);
            if (statValue == null) return;

            float v = value;
            if (clamp) {
                float min = statValue.getMin();
                float max = statValue.getMax();
                if (v < min) v = min;
                if (v > max) v = max;
            }

            statMap.setStatValue(statIndex, v);
        });

        return true;
    }

    public static float getCurrentHealth(Player player) {
        if (player == null) return -1;

        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) return -1;

        Store<EntityStore> store = ref.getStore();
        final int healthStatIdx = EntityStatType.getAssetMap().getIndex("Health");
        if (healthStatIdx == Integer.MIN_VALUE) return -1;

        EntityStatMap statMap = (EntityStatMap) store.getComponent(
                ref,
                EntityStatsModule.get().getEntityStatMapComponentType()
        );

        if (statMap == null) return -1;

        EntityStatValue healthStat = statMap.get(healthStatIdx);

        if (healthStat == null) return -1;

        return healthStat.get();
    }
}
