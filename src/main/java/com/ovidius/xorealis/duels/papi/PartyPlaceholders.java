package com.ovidius.xorealis.duels.papi;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Party;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ovidius.xorealis.duels.manager.PartyManager;
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

        if(params.equalsIgnoreCase("party_exists")){
            return partyOpt.isPresent() ? "yes" : "no";
        }

        if (partyOpt.isEmpty()) {
            if(params.equalsIgnoreCase("party_display_name")) return "§eОдиночная игра";
            if(params.equalsIgnoreCase("party_list_1")) return "§7- " + player.getName();
            return "";
        }

        Party party = partyOpt.get();

        if(params.equalsIgnoreCase("party_display_name")) {
            return "§aПати (" + party.getSize() + "/" + PartyManager.MAX_PARTY_SIZE + ")";
        }

        if(params.startsWith("party_list_")) {
            try {
                int index = Integer.parseInt(params.substring(params.length() - 1)) - 1;

                return party.getOnlineMembers().stream()
                        .skip(index)
                        .findFirst()
                        .map(member -> {
                            if (party.isLeader(member)) {
                                return "§6☆ §f" + member.getName(); // ☆ Лидер
                            } else {
                                return "§7- §f" + member.getName(); // - Участник
                            }
                        })
                        .orElse("");
            } catch (Exception e) {
                return "";
            }
        }
        return null;
    }

}
