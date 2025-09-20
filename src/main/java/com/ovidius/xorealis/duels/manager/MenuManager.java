package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.GameModeType;
import com.ovidius.xorealis.duels.object.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;

public class MenuManager {

    public static final String MAIN_MENU_TITLE = "Выбор режима";
    public static final String KIT_SELECTOR_TITLE = "Выберите кит";

    private static final int MAIN_MENU_SIZE = 27;

    public static final int SLOT_ONE_VS_ONE = 11;
    public static final int SLOT_TWO_VS_TWO = 13;
    public static final int SLOT_THREE_VS_THREE = 15;

    public MenuManager() {

    }

    public void openKitSelectorMenu(Player player, GameModeType mode) {
        Collection<Kit> allKits = XorealisDuels.getInstance().getKitManager().getAllKitTemplates();
        int size = ((allKits.size() + 8) / 9) * 9;
        size = Math.max(9, size);
        String title = ChatColor.DARK_GRAY + KIT_SELECTOR_TITLE + " (" + mode.name() + ")";
        boolean isInParty = XorealisDuels.getInstance().getPartyManager().getParty(player).isPresent();

        Inventory kitSelectorMenu = Bukkit.createInventory(null, size, title);
        for (Kit kit : allKits) {
            ItemStack icon = kit.getIcon().clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(kit.getDisplayName());
                if (isInParty) {
                    meta.setLore(List.of(
                            "",
                            "§a▶ ЛКМ - Начать дуэль с напарником",
                            "§b▶ ПКМ - Настроить раскладку"
                    ));
                } else {
                    meta.setLore(List.of(
                            "",
                            "§a▶ ЛКМ - Начать поиск случайной игры",
                            "§b▶ ПКМ - Настроить раскладку"
                    ));

                }
                icon.setItemMeta(meta);
            }
            kitSelectorMenu.addItem(icon);
        }
        player.openInventory(kitSelectorMenu);
    }

    public void openMainMenu(Player player) {
        Inventory mainMenu = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + MAIN_MENU_TITLE);
        mainMenu.setItem(SLOT_ONE_VS_ONE,
                createMenuItem(Material.IRON_SWORD, ChatColor.WHITE + "Дуэли 1v1",
                        List.of(ChatColor.GRAY + "Нажмите для поиска одиночной дуэли.")));
        mainMenu.setItem(SLOT_TWO_VS_TWO,
                createMenuItem(Material.GOLDEN_SWORD, ChatColor.YELLOW + "Дуэли 2v2",
                        List.of(ChatColor.GRAY + "Нажмите для поиска парной дуэли.")));

        mainMenu.setItem(SLOT_THREE_VS_THREE,
                createMenuItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Дуэли 3v3",
                        List.of(ChatColor.GRAY + "Нажмите для поиска командной дуэли.")));

        player.openInventory(mainMenu);
    }

    public void openKitEditor(Player player, Kit kit) {
        XorealisDuels.getInstance().getPlayerListener().enterEditMode(player, kit);
    }

    public static ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
