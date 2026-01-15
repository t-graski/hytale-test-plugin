package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class DuelLoadouts {

    private DuelLoadouts() {}

    public static void applyBasicDuelKit(Player player) {
        if (player == null) return;

        var ref = player.getReference();
        var store = ref.getStore();
        var world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return;

        world.execute(() -> {
            Inventory inv = player.getInventory();
            inv.clear();

            inv.getHotbar().setItemStackForSlot((short) 0, new ItemStack("Weapon_Sword_Iron", 1));
            inv.getHotbar().setItemStackForSlot((short) 1, new ItemStack("Potion_Health_Greater", 1));

            inv.getArmor().setItemStackForSlot((short) 0, new ItemStack("Armor_Iron_Head", 1));
            inv.getArmor().setItemStackForSlot((short) 1, new ItemStack("Armor_Iron_Chest", 1));
            inv.getArmor().setItemStackForSlot((short) 2, new ItemStack("Armor_Iron_Hands", 1));
            inv.getArmor().setItemStackForSlot((short) 3, new ItemStack("Armor_Iron_Legs", 1));

            inv.setUsingToolsItem(false);
            inv.setActiveHotbarSlot((byte) 0);

            player.sendInventory();

            player.invalidateEquipmentNetwork();
            player.getStatModifiersManager().setRecalculate(true);

            world.execute(() -> {
                DuelStatsUtil.setHealth(player, 9999);
                DuelStatsUtil.setStamina(player, 9999);
            });
        });
    }

    

    public static void clearInventory(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        inv.setUsingToolsItem(false);
        inv.setActiveHotbarSlot((byte) 0);
        
        player.sendInventory();
    }
}		
