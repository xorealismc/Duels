package com.ovidius.xorealis.duels.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MenuManager {

    public static final String MAIN_MENU_TITLE=ChatColor.DARK_GRAY+"Выбор режима";
    private static final int MAIN_MENU_SIZE=27;

    public static final int SLOT_ONE_VS_ONE=11;
    public static final int SLOT_TWO_VS_TWO=13;
    public static final int SLOT_THREE_VS_THREE=15;

    public MenuManager() {

    }

    public void openMainMenu(Player player){
        Inventory mainMenu = Bukkit.createInventory(null, 27, MAIN_MENU_TITLE);

        mainMenu.setItem(SLOT_ONE_VS_ONE,
                createMenuItem(Material.IRON_SWORD, ChatColor.WHITE+"Дуэли 1v1",
                        List.of(ChatColor.GRAY+"Нажмите для поиска одиночной дуэли.")));
        mainMenu.setItem(SLOT_TWO_VS_TWO,
                createMenuItem(Material.GOLDEN_SWORD, ChatColor.YELLOW + "Дуэли 2v2",
                        List.of(ChatColor.GRAY + "Нажмите для поиска парной дуэли.")));

        mainMenu.setItem(SLOT_THREE_VS_THREE,
                createMenuItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Дуэли 3v3",
                        List.of(ChatColor.GRAY + "Нажмите для поиска командной дуэли.")));

        player.openInventory(mainMenu);
    }

    private ItemStack createMenuItem(Material material,String displayName, List<String> lore){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if(meta != null){
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
