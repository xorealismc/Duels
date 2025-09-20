package com.ovidius.xorealis.duels.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;

@Getter
public class Arena {
    private final String id;
    private final String displayName;
    private final List<Location> team1Spawns;
    private final List<Location> team2Spawns;

    @Setter
    private ArenaState state;

    public Arena(String id, String displayName, List<Location> team1Spawns, List<Location> team2Spawns) {
        this.id = id;
        this.displayName = displayName;
        this.team1Spawns = team1Spawns;
        this.team2Spawns = team2Spawns;
        this.state = ArenaState.AVAILABLE;
    }
}