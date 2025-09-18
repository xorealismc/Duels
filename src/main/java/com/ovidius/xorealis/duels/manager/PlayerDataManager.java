package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.PlayerData;
import com.ovidius.xorealis.duels.object.PlayerKitLayout;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataManager {

    private final XorealisDuels plugin;
    private final File playerDataFolder;

    static {
        ConfigurationSerialization.registerClass(PlayerData.class, "PlayerData");
        ConfigurationSerialization.registerClass(PlayerKitLayout.class, "PlayerKitLayout");
    }


    public PlayerDataManager(XorealisDuels plugin){
        this.plugin = plugin;
        playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if(!playerDataFolder.exists()){
            playerDataFolder.mkdirs();
        }
    }
    private File getPlayerFile(UUID uuid){
        return new File(playerDataFolder, uuid.toString() + ".yml");
    }

    private FileConfiguration getPlayerConfig(UUID uuid) {
        File playerFile = new File(playerDataFolder, uuid + ".yml");
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    private void savePlayerConfig(UUID uuid, FileConfiguration config) {
        try {
            config.save(new File(playerDataFolder, uuid + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить данные для игрока " + uuid, e);
        }
    }

    public PlayerData loadPlayerData(UUID uuid) {
        FileConfiguration config = getPlayerConfig(uuid);
        PlayerData data = config.getSerializable("stats", PlayerData.class);
        return data != null ? data : new PlayerData();
    }

    public void savePlayerData(UUID uuid, PlayerData data) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("stats", data);
        savePlayerConfig(uuid, config);
    }

    public void savePlayerLayout(UUID uuid, String kitId, PlayerKitLayout layout) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("layouts." + kitId, layout);
        savePlayerConfig(uuid, config);
    }

    public Optional<PlayerKitLayout> loadPlayerLayout(UUID uuid, String kitId) {
        return Optional.ofNullable(getPlayerConfig(uuid).getSerializable("layouts." + kitId, PlayerKitLayout.class));
    }

    public void deletePlayerLayout(UUID uuid, String kitId) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("layouts." + kitId, null);
        savePlayerConfig(uuid, config);
    }

}
