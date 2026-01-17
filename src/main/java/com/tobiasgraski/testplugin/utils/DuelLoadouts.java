package com.tobiasgraski.testplugin.utils;

import java.util.concurrent.TimeUnit;

import com.hypixel.hytale.server.core.HytaleServer;
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
            inv.getHotbar().setItemStackForSlot((short) 1, new ItemStack("Food_Cheese", 4));
            inv.getHotbar().setItemStackForSlot((short) 2, new ItemStack("Weapon_Shortbow_Combat", 1));
            inv.getHotbar().setItemStackForSlot((short) 3, new ItemStack("Weapon_Arrow_Crude", 16));

            inv.getArmor().setItemStackForSlot((short) 0, new ItemStack("Armor_Iron_Head", 1));
            inv.getArmor().setItemStackForSlot((short) 1, new ItemStack("Armor_Iron_Chest", 1));
            inv.getArmor().setItemStackForSlot((short) 2, new ItemStack("Armor_Iron_Hands", 1));
            inv.getArmor().setItemStackForSlot((short) 3, new ItemStack("Armor_Iron_Legs", 1));

            inv.setUsingToolsItem(false);
            inv.setActiveHotbarSlot((byte) 0);

            // Push equipment state
            player.sendInventory();
            player.invalidateEquipmentNetwork();

            // Mark stats dirty
            player.getStatModifiersManager().setRecalculate(true);

            // Delay ~2-3 ticks (30 TPS => 33ms/tick). Then hop back onto world thread.
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                world.execute(() -> {
                    // Force dirty again just before setting, in case the engine cleared the flag.
                    player.getStatModifiersManager().setRecalculate(true);

                    DuelStatsUtil.setHealth(player, 9999);
                    DuelStatsUtil.setStamina(player, 9999);
                });
            }, 100, TimeUnit.MILLISECONDS); // try 100ms first; bump to 150-200ms if needed
        });
    }
    
    public static void applyBarbarianDuelKit(Player player) {
        if (player == null) return;

        var ref = player.getReference();
        var store = ref.getStore();
        var world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return;

        world.execute(() -> {
            Inventory inv = player.getInventory();
            inv.clear();

            inv.getHotbar().setItemStackForSlot((short) 0, new ItemStack("Weapon_Daggers_Onyxium", 1));
            inv.getHotbar().setItemStackForSlot((short) 1, new ItemStack("Food_Bread", 8));
            inv.getHotbar().setItemStackForSlot((short) 2, new ItemStack("Weapon_Shortbow_Onyxium", 1));
            inv.getHotbar().setItemStackForSlot((short) 3, new ItemStack("Weapon_Arrow_Crude", 16));

            inv.getArmor().setItemStackForSlot((short) 0, new ItemStack("Armor_Onyxium_Head", 1));
            inv.getArmor().setItemStackForSlot((short) 1, new ItemStack("Armor_Onyxium_Chest", 1));
            inv.getArmor().setItemStackForSlot((short) 2, new ItemStack("Armor_Onyxium_Hands", 1));
            inv.getArmor().setItemStackForSlot((short) 3, new ItemStack("Armor_Onyxium_Legs", 1));

            inv.setUsingToolsItem(false);
            inv.setActiveHotbarSlot((byte) 0);

            // Push equipment state
            player.sendInventory();
            player.invalidateEquipmentNetwork();

            // Mark stats dirty
            player.getStatModifiersManager().setRecalculate(true);

            // Delay ~2-3 ticks (30 TPS => 33ms/tick). Then hop back onto world thread.
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                world.execute(() -> {
                    // Force dirty again just before setting, in case the engine cleared the flag.
                    player.getStatModifiersManager().setRecalculate(true);

                    DuelStatsUtil.setHealth(player, 9999);
                    DuelStatsUtil.setStamina(player, 9999);
                });
            }, 100, TimeUnit.MILLISECONDS); // try 100ms first; bump to 150-200ms if needed
        });
    }

    
    public static void applyArcherDuelKit(Player player) {
        if (player == null) return;

        var ref = player.getReference();
        var store = ref.getStore();
        var world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return;

        world.execute(() -> {
            Inventory inv = player.getInventory();
            inv.clear();

            inv.getHotbar().setItemStackForSlot((short) 0, new ItemStack("Weapon_Crossbow_Iron", 1));
            inv.getHotbar().setItemStackForSlot((short) 1, new ItemStack("Food_Bread", 8));
            inv.getHotbar().setItemStackForSlot((short) 2, new ItemStack("Weapon_Arrow_Crude", 100));
            inv.getHotbar().setItemStackForSlot((short) 3, new ItemStack("Weapon_Arrow_Crude", 100));

            inv.getArmor().setItemStackForSlot((short) 0, new ItemStack("Armor_Leather_Soft_Head", 1));
            inv.getArmor().setItemStackForSlot((short) 1, new ItemStack("Armor_Leather_Soft_Chest", 1));
            inv.getArmor().setItemStackForSlot((short) 2, new ItemStack("Armor_Leather_Soft_Hands", 1));
            inv.getArmor().setItemStackForSlot((short) 3, new ItemStack("Armor_Leather_Soft_Legs", 1));

            inv.setUsingToolsItem(false);
            inv.setActiveHotbarSlot((byte) 0);

            // Push equipment state
            player.sendInventory();
            player.invalidateEquipmentNetwork();

            // Mark stats dirty
            player.getStatModifiersManager().setRecalculate(true);

            // Delay ~2-3 ticks (30 TPS => 33ms/tick). Then hop back onto world thread.
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
                world.execute(() -> {
                    // Force dirty again just before setting, in case the engine cleared the flag.
                    player.getStatModifiersManager().setRecalculate(true);

                    DuelStatsUtil.setHealth(player, 9999);
                    DuelStatsUtil.setStamina(player, 9999);
                });
            }, 100, TimeUnit.MILLISECONDS); // try 100ms first; bump to 150-200ms if needed
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
