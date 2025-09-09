package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.*;
import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class DuelManager implements Listener {
    private final XorealisDuels plugin;
    private final Map<UUID, Duel> activeDuels=new HashMap<>();
    private final Map<UUID, PlayerState> playerStates=new HashMap<>();

    public void startDuel(Player player1, Player player2, Arena arena, Kit kit) {
        Duel duel = new Duel(arena,kit,player1,player2);

        arena.setState(ArenaState.IN_USE);

        preparePlayer(player1, arena.getSpawn1());
        preparePlayer(player2,arena.getSpawn2());

        activeDuels.put(player1.getUniqueId(), duel);
        activeDuels.put(player2.getUniqueId(), duel);

        startCountdown(duel);
    }

    private void preparePlayer(Player player, Location spawnPoint) {
        PlayerState state = new PlayerState(
                player.getLocation(),
                player.getInventory().getContents(),
                player.getInventory().getArmorContents(),
                player.getGameMode(),
                player.getHealth(),
                player.getFoodLevel(),
                player.getExp(),
                player.getLevel()
        );
        playerStates.put(player.getUniqueId(), state);

        player.getInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(spawnPoint);
    }
    private void startCountdown(Duel duel) {
        new BukkitRunnable() {
            private int countdown = 5;

            @Override
            public void run() {
                if(countdown>0){
                    duel.getPlayer1().sendTitle("§a+"+ countdown,"",5,10,5);
                    duel.getPlayer2().sendTitle("§a"+countdown,"",5,10,5);
                }else {
                    this.cancel();
                    duel.setState(DuelState.ACTIVE);
                    duel.getPlayer1().sendTitle("§cБой!", "",5,10,5);
                    duel.getPlayer2().sendTitle("§cБой!", "",5,10,5);

                    duel.getPlayer1().getInventory().setContents(duel.getKit().getInventoryContents());
                    duel.getPlayer1().getInventory().setContents(duel.getKit().getArmorContents());

                    duel.getPlayer2().getInventory().setContents(duel.getKit().getInventoryContents());
                    duel.getPlayer2().getInventory().setArmorContents(duel.getKit().getArmorContents());
                }
            }
        }.runTaskTimer(plugin,0L,20L);
    }
    public void endDuel(Duel duel,Player winner, Player loser) {
        if(duel.getState()==DuelState.ENDING) return;

        duel.setState(DuelState.ENDING);

        winner.sendMessage("§a Вы победили в дуэли против "+loser.getName()+"!");
        loser.sendMessage("§cВы проиграли дуэль против "+winner.getName()+"!");

        new BukkitRunnable() {
            @Override
            public void run() {
                restorePlayer(winner);
                restorePlayer(loser);

                activeDuels.remove(winner.getUniqueId());
                activeDuels.remove(loser.getUniqueId());

                duel.getArena().setState(ArenaState.AVAILABLE);
            }
        }.runTaskLater(plugin,60L);
    }

    public void restorePlayer(Player player) {
        PlayerState state = playerStates.remove(player.getUniqueId());
        if(state != null && player.isOnline()){
            player.getInventory().clear();
            player.getInventory().setContents(state.getInventoryContents());
            player.getInventory().setArmorContents(state.getArmorContents());
            player.setGameMode(state.getGameMode());
            player.setHealth(state.getHealth());
            player.setFoodLevel(state.getFoodLevel());
            player.setExp(state.getExp());
            player.setLevel(state.getLevel());
            player.teleport(player.getLocation());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        Duel duel = activeDuels.get(loser.getUniqueId());

        if(duel!=null && duel.getState()==DuelState.ACTIVE){
            event.getDrops().clear();
            Player winner = duel.getOpponent(loser);
            endDuel(duel, winner, loser);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player loser = event.getPlayer();
        Duel duel = activeDuels.get(loser.getUniqueId());

        if(duel!=null&&duel.getState()!=DuelState.ENDING){
            Player winner = duel.getOpponent(loser);
            endDuel(duel,winner,loser);
        }
    }
}
