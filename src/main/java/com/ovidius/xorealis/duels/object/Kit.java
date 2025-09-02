package com.ovidius.xorealis.duels.object;

import org.bukkit.inventory.ItemStack;

public class Kit {
    private final String id;
    private final String displayName;
    private final ItemStack icon;
    private final ItemStack[] inventoryContents;
    private final ItemStack[] armorContents;

    public Kit(String id, String displayName, ItemStack icon, ItemStack[] inventoryContents, ItemStack[] armorContents) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.inventoryContents = inventoryContents;
        this.armorContents = armorContents;
    }

    public String getId() {
        return id;
    }
    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public ItemStack[] getInventoryContents() {
        return inventoryContents;
    }

    public ItemStack[] getArmorContents() {
        return armorContents;
    }
}
