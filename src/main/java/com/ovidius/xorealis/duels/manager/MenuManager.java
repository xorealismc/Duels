package com.ovidius.xorealis.duels.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MenuManager {
    public MenuManager() {

    }

    public void openMainMenu(Player player){
        Inventory mainMenu = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Выбор режима");

        player.openInventory(mainMenu);
    }
}
