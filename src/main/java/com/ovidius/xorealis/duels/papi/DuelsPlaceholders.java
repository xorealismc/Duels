package com.ovidius.xorealis.duels.papi;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Party;
import com.ovidius.xorealis.duels.object.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ovidius.xorealis.duels.manager.PartyManager;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Optional;

public class DuelsPlaceholders extends PlaceholderExpansion {

    private final XorealisDuels plugin;

    private final DecimalFormat eloFormat = new DecimalFormat("#,##0.00");

    public DuelsPlaceholders(XorealisDuels plugin) {
        this.plugin = plugin;
    }


    @Override public @NotNull String getIdentifier() {
        return "xorealisduels";
    }
    @Override public @NotNull String getAuthor() {
        return "SnappyWave";
    }
    @Override public @NotNull String getVersion() {
        return "1.0";
    }
    @Override public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;

        if (params.startsWith("party_")) {
            return handlePartyPlaceholders(player, params);
        }

        if (params.startsWith("stats_")) {
            return handleStatsPlaceholders(player, params);
        }

        return null;
    }
    private String handlePartyPlaceholders(Player player, String params) {
        Optional<Party> partyOpt = plugin.getPartyManager().getParty(player);

        if (params.equals("party_exists")) {
            return partyOpt.isPresent() ? "yes" : "no";
        }

        if (partyOpt.isEmpty()) return "";

        Party party = partyOpt.get();

        switch (params) {
            case "party_leader":
                return Bukkit.getOfflinePlayer(party.getLeader()).getName();
            case "party_size":
                return String.valueOf(party.getSize());
        }
        return null;
    }
    private String handleStatsPlaceholders(Player player, String params) {
        PlayerData data = plugin.getPlayerDataManager().loadPlayerData(player.getUniqueId());

        switch (params) {
            case "stats_elo":
                return String.valueOf(data.getSoloElo());
            case "stats_wins":
                return String.valueOf(data.getWins());
            case "stats_losses":
                return String.valueOf(data.getLosses());
        }
        return null;
    }

}
