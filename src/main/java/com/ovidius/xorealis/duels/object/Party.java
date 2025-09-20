package com.ovidius.xorealis.duels.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
public class Party {
    private UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();
    private final Map<UUID,Long> pendingInvites = new ConcurrentHashMap<>();

    public Party(Player leader) {
        this.leader = leader.getUniqueId();
        this.members.add(leader.getUniqueId());
    }

    public void addMember(Player player) {
        members.add(player.getUniqueId());
    }

    public void addInvite(Player target){pendingInvites.put(target.getUniqueId(),System.currentTimeMillis());}

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
    }

    public void removeInvite(Player target){pendingInvites.remove(target.getUniqueId());}

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public boolean isLeader(Player player) {
        return leader.equals(player.getUniqueId());
    }

    public boolean hasInvite(Player target) {
        return pendingInvites.containsKey(target.getUniqueId()) &&
                (System.currentTimeMillis() - pendingInvites.get(target.getUniqueId())) < 60_000;
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
