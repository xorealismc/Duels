package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Arena;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@AllArgsConstructor
public class ArenaManager {
    private final XorealisDuels plugin;
    private final List<Arena> arenas = new ArrayList<>();


    public void loadArenas() {
        arenas.clear();
        File arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            plugin.saveResource("arenas.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(arenasFile);


        String worldName = config.getString("duel_world", "world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            plugin.getLogger().severe("=============================================");
            plugin.getLogger().severe("КРИТИЧЕСКАЯ ОШИБКА: Мир для дуэлей '" + worldName + "' не найден!");
            plugin.getLogger().severe("Ни одна арена не будет загружена.");
            plugin.getLogger().severe("=============================================");
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) {
            plugin.getLogger().warning("Секция 'arenas' не найдена в arenas.yml!");
            return;
        }

        for (String arenaId : section.getKeys(false)) {
            try {
                String displayName = ChatColor.translateAlternateColorCodes('&', section.getString(arenaId + ".display-name", arenaId));
                Location spawn1 = parseLocation(world, section.getConfigurationSection(arenaId + ".spawn-1"));
                Location spawn2 = parseLocation(world, section.getConfigurationSection(arenaId + ".spawn-2"));

                Arena arena = new Arena(arenaId, displayName, spawn1, spawn2);
                arenas.add(arena);
                plugin.getLogger().info("Успешно загружена арена: " + arenaId + " в мире " + worldName);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка при загрузке арены с ID: " + arenaId, e);
            }
        }

        if(arenas.isEmpty()) {
            plugin.getLogger().warning("Внимание: Ни одной арены не было найдено в файле arenas.yml");
        } else {
            plugin.getLogger().info("Загружено " + arenas.size() + " арен.");
        }
    }
    private Location parseLocation(World world, ConfigurationSection section) {
        if(section == null) throw new IllegalArgumentException("Arena configuration is missing section");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw= (float) section.getDouble("yaw", 0.0);
        float pitch= (float) section.getDouble("pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Optional<Arena> findAvailableArena(){
        List<Arena> shuffledArenas = new ArrayList<>(arenas);
        Collections.shuffle(shuffledArenas);

        return shuffledArenas.stream().filter(arena->arena.getState()==
                com.ovidius.xorealis.duels.object.ArenaState.AVAILABLE).
                findFirst();
    }
    public List<Arena> getAllArenas(){
        return arenas;
    }
}
