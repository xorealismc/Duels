package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.manager.MenuManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();


        if (title.equals(ChatColor.DARK_GRAY + "Выберите кит")) {
            e.setCancelled(true);
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            String kitDisplayName = clickedItem.getItemMeta().getDisplayName();
            XorealisDuels.getInstance().getKitManager().getAllKitTemplates().stream()
                    .filter(kit -> kit.getDisplayName().equals(kitDisplayName))
                    .findFirst()
                    .ifPresent(kit -> {
                        if (e.isLeftClick()) {
                            player.sendMessage(ChatColor.GREEN + "Вы начали поиск игры с китом: " + kit.getDisplayName());
                            player.closeInventory();
                        } else if (e.isRightClick()) {
                            XorealisDuels.getInstance().getMenuManager().openKitEditor(player, kit);
                        }
                    });
            return;
        }

        if (title.startsWith(ChatColor.DARK_GRAY + "Редактор: ")) {
            int slot = e.getRawSlot();

            if (slot >= 45 && slot <= 53) {
                e.setCancelled(true);

                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

                switch (clickedItem.getType()) {
                    case EMERALD_BLOCK:
                        player.sendMessage(ChatColor.GREEN + "Раскладка сохранена!");
                        player.closeInventory();
                        break;
                    case TNT:
                        player.sendMessage(ChatColor.YELLOW + "Раскладка сброшена!");
                        player.closeInventory();
                        break;
                    case REDSTONE_BLOCK:
                        player.closeInventory();
                        break;
                }
            }
            else if (e.getView().getTopInventory() != e.getClickedInventory() && e.isShiftClick()) {
                e.setCancelled(true);
            }

        }

    }
}
