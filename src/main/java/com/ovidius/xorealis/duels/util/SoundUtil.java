package com.ovidius.xorealis.duels.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    public static void playSuccessClick(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7F, 1.5F);
    }

    public static void playErrorClick(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.5F);
    }

    public static void playCountdownTick(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.2F);
    }

    public static void playDuelStart(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
    }

}
