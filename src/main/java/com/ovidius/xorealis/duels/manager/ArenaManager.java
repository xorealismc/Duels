package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Arena;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
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
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) {
            plugin.getLogger().warning("Arenas configuration not found");
            return;
        }
        for (String arenaId : section.getKeys(false)) {
            try {
                String worldName = section.getString(arenaId + ".world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().severe("World '" + worldName + "' for arena '" + arenaId + "' not found");
                    continue;
                }
                Location spawn1 = parseLocation(
                        world,
                        section.getConfigurationSection(arenaId + ".spawn-1"));
                Location spawn2 = parseLocation(
                        world,
                        section.getConfigurationSection(arenaId + ".spawn-2"));
                Arena arena = new Arena(worldName, spawn1, spawn2);
                arenas.add(arena);
                plugin.getLogger().info("Loaded arena '" + arenaId + "'");

            }catch (Exception e){
                plugin.getLogger().log(Level.SEVERE, "Error loading arena '" + arenaId + "'", e);
            }
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
