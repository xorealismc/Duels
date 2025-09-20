package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Arena;
import com.ovidius.xorealis.duels.object.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.NumberConversions;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ArenaManager {
    private final XorealisDuels plugin;
    private final List<Arena> arenas = new ArrayList<>();

    public ArenaManager(XorealisDuels plugin) {
        this.plugin = plugin;
    }

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
            plugin.getLogger().log(Level.SEVERE, "Critical Error: Duel world ''{0}'' not found! No arenas will be loaded.", worldName);
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) {
            plugin.getLogger().warning("The 'arenas' section was not found in arenas.yml.");
            return;
        }

        for (String arenaId : section.getKeys(false)) {
            try {
                String displayName = ChatColor.translateAlternateColorCodes('&', section.getString(arenaId + ".display-name", arenaId));
                List<Location> spawns1 = parseLocationList(world, section.getMapList(arenaId + ".team-1-spawns"));
                List<Location> spawns2 = parseLocationList(world, section.getMapList(arenaId + ".team-2-spawns"));

                if (spawns1.isEmpty() || spawns2.isEmpty()) {
                    plugin.getLogger().warning("Arena '" + arenaId + "' has no spawn points and was skipped.");
                    continue;
                }

                Arena arena = new Arena(arenaId, displayName, spawns1, spawns2);
                arenas.add(arena);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading arena with ID: " + arenaId, e);
            }
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }

    private List<Location> parseLocationList(World world, List<Map<?, ?>> rawList) {
        List<Location> locations = new ArrayList<>();
        if (rawList == null) return locations;

        for (Map<?, ?> map : rawList) {
            try {
                double x = NumberConversions.toDouble(map.get("x"));
                double y = NumberConversions.toDouble(map.get("y"));
                double z = NumberConversions.toDouble(map.get("z"));
                Object rawYaw = map.get("yaw");
                Object rawPitch = map.get("pitch");

                float yaw = (rawYaw != null) ? NumberConversions.toFloat(rawYaw) : 0.0f;
                float pitch = (rawPitch != null) ? NumberConversions.toFloat(rawPitch) : 0.0f;

                locations.add(new Location(world, x, y, z, yaw, pitch));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse a spawn point location: " + e.getMessage());
            }
        }
        return locations;
    }

    public Optional<Arena> findAvailableArena(int requiredSpawnsPerTeam) {
        List<Arena> shuffledArenas = new ArrayList<>(arenas);
        Collections.shuffle(shuffledArenas);
        return shuffledArenas.stream()
                .filter(arena -> arena.getState() == ArenaState.AVAILABLE)
                .filter(arena -> arena.getTeam1Spawns().size() >= requiredSpawnsPerTeam && arena.getTeam2Spawns().size() >= requiredSpawnsPerTeam)
                .findFirst();
    }

    public List<Arena> getAllArenas() {
        return Collections.unmodifiableList(arenas);
    }
}