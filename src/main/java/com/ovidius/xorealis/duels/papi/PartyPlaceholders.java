package com.ovidius.xorealis.duels.papi;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Party;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.stream.Collectors;

public class PartyPlaceholders extends PlaceholderExpansion {

    private final XorealisDuels plugin;

    public PartyPlaceholders(XorealisDuels plugin) {
        this.plugin = plugin;
    }


    @Override
    public @NotNull String getIdentifier() {
        return "xorealisduels";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SnappyWave";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        Optional<Party> partyOpt = plugin.getPartyManager().getParty(player);
        if (partyOpt.isEmpty()) return "Не в пати";
        Party party = partyOpt.get();
        switch (params.toLowerCase()) {
            case "party_leader":
                return Bukkit.getOfflinePlayer(party.getLeader()).getName();
            case "party_size":
                return String.valueOf(party.getSize());
            case "party_member_list":
                return party.getOnlineMembers().stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));
            case "party_member_1":
            case "party_member_2":
            case "party_member_3":
                try{
                    int index = Integer.parseInt(params.substring(params.length()-1))-1;
                    return party.getOnlineMembers().stream()
                            .skip(index)
                            .findFirst()
                            .map(Player::getName)
                            .orElse("");
                }catch (Exception e){
                    return "";
                }
        }
        return null;
    }

}
