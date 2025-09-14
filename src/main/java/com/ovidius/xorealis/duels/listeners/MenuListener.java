package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.manager.MenuManager;
import com.ovidius.xorealis.duels.object.Kit;
import com.ovidius.xorealis.duels.object.PlayerKitLayout;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public class MenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getHolder() != null) {
            return;
        }
        String cleanTitle = ChatColor.stripColor(e.getView().getTitle()).trim();
        if (cleanTitle.equals(MenuManager.MAIN_MENU_TITLE)) {
            handleMainMenuClick(e);
        } else if (cleanTitle.equals(MenuManager.KIT_SELECTOR_TITLE)) {
            handleKitSelectorClick(e);
        }
    }

    private void handleMainMenuClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        switch (e.getSlot()) {
            case MenuManager.SLOT_ONE_VS_ONE:
                XorealisDuels.getInstance().getMenuManager().openKitSelectorMenu(player);
                break;
            case MenuManager.SLOT_TWO_VS_TWO:
                player.sendMessage(ChatColor.YELLOW + "Режим 2v2 еще в разработке!");
                player.closeInventory();
                break;
            case MenuManager.SLOT_THREE_VS_THREE:
                player.sendMessage(ChatColor.AQUA + "Режим 3v3 еще в разработке!");
                player.closeInventory();
                break;
        }
    }

    private void handleKitSelectorClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String kitDisplayName = clickedItem.getItemMeta().getDisplayName();
        XorealisDuels.getInstance().getKitManager().getAllKitTemplates().stream()
                .filter(kit -> kit.getDisplayName().equals(kitDisplayName))
                .findFirst()
                .ifPresent(kit -> {
                    if (e.isLeftClick()) {
                        XorealisDuels.getInstance().getQueueManager().addPlayerToQueue(player, kit);
                        player.closeInventory();
                    } else if (e.isRightClick()) {
                        XorealisDuels.getInstance().getPlayerListener().enterEditMode(player, kit);
                    }
                });
    }
}




