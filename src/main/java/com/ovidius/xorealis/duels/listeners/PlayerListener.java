package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.manager.MenuManager;
import com.ovidius.xorealis.duels.object.Kit;
import com.ovidius.xorealis.duels.object.PlayerKitLayout;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerListener implements Listener {

    private final XorealisDuels plugin;
    private final Set<UUID> playersInLobby = new HashSet<>();
    private final Map<UUID, ItemStack[]> inventoryCache = new HashMap<>();
    private final Map<UUID, ItemStack[]> armorCache = new HashMap<>();
    private final Map<UUID, String> editingKit = new HashMap<>();
    private final List<Material> editorControlItems = List.of(Material.EMERALD_BLOCK, Material.TNT, Material.REDSTONE_BLOCK);

    public PlayerListener(XorealisDuels plugin) {
        this.plugin = plugin;
    }

    public void setLobbyState(Player player) {
        playersInLobby.add(player.getUniqueId());
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setExp(0);
        player.setLevel(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        giveLobbyItems(player);
    }

    public void removeLobbyState(Player player) {
        playersInLobby.remove(player.getUniqueId());
    }

    public void enterEditMode(Player player, Kit kit) {
        player.closeInventory();
        removeLobbyState(player);
        inventoryCache.put(player.getUniqueId(), player.getInventory().getContents());
        armorCache.put(player.getUniqueId(), player.getInventory().getArmorContents());
        editingKit.put(player.getUniqueId(), kit.getId());
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();

        plugin.getPlayerDataManager().loadPlayerLayout(player.getUniqueId(), kit.getId())
                .ifPresentOrElse(layout -> {
                    player.getInventory().setContents(layout.inventoryContents());
                    player.getInventory().setArmorContents(layout.armorContents());
                }, () -> {
                    player.getInventory().setContents(kit.getInventoryContents());
                    player.getInventory().setArmorContents(kit.getArmorContents());
                });

        giveEditorControls(player);
        player.sendMessage("§aYou have entered edit mode. Right-click the items in your hotbar.");
    }

    public void exitEditMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (inventoryCache.containsKey(uuid)) {
            player.getInventory().clear();
            player.getInventory().setContents(inventoryCache.get(uuid));
            player.getInventory().setArmorContents(armorCache.get(uuid));
            inventoryCache.remove(uuid);
            armorCache.remove(uuid);
            editingKit.remove(uuid);
        }
        setLobbyState(player);
    }

    public boolean isEditing(Player player) {
        return editingKit.containsKey(player.getUniqueId());
    }

    private void giveLobbyItems(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName("§aМеню Дуэлей §7(ПКМ)");
        meta.setLore(List.of("§7Нажмите, чтобы открыть меню."));
        compass.setItemMeta(meta);
        player.getInventory().setItem(0, compass);
    }

    private void giveEditorControls(Player player) {
        player.getInventory().setItem(8, MenuManager.createMenuItem(Material.EMERALD_BLOCK, "§aSave & Exit", List.of("§7Saves the current layout.")));
        player.getInventory().setItem(7, MenuManager.createMenuItem(Material.TNT, "§cReset", List.of("§7Resets to the default layout.")));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setLobbyState(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if(isEditing(player)) {
            exitEditMode(player);
        }
        playersInLobby.remove(player.getUniqueId());
        plugin.getQueueManager().removeAllQueues(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if(isEditing(player) || playersInLobby.contains(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (isEditing(player)) {
            handleEditorInteract(e);
        }
        else if (playersInLobby.contains(player.getUniqueId())) {
            handleLobbyInteract(e);
        }
    }

    private void handleLobbyInteract(PlayerInteractEvent e) {
        if (isDuelCompass(e.getItem())) {
            e.setCancelled(true);
            plugin.getMenuManager().openMainMenu(e.getPlayer());
        }
    }

    private void handleEditorInteract(PlayerInteractEvent e) {
        e.setCancelled(true);
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if(item == null || !editorControlItems.contains(item.getType())) return;

        String kitId = editingKit.get(player.getUniqueId());
        if(kitId == null) {
            exitEditMode(player);
            return;
        }

        switch (item.getType()) {
            case EMERALD_BLOCK:
                handleSaveKit(player, kitId);
                break;
            case TNT:
                handleResetKit(player, kitId);
                break;
        }
    }

    private void handleSaveKit(Player player, String kitId) {
        player.getInventory().setItem(7, null);
        player.getInventory().setItem(8, null);

        ItemStack[] inv = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        PlayerKitLayout layout = new PlayerKitLayout(inv, armor);

        plugin.getPlayerDataManager().savePlayerLayout(player.getUniqueId(), kitId, layout);
        player.sendMessage("§aLayout saved!");
        exitEditMode(player);
    }

    private void handleResetKit(Player player, String kitId) {
        plugin.getPlayerDataManager().deletePlayerLayout(player.getUniqueId(), kitId);
        player.sendMessage("§eLayout reset!");
        exitEditMode(player);

        plugin.getKitManager().getKitTemplate(kitId).ifPresent(kit -> enterEditMode(player, kit));
    }

    @EventHandler
    public void onEditorInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (isEditing(player)) {
            ItemStack currentItem = e.getCurrentItem();
            if (currentItem != null && editorControlItems.contains(currentItem.getType())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLobbyInventoryInteract(InventoryClickEvent e) {
        if (!playersInLobby.contains(e.getWhoClicked().getUniqueId())) return;
        if (e.getView().getTopInventory().getHolder() == null) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeathInLobby(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (playersInLobby.contains(player.getUniqueId())) {
            e.getDrops().clear();
            e.setDeathMessage(null);
            e.setKeepInventory(true);
            e.setKeepLevel(true);
        }
    }

    @EventHandler
    public void onPlayerRespawnInLobby(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (playersInLobby.contains(player.getUniqueId())) {
            new BukkitRunnable() {
                @Override
                public void run() { setLobbyState(player); }
            }.runTaskLater(plugin, 1L);
        }
    }

    private boolean isDuelCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        return ChatColor.stripColor(meta.getDisplayName()).startsWith("Меню Дуэлей");
    }
}