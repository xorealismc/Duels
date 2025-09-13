package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlayerListener implements Listener {

    private final XorealisDuels plugin;
    private final Set<UUID> playersInLobby = new HashSet<>();

    public PlayerListener(XorealisDuels plugin) {
        this.plugin = plugin;
    }

    public void setLobbyState(Player player) {
        playersInLobby.add(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.getActivePotionEffects().clear();

        giveLobbyItems(player);
    }

    public void removeLobbyState(Player player) {
        playersInLobby.remove(player.getUniqueId());
    }

    private void giveLobbyItems(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();

        if (compassMeta == null) return;

        compassMeta.setDisplayName(ChatColor.GREEN + "Меню Дуэлей" + ChatColor.GRAY + "(ПКМ)");
        compassMeta.setLore(List.of(
                ChatColor.GRAY + "Нажмите что бы открыть меню",
                ChatColor.GRAY + "выбора режимов дуэлей."
        ));
        compass.setItemMeta(compassMeta);
        player.getInventory().setItem(0, compass);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setLobbyState(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersInLobby.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (playersInLobby.contains(player.getUniqueId()) &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            if (isDuelCompass(event.getItem())) {
                event.setCancelled(true);
                plugin.getMenuManager().openMainMenu(player);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (playersInLobby.contains(event.getPlayer().getUniqueId()) &&
                isDuelCompass(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassMoveInInventory(InventoryClickEvent event) {
        if (!playersInLobby.contains(event.getWhoClicked().getUniqueId())) return;

        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (isDuelCompass(event.getCurrentItem())) {
                event.setCancelled(true);
            }
        }
        if (isDuelCompass(event.getCursor())) {
            event.setCancelled(true);
        }

    }


    private boolean isDuelCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) {
            return false;
        }
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).startsWith("Меню Дуэлей");
    }
}
