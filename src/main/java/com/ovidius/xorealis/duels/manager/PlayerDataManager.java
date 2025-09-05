package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.PlayerKitLayout;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

    public void savePlayerLayout(UUID uuid, String kitId, PlayerKitLayout layout){
        File playerFile = getPlayerFile(uuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        String path = "layouts."+kitId;
        config.set(path+".inventory-contents",layout.inventoryContents());
        config.set(path+".armor-contents",layout.armorContents());

        try{
            config.save(playerFile);
        }catch (IOException e){
            plugin.getLogger().log(Level.SEVERE, "Could not save player data for "+uuid,e);
        }
    }
    public Optional<PlayerKitLayout> loadPlayerLayout(UUID uuid,String kitId){
        File playerFile = getPlayerFile(uuid);
        if(!playerFile.exists()) return Optional.empty();

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        String path = "layouts."+kitId;

        if(!config.contains(path))return Optional.empty();

        List<?> rawInventory = config.getList(path+".inventory-contents");
        List<?> rawArmor = config.getList(path+".armor-contents");

        if(rawInventory == null || rawArmor == null) return Optional.empty();

        ItemStack[] inventory = rawInventory.toArray(new ItemStack[0]);
        ItemStack[] armor = rawArmor.toArray(new ItemStack[0]);

        return Optional.of(new PlayerKitLayout(inventory,armor));
    }

    public void deletePlayerLayout(UUID uuid,String kitId){
        File playerFile = getPlayerFile(uuid);
        if(!playerFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set("layouts."+kitId,null);
        try {
            config.save(playerFile);
        }catch (IOException e){
            plugin.getLogger().log(Level.SEVERE, "Could not save player data for "+uuid,e);
        }
    }

}
