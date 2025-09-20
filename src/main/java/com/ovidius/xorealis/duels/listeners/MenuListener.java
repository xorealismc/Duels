package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.manager.MenuManager;
import com.ovidius.xorealis.duels.object.GameModeType;
import com.ovidius.xorealis.duels.object.Kit;
import com.ovidius.xorealis.duels.util.SoundUtil;
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
        if (e.getView().getTopInventory().getHolder() != null) return;

        String cleanTitle = ChatColor.stripColor(e.getView().getTitle()).trim();

        if (cleanTitle.equalsIgnoreCase(MenuManager.MAIN_MENU_TITLE)) {
            handleMainMenuClick(e);
        } else if (cleanTitle.startsWith(MenuManager.KIT_SELECTOR_TITLE)) {
            handleKitSelectorClick(e);
        }
    }

    private void handleMainMenuClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        SoundUtil.playSuccessClick(player);

        switch (e.getSlot()) {
            case MenuManager.SLOT_ONE_VS_ONE:
                XorealisDuels.getInstance().getMenuManager().openKitSelectorMenu(player, GameModeType.SOLO);
                break;
            case MenuManager.SLOT_TWO_VS_TWO:
                XorealisDuels.getInstance().getMenuManager().openKitSelectorMenu(player, GameModeType.DUO);
                break;
            case MenuManager.SLOT_THREE_VS_THREE:
                XorealisDuels.getInstance().getMenuManager().openKitSelectorMenu(player, GameModeType.TRIO);
                break;
        }
    }

    private void handleKitSelectorClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        SoundUtil.playSuccessClick(player);

        String title = e.getView().getTitle();
        GameModeType mode;
        try {
            String modeString = title.substring(title.lastIndexOf("(") + 1, title.lastIndexOf(")"));
            mode = GameModeType.valueOf(modeString);
        } catch (Exception ex) {
            player.closeInventory();
            return;
        }

        String kitDisplayName = clickedItem.getItemMeta().getDisplayName();
        XorealisDuels.getInstance().getKitManager().getAllKitTemplates().stream()
                .filter(kit -> kit.getDisplayName().equals(kitDisplayName))
                .findFirst()
                .ifPresent(kit -> {
                    if (e.isRightClick()) {
                        XorealisDuels.getInstance().getPlayerListener().enterEditMode(player, kit);
                    } else if (e.isLeftClick()) {
                        XorealisDuels.getInstance().getPartyManager().attemptToQueue(player, kit, mode);
                    }
                });
    }
}