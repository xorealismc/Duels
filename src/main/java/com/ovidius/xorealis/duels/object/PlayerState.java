package com.ovidius.xorealis.duels.object;

import lombok.Value;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@Value
public class PlayerState {
    Location location;
    ItemStack[] inventoryContents;
    ItemStack[] armorContents;
    GameMode gameMode;
    double health;
    int foodLevel;
    float exp;
    int level;
}
