package com.ovidius.xorealis.duels;

import com.ovidius.xorealis.duels.listeners.PlayerListener;
import com.ovidius.xorealis.duels.manager.ArenaManager;
import com.ovidius.xorealis.duels.manager.KitManager;
import com.ovidius.xorealis.duels.manager.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class XorealisDuels extends JavaPlugin {

    private static XorealisDuels instance;

    private MenuManager menuManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;

    private XorealisDuels() {}

    @Override
    public void onEnable() {
        instance = this;

        loadManager();
        loadListeners();

        getLogger().info("Starting XorealisDuels");
        getLogger().info("Loading Managers");
        getLogger().info("Loading Listeners");

    }

    @Override
    public void onDisable() {
        getLogger().info("Stopping XorealisDuels");
    }



    private void loadManager() {
        menuManager = new MenuManager();
        arenaManager = new ArenaManager();
        kitManager = new KitManager();
    }
    private void loadListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    public static XorealisDuels getInstance() {
        return instance;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }
}
