package com.ovidius.xorealis.duels.listeners;

import com.ovidius.xorealis.duels.XorealisDuels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();

        compassMeta.setDisplayName(ChatColor.GREEN +"Меню Дуэлей"+ChatColor.GRAY+"(ПКМ)");
        compassMeta.setLore(List.of(
                ChatColor.GRAY+"Нажмите что бы открыть меню",
                ChatColor.GRAY+"выбора режимов дуэлей."
        ));
        compass.setItemMeta(compassMeta);
        player.getInventory().setItem(0, compass);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemHand = player.getInventory().getItemInMainHand();

            if(itemHand != null && itemHand.getType() == Material.COMPASS && itemHand.hasItemMeta() && itemHand.hasItemMeta() && itemHand.getItemMeta().hasDisplayName()) {
                event.setCancelled(true);
                XorealisDuels.getInstance().getMenuManager().openMainMenu(player);
            }
        }
    }
}
