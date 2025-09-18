package com.ovidius.xorealis.duels;

import com.ovidius.xorealis.duels.command.DuelsCommand;
import com.ovidius.xorealis.duels.command.LeaveQueueCommand;
import com.ovidius.xorealis.duels.command.PartyCommand;
import com.ovidius.xorealis.duels.listeners.DuelProtectionListener;
import com.ovidius.xorealis.duels.listeners.MenuListener;
import com.ovidius.xorealis.duels.listeners.PlayerListener;
import com.ovidius.xorealis.duels.manager.*;
import com.ovidius.xorealis.duels.papi.DuelsPlaceholders;
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
    private DuelManager duelManager;
    private PlayerListener playerListener;
    private PartyManager partyManager;

    public XorealisDuels() {}

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("kits.yml", false);
        saveResource("arenas.yml", false);

        loadManagers();
        loadListeners();
        loadCommands();

        getLogger().info("XorealisDuels has been enabled successfully.");

        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DuelsPlaceholders(this).register();
            getLogger().info("PlaceholderAPI has been registered.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Stopping XorealisDuels");
    }

    private void loadManagers() {
        menuManager = new MenuManager();
        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        playerDataManager = new PlayerDataManager(this);
        queueManager = new QueueManager(this);
        duelManager = new DuelManager(this);
        partyManager = new PartyManager();

        arenaManager.loadArenas();
        kitManager.loadKits();
    }
    private void loadListeners() {
        playerListener = new PlayerListener(this);

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(duelManager, this);
        getServer().getPluginManager().registerEvents(new DuelProtectionListener(this), this);
    }

    private void loadCommands() {
        getCommand("leavequeue").setExecutor(new LeaveQueueCommand(this));
        getCommand("duels").setExecutor(new DuelsCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
    }
}
