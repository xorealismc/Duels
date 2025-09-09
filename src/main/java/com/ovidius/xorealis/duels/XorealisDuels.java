package com.ovidius.xorealis.duels;

import com.ovidius.xorealis.duels.listeners.MenuListener;
import com.ovidius.xorealis.duels.listeners.PlayerListener;
import com.ovidius.xorealis.duels.manager.*;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
@Getter
public final class XorealisDuels extends JavaPlugin {

    @Getter private static XorealisDuels instance;

    private MenuManager menuManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private PlayerDataManager playerDataManager;
    private QueueManager queueManager;

    private XorealisDuels() {}

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("kits.yml", false);
        saveResource("arenas.yml", false);

        loadManager();
        loadListeners();

        getLogger().info("XorealisDuels has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Stopping XorealisDuels");
    }

    private void loadManager() {
        menuManager = new MenuManager();
        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        playerDataManager = new PlayerDataManager(this);
        queueManager = new QueueManager(this);

        arenaManager.loadArenas();
        kitManager.loadKits();
    }
    private void loadListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }
}
