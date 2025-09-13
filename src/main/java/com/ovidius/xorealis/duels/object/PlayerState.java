package com.ovidius.xorealis.duels.object;

import lombok.Value;
import org.bukkit.GameMode;
import org.bukkit.Location;

@Value
public class PlayerState {
    Location location;
    GameMode gameMode;
    double health;
    int foodLevel;
    float exp;
    int level;
}
