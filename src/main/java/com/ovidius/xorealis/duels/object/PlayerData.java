package com.ovidius.xorealis.duels.object;

import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Data
public class PlayerData  implements ConfigurationSerializable {

    private int soloElo = 1000;
    private int teamElo = 1000;

    private int wins = 0;
    private int losses = 0;
    private int kills = 0;
    private int winstreak = 0;

    public PlayerData() {}

    public int getElo(int teamSize) {
        return (teamSize == 1) ? soloElo : teamElo;
    }

    public void setElo(int teamSize, int newElo) {
        if (teamSize == 1) {
            this.soloElo = newElo;
        } else {
            this.teamElo = newElo;
        }
    }
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("solo-elo", soloElo);
        map.put("team-elo", teamElo);
        map.put("wins", wins);
        map.put("losses", losses);
        map.put("kills", kills);
        map.put("winstreak", winstreak);
        return map;
    }

    public PlayerData(Map<String, Object> map) {
        this.soloElo = (int) map.getOrDefault("solo-elo", 1000);
        this.teamElo = (int) map.getOrDefault("team-elo", 1000);
        this.wins = (int) map.getOrDefault("wins", 0);
        this.losses = (int) map.getOrDefault("losses", 0);
        this.kills = (int) map.getOrDefault("kills", 0);
        this.winstreak = (int) map.getOrDefault("winstreak", 0);
    }
}
