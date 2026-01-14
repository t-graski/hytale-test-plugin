package com.tobiasgraski.testplugin.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public final class DuelLoadouts {

    private DuelLoadouts() {}

    public static void applyBasicDuelKit(Player player) {
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
    }
    

    public static void clearInventory(Player player) {
        Inventory inv = player.getInventory();
        inv.clear();

        inv.setUsingToolsItem(false);
        inv.setActiveHotbarSlot((byte) 0);
        
        player.sendInventory();
    }
}
