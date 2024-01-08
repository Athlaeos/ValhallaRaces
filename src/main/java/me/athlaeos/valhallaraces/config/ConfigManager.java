package me.athlaeos.valhallaraces.config;

import java.util.HashMap;

//All credit to spigotmc.org user Bimmr for this manager
public class ConfigManager {

    private static final HashMap<String, Config> configs = new HashMap<>();

    public static HashMap<String, Config> getConfigs() {
        return configs;
    }

    public static Config getConfig(String name) {
        if (!configs.containsKey(name))
            configs.put(name, new Config(name));

        return configs.get(name);
    }

    public static Config saveConfig(String name) {
        return getConfig(name).save();
    }

    public static Config reloadConfig(String name) {
        return getConfig(name).reload();
    }

}
