package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.managers.AccumulativeStatManager;
import me.athlaeos.valhallammo.managers.PerkRewardsManager;
import me.athlaeos.valhallammo.perkrewards.PerkReward;
import me.athlaeos.valhallammo.statsources.EvEAccumulativeStatSource;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ClassManager {
    private static ClassManager manager = null;
    private static final NamespacedKey classKey = new NamespacedKey(ValhallaRaces.getPlugin(), "valhallaraces_class");

    private final Map<String, Class> registeredClasses = new HashMap<>();

    public void loadClasses(){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("classes.yml").get();
        ConfigurationSection raceSection = config.getConfigurationSection("");
        if (raceSection != null){
            for (String r : raceSection.getKeys(false)){
                ItemStack alternativeIcon = config.getItemStack(r + ".true_icon");
                Material icon = Material.BOOK;
                try {
                    icon = Material.valueOf(config.getString(r + ".icon"));
                } catch (IllegalArgumentException ignored){
                }
                String iconDisplayName = config.getString(r + ".display_name");
                String chatPrefix = config.getString(r + ".prefix");
                int modelData = config.getInt(r + ".data");
                int guiPosition = config.getInt(r + ".position");
                List<String> description = config.getStringList(r + ".description");
                List<String> commands = config.getStringList(r + ".commands");
                List<String> limitToRaces = config.getStringList(r + ".race_filter");

                Collection<PerkReward> perkRewards = new HashSet<>();
                ConfigurationSection rewardSection = config.getConfigurationSection(r + ".perk_rewards");
                if (rewardSection != null){
                    for (String type : rewardSection.getKeys(false)){
                        Object arg = config.get(r + ".perk_rewards." + type);
                        if (arg == null) continue;
                        PerkReward reward = PerkRewardsManager.getInstance().createReward(type, arg);
                        if (reward == null) continue;
                        perkRewards.add(reward);
                    }
                }

                ConfigurationSection statsSection = config.getConfigurationSection(r + ".stat_buffs");
                if (statsSection != null){
                    for (String statType : statsSection.getKeys(false)){
                        double buff = config.getDouble(r + ".stat_buffs." + statType);
                        if (buff != 0){
                            if (AccumulativeStatManager.getInstance().getSources().getOrDefault(statType, new HashSet<>()).stream().anyMatch(source -> source instanceof EvEAccumulativeStatSource)){
                                AccumulativeStatManager.getInstance().register(statType, new OffensiveClassStatSource(r, buff));
                            } else {
                                AccumulativeStatManager.getInstance().register(statType, new ClassStatSource(r, buff));
                            }
                        }
                    }
                }

                Collection<PerkReward> antiPerkRewards = new HashSet<>();
                ConfigurationSection antiRewardSection = config.getConfigurationSection(r + ".anti_perk_rewards");
                if (antiRewardSection != null){
                    for (String type : antiRewardSection.getKeys(false)){
                        Object arg = config.get(r + ".anti_perk_rewards." + type);
                        if (arg == null) continue;
                        PerkReward reward = PerkRewardsManager.getInstance().createReward(type, arg);
                        if (reward == null) continue;
                        antiPerkRewards.add(reward);
                    }
                }

                String permissionRequired = config.getString(r + ".permission");
                if (permissionRequired != null){
                    if (ValhallaRaces.getPlugin().getServer().getPluginManager().getPermission(permissionRequired) == null){
                        ValhallaRaces.getPlugin().getServer().getPluginManager().addPermission(new Permission(permissionRequired));
                    }
                }
                registeredClasses.put(r, new Class(r, iconDisplayName, chatPrefix, alternativeIcon, icon, modelData, guiPosition, limitToRaces, commands, description, permissionRequired, perkRewards, antiPerkRewards));
            }
        }
    }

    public Class getClass(Player p){
        if (p.getPersistentDataContainer().has(classKey, PersistentDataType.STRING)){
            return registeredClasses.get(p.getPersistentDataContainer().get(classKey, PersistentDataType.STRING));
        }
        return null;
    }

    public void setClass(Player p, Class playerClass){
        Class existingClass = getClass(p);
        if (existingClass != null){
            for (PerkReward reward : existingClass.getAntiPerkRewards()){
                reward.execute(p);
            }
        }
        if (playerClass == null) {
            p.getPersistentDataContainer().remove(classKey);
        } else {
            p.getPersistentDataContainer().set(classKey, PersistentDataType.STRING, playerClass.getName());
            for (PerkReward reward : playerClass.getPerkRewards()){
                reward.execute(p);
            }
            for (String command : playerClass.getCommands()){
                ValhallaRaces.getPlugin().getServer().dispatchCommand(ValhallaRaces.getPlugin().getServer().getConsoleSender(), command.replace("%player%", p.getName()));
            }
        }
    }

    public Map<String, Class> getRegisteredClasses() {
        return registeredClasses;
    }

    public static ClassManager getInstance(){
        if (manager == null) manager = new ClassManager();
        return manager;
    }
}
