package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.config.ConfigUpdater;
import me.athlaeos.valhallaraces.commands.RacesCommand;
import me.athlaeos.valhallaraces.hooks.RacesPlaceholderExpansion;
import me.athlaeos.valhallaraces.listener.PlayerPickRaceClassListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class ValhallaRaces extends JavaPlugin {
    private static ValhallaRaces plugin = null;

    @Override
    public void onEnable() {
        plugin = this;
        saveAndUpdateConfig("config.yml");
        saveConfig("races.yml");
        saveConfig("classes.yml");

        RaceManager.getInstance().loadRaces();
        ClassManager.getInstance().loadClasses();
        new RacesCommand();

        this.getServer().getPluginManager().registerEvents(new PlayerPickRaceClassListener(), this);
        // Plugin startup logic
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RacesPlaceholderExpansion().register();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ValhallaRaces getPlugin() {
        return plugin;
    }

    private void saveAndUpdateConfig(String config){
        saveConfig(config);
        updateConfig(config);
    }

    public void saveConfig(String name){
        File config = new File(this.getDataFolder(), name);
        if (!config.exists()){
            this.saveResource(name, false);
        }
    }

    private void updateConfig(String name){
        File configFile = new File(getDataFolder(), name);
        try {
            ConfigUpdater.update(plugin, name, configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
