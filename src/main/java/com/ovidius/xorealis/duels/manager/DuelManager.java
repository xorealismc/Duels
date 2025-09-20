package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.*;
import com.ovidius.xorealis.duels.util.EffectUtil;
import com.ovidius.xorealis.duels.util.EloUtil;
import com.ovidius.xorealis.duels.util.SoundUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DuelManager implements Listener {
    private final XorealisDuels plugin;
    private final Map<UUID, Duel> activeDuels = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();

    public void startDuel(Player player1, Player player2, Arena arena, Kit kit) {
        Duel duel = new Duel(arena, kit, player1, player2);
        startDuelCommon(duel, List.of(player1), List.of(player2), arena);
    }

    public void startDuel(Party party1, Party party2, Arena arena, Kit kit) {
        Duel duel = new Duel(arena, kit, party1, party2);
        startDuelCommon(duel, party1.getOnlineMembers(), party2.getOnlineMembers(), arena);
    }

    private void startDuelCommon(Duel duel, Collection<Player> team1, Collection<Player> team2, Arena arena) {
        arena.setState(ArenaState.IN_USE);
        teleportTeam(team1, arena.getTeam1Spawns());
        teleportTeam(team2, arena.getTeam2Spawns());
        duel.getAllParticipantUUIDs().forEach(uuid -> activeDuels.put(uuid, duel));
        startCountdown(duel);
    }

    private void teleportTeam(Collection<Player> members, List<Location> spawns) {
        int i = 0;
        for (Player member : members) {
            Location spawn = spawns.get(Math.min(i++, spawns.size() - 1));
            preparePlayer(member, spawn);
        }
    }

    private void preparePlayer(Player player, Location spawnPoint) {
        PlayerState state = new PlayerState(
                player.getLocation(),
                player.getGameMode(),
                player.getHealth(),
                player.getFoodLevel(),
                player.getExp(),
                player.getLevel()
        );
        playerStates.put(player.getUniqueId(), state);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(spawnPoint);
    }

    private void startCountdown(Duel duel) {
        new BukkitRunnable() {
            private int countdown = 5;

            @Override
            public void run() {
                if (duel.getState() != DuelState.STARTING) {
                    this.cancel();
                    return;
                }
                if (countdown > 0) {
                    String title = "§a" + countdown;
                    duel.getOnlineAlivePlayers().forEach(p -> {
                        SoundUtil.playCountdownTick(p);
                        p.sendTitle(title, "", 0, 20, 5);
                    });
                    countdown--;
                } else {
                    this.cancel();
                    duel.setState(DuelState.ACTIVE);
                    duel.getOnlineAlivePlayers().forEach(p -> {
                        p.setGameMode(GameMode.SURVIVAL);
                        SoundUtil.playDuelStart(p);
                        p.sendTitle("§cБой!", "", 5, 15, 5);
                    });
                    applyKits(duel);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void applyKits(Duel duel) {
        Kit kit = duel.getKit();
        duel.getOnlineAlivePlayers().forEach(player ->
                plugin.getPlayerDataManager().loadPlayerLayout(player.getUniqueId(), kit.getId())
                        .ifPresentOrElse(
                                layout -> {
                                    player.getInventory().setContents(layout.inventoryContents());
                                    player.getInventory().setArmorContents(layout.armorContents());
                                },
                                () -> {
                                    player.getInventory().setContents(kit.getInventoryContents());
                                    player.getInventory().setArmorContents(kit.getArmorContents());
                                }
                        )
        );
    }

    public void endDuel(Duel duel, int winningTeamNumber) {
        if (duel.getState() == DuelState.ENDING) return;
        duel.setState(DuelState.ENDING);

        Set<Player> winners = duel.getTeamAsPlayers(winningTeamNumber);
        Set<Player> losers = duel.getTeamAsPlayers(winningTeamNumber == 1 ? 2 : 1);

        int teamSize = winners.size();

        Map<UUID, PlayerData> winnersData = new HashMap<>();
        winners.forEach(p -> winnersData.put(p.getUniqueId(), plugin.getPlayerDataManager().loadPlayerData(p.getUniqueId())));

        Map<UUID, PlayerData> losersData = new HashMap<>();
        losers.forEach(p -> losersData.put(p.getUniqueId(), plugin.getPlayerDataManager().loadPlayerData(p.getUniqueId())));

        int avgWinnerElo = (int) winnersData.values().stream()
                .mapToInt(data -> data.getElo(teamSize))
                .average()
                .orElse(1000);

        int avgLoserElo = (int) losersData.values().stream()
                .mapToInt(data -> data.getElo(teamSize))
                .average()
                .orElse(1000);

        int newAvgWinnerElo = EloUtil.calculateNewRating(avgWinnerElo, avgLoserElo, 1.0);
        int eloChange = newAvgWinnerElo - avgWinnerElo;

        winners.forEach(winner -> {
            PlayerData data = winnersData.get(winner.getUniqueId());

            int oldElo = data.getElo(teamSize);
            data.setElo(teamSize, oldElo + eloChange);
            data.setWins(data.getWins() + 1);
            data.setWinstreak(data.getWinstreak() + 1);

            plugin.getPlayerDataManager().savePlayerData(winner.getUniqueId(), data);
            winner.sendMessage("§aВаша команда победила! §2(+" + eloChange + " ELO)");
        });

        losers.forEach(loser -> {
            PlayerData data = losersData.get(loser.getUniqueId());

            int oldElo = data.getElo(teamSize);
            data.setElo(teamSize, oldElo - eloChange);
            data.setLosses(data.getLosses() + 1);
            data.setWinstreak(0);

            plugin.getPlayerDataManager().savePlayerData(loser.getUniqueId(), data);
            loser.sendMessage("§cВаша команда проиграла! §4(-" + eloChange + " ELO)");
        });

        winners.forEach(p -> {
            p.sendTitle("§6ПОБЕДА!", "§7Ваша команда победила!", 10, 40, 10);
            SoundUtil.playVictory(p);
            EffectUtil.spawnVictoryFireworks(p);
        });

        losers.forEach(p -> {
            p.sendTitle("§cПОРАЖЕНИЕ", "§7Ваша команда проиграла.", 10, 40, 10);
            SoundUtil.playDefeat(p);
            EffectUtil.playDefeatParticles(p);
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupDuel(duel);
            }
        }.runTaskLater(plugin, 60L);
    }

    private void endDuelAsDraw(Duel duel) {
        if (duel.getState() == DuelState.ENDING) return;
        duel.setState(DuelState.ENDING);

        duel.getOnlineAlivePlayers().forEach(p -> p.sendTitle("§eНичья!", "§7Дуэль была остановлена.", 10, 40, 10));

        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupDuel(duel);
            }
        }.runTaskLater(plugin, 40L);
    }

    private void cleanupDuel(Duel duel) {
        duel.getAllParticipantUUIDs()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(this::restorePlayer);
        duel.getAllParticipantUUIDs().forEach(activeDuels::remove);
        duel.getArena().setState(ArenaState.AVAILABLE);
    }

    private void restorePlayer(Player player) {
        PlayerState state = playerStates.remove(player.getUniqueId());
        if (state != null && player.isOnline()) {
            player.setGameMode(state.getGameMode());
            player.setHealth(state.getHealth());
            player.setFoodLevel(state.getFoodLevel());
            player.setExp(state.getExp());
            player.setLevel(state.getLevel());
            player.teleport(state.getLocation());
            plugin.getPlayerListener().setLobbyState(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        Duel duel = activeDuels.get(loser.getUniqueId());

        if (duel != null && duel.getState() == DuelState.ACTIVE) {
            event.getDrops().clear();
            event.setDeathMessage(null);
            new BukkitRunnable() {
                @Override
                public void run() {
                    loser.spigot().respawn();
                }
            }.runTaskLater(plugin, 1L);

            duel.getAlivePlayers().remove(loser.getUniqueId());
            checkWinCondition(duel);
        }
    }

    private void checkWinCondition(Duel duel) {
        boolean team1HasPlayers = duel.getAlivePlayers().stream().anyMatch(uuid -> duel.getTeam1().contains(uuid));
        boolean team2HasPlayers = duel.getAlivePlayers().stream().anyMatch(uuid -> duel.getTeam2().contains(uuid));

        if (!team1HasPlayers && team2HasPlayers) {
            endDuel(duel, 2);
        } else if (team1HasPlayers && !team2HasPlayers) {
            endDuel(duel, 1);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player loser = event.getPlayer();
        Duel duel = activeDuels.get(loser.getUniqueId());
        if (duel != null && duel.getState() != DuelState.ENDING) {
            duel.getAlivePlayers().remove(loser.getUniqueId());
            checkWinCondition(duel);
        }
    }

    public void forceStopDuel(Player player, @Nullable String admin) {
        Duel duel = activeDuels.get(player.getUniqueId());
        if (duel == null) return;

        String adminMessage = (admin != null) ? " администратором " + admin : "";
        duel.getOnlineAlivePlayers().forEach(p -> p.sendMessage("§cВаша дуэль была принудительно остановлена" + adminMessage + "."));

        endDuelAsDraw(duel);
    }

    public boolean isPlayerInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }
}