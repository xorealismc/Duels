package com.ovidius.xorealis.duels.object;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;


public record PlayerKitLayout(ItemStack[] inventoryContents, ItemStack[] armorContents)
        implements ConfigurationSerializable {

    public static PlayerKitLayout deserialize(Map<String, Object> args) {
        return new PlayerKitLayout(
                ((java.util.List<ItemStack>) args.get("inventory-contents")).toArray(new ItemStack[0]),
                ((java.util.List<ItemStack>) args.get("armor-contents")).toArray(new ItemStack[0])
        );
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of(
                "inventory-contents", Arrays.asList(inventoryContents),
                "armor-contents", Arrays.asList(armorContents)
        );
    }
}