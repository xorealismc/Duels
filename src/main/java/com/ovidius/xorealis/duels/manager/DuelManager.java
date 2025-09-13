package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.listeners.PlayerListener;
import com.ovidius.xorealis.duels.object.*;
import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class DuelManager implements Listener {
    private final XorealisDuels plugin;

    private final Map<UUID, Duel> activeDuels = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final Map<Duel, BukkitTask> countdownTasks = new ConcurrentHashMap<>();

    public void startDuel(Player player1, Player player2, Arena arena, Kit kit) {

        plugin.getPlayerListener().removeLobbyState(player1);
        plugin.getPlayerListener().removeLobbyState(player2);

        Duel duel = new Duel(arena, kit, player1, player2);
        arena.setState(ArenaState.IN_USE);

        preparePlayer(player1, arena.getSpawn1());
        preparePlayer(player2, arena.getSpawn2());

        activeDuels.put(player1.getUniqueId(), duel);
        activeDuels.put(player2.getUniqueId(), duel);

        startCountdown(duel);
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
        BukkitTask task = new BukkitRunnable() {
            private int countdown = 5;
            @Override
            public void run() {
                if (duel.getState() != DuelState.STARTING) {this.cancel();return;}

                if (countdown > 0) {
                    duel.getPlayer1().sendTitle("§a" + countdown, "", 5, 20, 5);
                    duel.getPlayer2().sendTitle("§a" + countdown, "", 5, 20, 5);
                    countdown--;
                } else {
                    this.cancel();
                    duel.setState(DuelState.ACTIVE);
                    duel.getPlayer1().sendTitle("§cБой!", "", 5, 15, 5);
                    duel.getPlayer2().sendTitle("§cБой!", "", 5, 15, 5);

                    applyKits(duel);
                    countdownTasks.remove(duel);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        countdownTasks.put(duel, task);
    }

    private void applyKits(Duel duel) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        Kit kit = duel.getKit();

        playerDataManager.loadPlayerLayout(duel.getPlayer1().getUniqueId(), kit.getId())
                .ifPresentOrElse
                        (layout -> {
                                    duel.getPlayer1().getInventory().setContents(layout.inventoryContents());
                                    duel.getPlayer1().getInventory().setArmorContents(layout.armorContents());
                                },
                                () -> {
                                    duel.getPlayer1().getInventory().setContents(kit.getInventoryContents());
                                    duel.getPlayer1().getInventory().setArmorContents(kit.getArmorContents());
                                });
        playerDataManager.loadPlayerLayout(duel.getPlayer2().getUniqueId(), kit.getId())
                .ifPresentOrElse
                        (layout -> {
                                    duel.getPlayer2().getInventory().setContents(layout.inventoryContents());
                                    duel.getPlayer2().getInventory().setArmorContents(layout.armorContents());
                                },
                                () -> {
                                    duel.getPlayer2().getInventory().setContents(kit.getInventoryContents());
                                    duel.getPlayer2().getInventory().setArmorContents(kit.getArmorContents());
                                });
    }

    public void endDuel(Duel duel, Player winner, Player loser) {
        if (duel.getState() == DuelState.ENDING) return;
        duel.setState(DuelState.ENDING);

        winner.sendMessage("§a Вы победили в дуэли против " + loser.getName() + "!");
        loser.sendMessage("§cВы проиграли дуэль против " + winner.getName() + "!");

        new BukkitRunnable() {
            @Override
            public void run() {
                restorePlayer(winner);
                restorePlayer(loser);

                activeDuels.remove(winner.getUniqueId());
                activeDuels.remove(loser.getUniqueId());

                duel.getArena().setState(ArenaState.AVAILABLE);
            }
        }.runTaskLater(plugin, 60L);
    }

    public void restorePlayer(Player player) {
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

            Player winner = duel.getOpponent(loser);
            if(winner != null) {
                endDuel(duel, winner, loser);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player loser = event.getPlayer();
        Duel duel = activeDuels.get(loser.getUniqueId());

        if (duel != null && duel.getState() != DuelState.ENDING) {
            Player winner = duel.getOpponent(loser);
            endDuel(duel, winner, loser);
        }
    }

    public boolean isPlayerInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }
}
