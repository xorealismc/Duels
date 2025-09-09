package com.ovidius.xorealis.duels.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class Kit {
    private final String id;
    private final String displayName;
    private final ItemStack icon;
    private final ItemStack[] inventoryContents;
    private final ItemStack[] armorContents;
}
