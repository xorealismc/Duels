package com.ovidius.xorealis.duels.papi;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.manager.PartyManager;
import com.ovidius.xorealis.duels.object.Party;
import com.ovidius.xorealis.duels.object.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DuelsPlaceholders extends PlaceholderExpansion {

    private final XorealisDuels plugin;

    public DuelsPlaceholders(XorealisDuels plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "xorealisduels"; }
    @Override public @NotNull String getAuthor() { return "SnappyWave & Ovidius"; }
    @Override public @NotNull String getVersion() { return "2.0-FINAL"; } // Финальная версия
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;

        String lowerParams = params.toLowerCase();

        if (lowerParams.startsWith("stats_")) {
            return handleStatsPlaceholders(player, lowerParams);
        }

        if (lowerParams.startsWith("party_")) {
            return handlePartyPlaceholders(player, lowerParams);
        }

        return null;
    }

    private String handleStatsPlaceholders(Player player, String params) {
        PlayerData data = plugin.getPlayerDataManager().loadPlayerData(player.getUniqueId());
        return switch (params) {
            case "stats_elo" -> String.valueOf(data.getSoloElo());
            case "stats_team_elo" -> String.valueOf(data.getTeamElo());
            case "stats_wins" -> String.valueOf(data.getWins());
            case "stats_losses" -> String.valueOf(data.getLosses());
            default -> null;
        };
    }

    private String handlePartyPlaceholders(Player player, String params) {
        Optional<Party> partyOpt = plugin.getPartyManager().getParty(player);

        if (params.equals("party_header")) {
            return partyOpt
                    .map(party -> "§aПати (§f" + party.getSize() + "§a/§f" + PartyManager.MAX_PARTY_SIZE + "§a):")
                    .orElse("§eОдиночная игра:");
        }

        if (params.startsWith("party_member_")) {
            int index;
            try {
                index = Integer.parseInt(params.substring("party_member_".length())) - 1;
            } catch (NumberFormatException e) { return ""; }
            if(index < 0) return "";

            if (partyOpt.isEmpty()) {
                return (index == 0) ? "§7- §f" + player.getName() : "";
            } else {
                Party party = partyOpt.get();
                // Теперь эта строка получает список в гарантированном порядке
                List<UUID> membersList = new ArrayList<>(party.getMembers());

                if (index >= membersList.size()) return "";

                UUID memberUUID = membersList.get(index);
                // Используем OfflinePlayer для надежности, если игрок на мгновение выйдет
                OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);

                return party.getLeader().equals(memberUUID) ? "§6☆ §f" + member.getName() : "§7- §f" + member.getName();
            }
        }
        return null;
    }
}