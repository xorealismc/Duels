package com.ovidius.xorealis.duels;

import org.bukkit.plugin.java.JavaPlugin;

public final class XorealisDuels extends JavaPlugin {

    private static XorealisDuels instance;
    private XorealisDuels() {}

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Starting XorealisDuels");


    }

    @Override
    public void onDisable() {
        getLogger().info("Stopping XorealisDuels");
    }

    public static XorealisDuels getInstance() {
        return instance;
    }

}
