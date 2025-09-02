package com.ovidius.xorealis.duels.listeners;

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

        if (!e.getView().getTitle().equals(MenuManager.MAIN_MENU_TITLE)) {
            return;
        }
        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        int slot = e.getRawSlot();
        switch (slot) {
            case MenuManager.SLOT_ONE_VS_ONE -> handleOneVsOne(player);
            case MenuManager.SLOT_TWO_VS_TWO -> handleTwoVsTwo(player);
            case MenuManager.SLOT_THREE_VS_THREE -> handleThreeVsThree(player);
            default -> {

            }
        }
    }
    private void handleOneVsOne(Player player) {
        player.sendMessage(ChatColor.GREEN + "Вы выбрали режим 1v1!");
        player.closeInventory();
    }

    private void handleTwoVsTwo(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Вы выбрали режим 2v2!");
        player.closeInventory();
    }

    private void handleThreeVsThree(Player player) {
        player.sendMessage(ChatColor.AQUA + "Вы выбрали режим 3v3!");
        player.closeInventory();
    }
}
