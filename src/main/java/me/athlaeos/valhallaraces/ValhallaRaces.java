package me.athlaeos.valhallaraces;

import me.athlaeos.valhallaraces.commands.RacesCommand;
import me.athlaeos.valhallaraces.config.ConfigUpdater;
import me.athlaeos.valhallaraces.hooks.RacesPlaceholderExpansion;
import me.athlaeos.valhallaraces.listener.PlayerPickRaceClassListener;
import me.athlaeos.valhallaraces.listener.ValhallaLoadStatsListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class ValhallaRaces extends JavaPlugin {
    private static ValhallaRaces plugin = null;
    private PlayerPickRaceClassListener racePickerListener;

    @Override
    public void onEnable() {
        plugin = this;
        saveConfig("config.yml");
        saveConfig("races.yml");
        saveConfig("classes.yml");

        RaceManager.loadRaces();
        ClassManager.loadClasses();
        new RacesCommand();

        racePickerListener = new PlayerPickRaceClassListener();
        this.getServer().getPluginManager().registerEvents(racePickerListener, this);
        this.getServer().getPluginManager().registerEvents(new ValhallaLoadStatsListener(), this);
        // Plugin startup logic
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RacesPlaceholderExpansion().register();
        }
    }

    public PlayerPickRaceClassListener getRacePickerListener() {
        return racePickerListener;
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
            ConfigUpdater.update(plugin, name, configFile, Arrays.asList("races_decoration", "classes_decoration"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
