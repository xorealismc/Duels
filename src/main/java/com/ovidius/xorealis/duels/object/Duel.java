package com.ovidius.xorealis.duels.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Stream;

@Getter
public class Duel {
    private final Arena arena;
    private final Kit kit;
    private final Set<UUID> team1;
    private final Set<UUID> team2;
    private final Set<UUID> alivePlayers;

    @Setter
    private DuelState state;

    public Duel(Arena arena, Kit kit, Player player1, Player player2) {
        this.arena = arena;
        this.kit = kit;
        this.state = DuelState.STARTING;
        this.team1 = Set.of(player1.getUniqueId());
        this.team2 = Set.of(player2.getUniqueId());
        this.alivePlayers = new HashSet<>(Arrays.asList(player1.getUniqueId(), player2.getUniqueId()));
    }

    public Duel(Arena arena, Kit kit, Party party1, Party party2) {
        this.arena = arena;
        this.kit = kit;
        this.state = DuelState.STARTING;
        this.team1 = new HashSet<>(party1.getMembers());
        this.team2 = new HashSet<>(party2.getMembers());
        this.alivePlayers = new HashSet<>();
        alivePlayers.addAll(team1);
        alivePlayers.addAll(team2);
    }

    public Stream<Player> getOnlineAlivePlayers() {
        return alivePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull);
    }

    public Stream<UUID> getAllParticipantUUIDs() {
        return Stream.concat(team1.stream(), team2.stream());
    }

    public Set<Player> getTeamAsPlayers(int teamNumber) {
        Set<UUID> teamUUIDs = (teamNumber == 1) ? team1 : team2;
        Set<Player> teamPlayers = new HashSet<>();
        for (UUID uuid : teamUUIDs) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) teamPlayers.add(p);
        }
        return teamPlayers;
    }
}