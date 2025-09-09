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
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public class MenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();


        if (title.equals(MenuManager.MAIN_MENU_TITLE)) {
            e.setCancelled(true);
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int slot = e.getSlot();
            switch (slot) {
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
            return;
        }
        if(title.equals(ChatColor.DARK_GRAY+"Выберите кит")){
            e.setCancelled(true);
            ItemStack clickedItem = e.getCurrentItem();
            if(clickedItem == null || clickedItem.getType() == Material.AIR) return;

            String kitDisplayName=clickedItem.getItemMeta().getDisplayName();
            XorealisDuels.getInstance().getKitManager().getAllKitTemplates().stream()
                    .filter(kit -> kit.getDisplayName().equals(kitDisplayName))
                    .findFirst()
                    .ifPresent(kit -> {
                        if(e.isLeftClick()){
                            XorealisDuels.getInstance().getQueueManager().addPlayerToQueue(player, kit);
                            player.closeInventory();
                        }
                        else if(e.isRightClick()){
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

                String kitDisplayName = title.substring(title.lastIndexOf(" ") + 1);
                Optional<Kit> optionalKit = XorealisDuels.getInstance().getKitManager().getAllKitTemplates().stream()
                        .filter(k -> k.getDisplayName().equals(kitDisplayName))
                        .findFirst();

                if (optionalKit.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Ошибка: не удалось найти кит для сохранения!");
                    player.closeInventory();
                    return;
                }
                Kit kit = optionalKit.get();

                switch(clickedItem.getType()) {
                    case EMERALD_BLOCK:
                        ItemStack[] inventoryToSave = e.getView().getTopInventory().getContents();
                        inventoryToSave = Arrays.copyOfRange(inventoryToSave, 0, 36);

                        ItemStack[] armorToSave = player.getInventory().getArmorContents();
                        PlayerKitLayout layout = new PlayerKitLayout(inventoryToSave, armorToSave);

                        XorealisDuels.getInstance().getPlayerDataManager().savePlayerLayout(player.getUniqueId(), kit.getId(), layout);

                        player.sendMessage(ChatColor.GREEN + "Раскладка сохранена!");
                        player.closeInventory();
                        break;

                    case TNT:
                        XorealisDuels.getInstance().getPlayerDataManager().deletePlayerLayout(player.getUniqueId(), kit.getId());
                        player.sendMessage(ChatColor.YELLOW + "Раскладка сброшена до стандартной!");
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
