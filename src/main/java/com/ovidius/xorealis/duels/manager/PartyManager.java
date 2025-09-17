package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.object.Party;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PartyManager {
    private final Map<UUID, Party> playerPartyMap = new HashMap<>();
    public void createParty(Player leader) {
        if(playerPartyMap.containsKey(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Вы уже находитесь в пати! Пожалуйста покиньте её");
            return;
        }
        Party party = new Party(leader);
        playerPartyMap.put(leader.getUniqueId(), party);
        leader.sendMessage("Вы создали пати!");
    }

    public Optional<Party> getParty(Player player) {
        return Optional.ofNullable(playerPartyMap.get(player.getUniqueId()));
    }
}
