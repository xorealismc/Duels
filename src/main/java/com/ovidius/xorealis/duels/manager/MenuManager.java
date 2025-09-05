package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
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

    public static final String MAIN_MENU_TITLE=ChatColor.DARK_GRAY+"Выбор режима";
    private static final int MAIN_MENU_SIZE=27;

    public static final int SLOT_ONE_VS_ONE=11;
    public static final int SLOT_TWO_VS_TWO=13;
    public static final int SLOT_THREE_VS_THREE=15;

    public MenuManager() {

    }

    public void openKitSelectorMenu(Player player) {
        Collection<Kit> allKits = XorealisDuels.getInstance().getKitManager().getAllKitTemplates();
        int size = ((allKits.size() + 8) / 9) * 9;
        size = Math.max(9, size);
        Inventory kitSelectorMenu = Bukkit.createInventory(null, size, ChatColor.DARK_GRAY + "Выберите кит");
        for (Kit kit : allKits) {
            ItemStack icon = kit.getIcon().clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(kit.getDisplayName());
                meta.setLore(List.of(
                        "",
                        ChatColor.GREEN + "▶ ЛКМ - Начать поиск игры",
                        ChatColor.AQUA + "▶ ПКМ - Настроить раскладку"
                ));
                icon.setItemMeta(meta);
            }
            kitSelectorMenu.addItem(icon);
        }
        player.openInventory(kitSelectorMenu);
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
    public void openKitEditor(Player player, Kit kit) {
        Inventory editor = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Редактор: " + kit.getDisplayName());

        XorealisDuels.getInstance().getPlayerDataManager()
                .loadPlayerLayout(player.getUniqueId(), kit.getId())
                .ifPresentOrElse(
                        layout -> editor.setContents(layout.inventoryContents()),
                        () -> editor.setContents(kit.getInventoryContents())
                );

        ItemStack saveButton = createMenuItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Сохранить и выйти", List.of(ChatColor.GRAY + "Сохраняет текущую раскладку."));
        ItemStack resetButton = createMenuItem(Material.TNT, ChatColor.YELLOW + "Сбросить раскладку", List.of(ChatColor.GRAY + "Вернуть к стандартной расстановке."));
        ItemStack backButton = createMenuItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Выйти без сохранения", List.of(ChatColor.GRAY + "Изменения не будут сохранены."));
        ItemStack filler = createMenuItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);

        for(int i = 45; i < 54; i++) editor.setItem(i, filler);

        editor.setItem(48, resetButton);
        editor.setItem(49, saveButton);
        editor.setItem(50, backButton);

        player.openInventory(editor);
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
