package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import org.bukkit.ChatColor;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final Map<UUID, ItemStack[]> playerInventoryCache = new HashMap<>();
    private final Map<UUID, ItemStack[]> playerArmorCache = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();

        compassMeta.setDisplayName(ChatColor.GREEN +"Меню Дуэлей"+ChatColor.GRAY+"(ПКМ)");
        compassMeta.setLore(List.of(
                ChatColor.GRAY+"Нажмите что бы открыть меню",
                ChatColor.GRAY+"выбора режимов дуэлей."
        ));
        compass.setItemMeta(compassMeta);
        player.getInventory().setItem(0, compass);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemHand = player.getInventory().getItemInMainHand();

            if(itemHand != null && itemHand.getType() == Material.COMPASS && itemHand.hasItemMeta() && itemHand.hasItemMeta() && itemHand.getItemMeta().hasDisplayName()) {
                event.setCancelled(true);
                XorealisDuels.getInstance().getMenuManager().openMainMenu(player);
            }
        }
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (isDuelCompass(droppedItem)) {
            event.setCancelled(true); // Отменяем выбрасывание
            event.getPlayer().sendMessage(ChatColor.RED + "Вы не можете выбросить этот предмет.");
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassMove(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (isDuelCompass(item) || isDuelCompass(cursor)) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "Вы не можете переместить этот предмет.");
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (event.getView().getTitle().startsWith(ChatColor.DARK_GRAY + "Редактор: ")) {
            playerInventoryCache.put(player.getUniqueId(), player.getInventory().getContents());
            playerArmorCache.put(player.getUniqueId(), player.getInventory().getArmorContents());
            player.getInventory().clear();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (event.getView().getTitle().startsWith(ChatColor.DARK_GRAY + "Редактор: ")) {
            restorePlayerInventory(player);
            player.sendMessage(ChatColor.GRAY + "Вы вышли из редактора.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        restorePlayerInventory(event.getPlayer());
    }

    private void restorePlayerInventory(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerInventoryCache.containsKey(uuid) && playerArmorCache.containsKey(uuid)) {
            player.getInventory().clear();
            player.getInventory().setContents(playerInventoryCache.get(uuid));
            player.getInventory().setArmorContents(playerArmorCache.get(uuid));
            playerInventoryCache.remove(uuid);
            playerArmorCache.remove(uuid);
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
