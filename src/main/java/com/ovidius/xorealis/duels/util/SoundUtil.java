package com.ovidius.xorealis.duels.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

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
    public static void spawnVictoryFireworks(Player winner) {
        Location loc = winner.getLocation();
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(1);
        fwm.addEffect(FireworkEffect.builder()
                .withColor(Color.LIME)
                .withColor(Color.YELLOW)
                .flicker(true)
                .trail(true)
                .build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    public static void playDefeatParticles(Player loser) {
        Location loc = loser.getLocation().add(0, 1, 0);
    }

}
