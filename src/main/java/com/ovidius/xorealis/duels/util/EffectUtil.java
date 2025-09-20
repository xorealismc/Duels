package com.ovidius.xorealis.duels.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;


public final class EffectUtil {


    private EffectUtil() {}

    /**
     * Запускает на месте игрока красивый победный фейерверк.
     */
    public static void spawnVictoryFireworks(Player winner) {
        Location loc = winner.getLocation();
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(1);
        fwm.addEffect(FireworkEffect.builder()
                .withColor(Color.LIME, Color.YELLOW)
                .withFade(Color.WHITE)
                .flicker(true)
                .trail(true)
                .build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    /**
     * Создает над проигравшим игроком облачко "злых" частиц.
     */
    public static void playDefeatParticles(Player loser) {
        Location loc = loser.getLocation().add(0, 1.5, 0);

        loser.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, loc, 20, 0.5, 0.5, 0.5, 0);
    }
}