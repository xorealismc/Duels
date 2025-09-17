package com.ovidius.xorealis.duels.object;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Party {
    private final UUID leader;
    private final Set<UUID> members = new HashSet<>();

    public Party(Player leader) {
        this.leader = leader.getUniqueId();
        this.members.add(leader.getUniqueId());
    }

    public void addMember(Player player) {
        members.add(player.getUniqueId());
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
    }

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public boolean isLeader(Player player) {
        return leader.equals(player.getUniqueId());
    }

    public int getSize(){
        return members.size();
    }

    public Set<Player> getOnlineMembers(){
        return members.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void broadcast(String message) {
        getOnlineMembers().forEach(player -> player.sendMessage(message));
    }
}
