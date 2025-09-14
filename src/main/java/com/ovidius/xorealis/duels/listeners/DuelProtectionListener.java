package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

@RequiredArgsConstructor
public class DuelProtectionListener implements Listener {
    private final XorealisDuels plugin;

    private final List<String> allowedCommands = List.of("ac","helpop","report");

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(isPlayerInDuel(event.getPlayer())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(isPlayerInDuel(event.getPlayer())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDropItemInDuel(PlayerDropItemEvent event) {
        if (isPlayerInDuel(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        if(isPlayerInDuel(player)){
            String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();
            if(!allowedCommands.contains(command)){
                event.setCancelled(true);
                player.sendMessage("§cВы не можете использовать эту команду во время дуэли.");
            }
        }
    }
    private boolean isPlayerInDuel(Player player){
        return plugin.getDuelManager().isPlayerInDuel(player);
    }
}
