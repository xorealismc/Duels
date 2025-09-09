package com.ovidius.xorealis.duels.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
public class Arena {
    private final String id;
    private final Location spawn1;
    private final Location spawn2;
    @Setter
    private ArenaState state;

    public Arena(String id, Location spawn1, Location spawn2) {
        this.id = id;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
        this.state = ArenaState.AVAILABLE;
    }


}
