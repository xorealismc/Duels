package com.ovidius.xorealis.duels.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;


public final class SoundUtil {


    private SoundUtil() {}

    /**
     * Звук успешного клика или действия.
     */
    public static void playSuccessClick(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.5f);
    }

    /**
     * Звук ошибки или неудачного действия.
     */
    public static void playErrorClick(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
    }

    /**
     * Звук тика таймера обратного отсчета.
     */
    public static void playCountdownTick(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
    }

    /**
     * Громкий, эпичный звук начала боя.
     */
    public static void playDuelStart(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
    }

    /**
     * Победный звук.
     */
    public static void playVictory(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /**
     * Звук поражения.
     */
    public static void playDefeat(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
}