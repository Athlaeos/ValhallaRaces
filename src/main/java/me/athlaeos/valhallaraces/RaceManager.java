package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.managers.AccumulativeStatManager;
import me.athlaeos.valhallammo.managers.PerkRewardsManager;
import me.athlaeos.valhallammo.perkrewards.PerkReward;
import me.athlaeos.valhallammo.statsources.AccumulativeStatSource;
import me.athlaeos.valhallammo.statsources.EvEAccumulativeStatSource;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class RaceManager {
    private static RaceManager manager = null;
    private static final NamespacedKey raceKey = new NamespacedKey(ValhallaRaces.getPlugin(), "valhallaraces_race");
    private final Location default_spawnpoint;

    private Map<String, Race> registeredRaces = new HashMap<>();

    public RaceManager(){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("config.yml").get();
        default_spawnpoint = parseLocation(config.getString("default_spawnpoint", ""));
    }

    public void loadRaces(){
        registeredRaces = new HashMap<>();
        YamlConfiguration config = ConfigManager.getInstance().getConfig("races.yml").get();
        ConfigurationSection raceSection = config.getConfigurationSection("");
        if (raceSection != null){
            for (String stat : AccumulativeStatManager.getInstance().getSources().keySet()){
                Collection<AccumulativeStatSource> sources = AccumulativeStatManager.getInstance().getSources().get(stat);
                for (AccumulativeStatSource source : new HashSet<>(sources)){
                    if (source instanceof OffensiveRaceStatSource || source instanceof RaceStatSource){
                        AccumulativeStatManager.getInstance().getSources().get(stat).remove(source);
                    }
                }
            }
            for (String r : raceSection.getKeys(false)){
                Location cityCenter = parseLocation(config.getString(r + ".city_coordinates", ""));
                if (cityCenter == null) cityCenter = default_spawnpoint;
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

                ConfigurationSection statsSection = config.getConfigurationSection(r + ".stat_buffs");
                if (statsSection != null){
                    for (String statType : statsSection.getKeys(false)){
                        double buff = config.getDouble(r + ".stat_buffs." + statType);
                        if (buff != 0){
                            if (AccumulativeStatManager.getInstance().getSources().getOrDefault(statType, new HashSet<>()).stream()
                                    .anyMatch(source -> source instanceof EvEAccumulativeStatSource)){
                                if (AccumulativeStatManager.getInstance().getAttackerStatPossessiveMap().getOrDefault(statType, false)){
                                    AccumulativeStatManager.getInstance().register(statType, new OffensiveRaceStatSource(r, buff));
                                } else {
                                    AccumulativeStatManager.getInstance().register(statType, new DefensiveRaceStatSource(r, buff));
                                }
                            } else {
                                AccumulativeStatManager.getInstance().register(statType, new RaceStatSource(r, buff));
                            }
                        }
                    }
                }

                String permissionRequired = config.getString(r + ".permission");
                if (permissionRequired != null){
                    if (ValhallaRaces.getPlugin().getServer().getPluginManager().getPermission(permissionRequired) == null){
                        ValhallaRaces.getPlugin().getServer().getPluginManager().addPermission(new Permission(permissionRequired));
                    }
                }
                registeredRaces.put(r, new Race(r, iconDisplayName, chatPrefix, cityCenter, icon, alternativeIcon, modelData, guiPosition, description, permissionRequired, commands, perkRewards, antiPerkRewards));
            }
        }
    }

    public Race getRace(Player p){
        if (p.getPersistentDataContainer().has(raceKey, PersistentDataType.STRING)){
            return registeredRaces.get(p.getPersistentDataContainer().get(raceKey, PersistentDataType.STRING));
        }
        return null;
    }

    public void setRace(Player p, Race race){
        Race existingRace = getRace(p);
        if (existingRace != null){
            for (PerkReward reward : existingRace.getAntiPerkRewards()){
                reward.execute(p);
            }
        }
        if (race == null) {
            p.getPersistentDataContainer().remove(raceKey);
        } else {
            p.getPersistentDataContainer().set(raceKey, PersistentDataType.STRING, race.getName());
            for (PerkReward reward : race.getPerkRewards()){
                reward.execute(p);
            }
            for (String command : race.getCommands()){
                String delayArg = StringUtils.substringBetween(command, "<delay:", ">");
                if (delayArg != null){
                    try {
                        int delay = Integer.parseInt(delayArg);
                        ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(
                                ValhallaRaces.getPlugin(),
                                () -> { ValhallaRaces.getPlugin().getServer().dispatchCommand(
                                        ValhallaRaces.getPlugin().getServer().getConsoleSender(), command
                                                .replace("%player%", p.getName())
                                                .replace("<delay:" + delayArg + ">", ""));
                                }
                                , delay);
                    } catch (IllegalArgumentException ignored){
                        ValhallaRaces.getPlugin().getServer().getLogger().warning("Race command execution delay in command '/" + command + "' was invalid, no command executed");
                    }
                } else {
                    ValhallaRaces.getPlugin().getServer().dispatchCommand(ValhallaRaces.getPlugin().getServer().getConsoleSender(), command.replace("%player%", p.getName()));
                }
            }
        }
    }

    public static RaceManager getInstance(){
        if (manager == null) manager = new RaceManager();
        return manager;
    }

    public Map<String, Race> getRegisteredRaces() {
        return registeredRaces;
    }

    private Location parseLocation(String locationString){
        if (locationString == null) return null;
        String[] args = locationString.split(",");
        if (args.length >= 3){
            try {
                World w;
                double x = Double.parseDouble(args[1 - (args.length == 3 ? 1 : 0)]);
                double y = Double.parseDouble(args[2 - (args.length == 3 ? 1 : 0)]);
                double z = Double.parseDouble(args[3 - (args.length == 3 ? 1 : 0)]);
                if (args.length == 4){
                    w = ValhallaMMO.getPlugin().getServer().getWorld(args[0]);
                } else {
                    w = ValhallaMMO.getPlugin().getServer().getWorlds().get(0);
                }
                if (w == null) return null;
                return new Location(w, x, y, z);
            } catch (IllegalArgumentException ignore){
            }
        }
        return null;
    }

    public void reload(){
        manager = null;
        ConfigManager.getInstance().getConfig("races.yml").reload();
        ConfigManager.getInstance().getConfig("races.yml").save();
        RaceManager.getInstance().loadRaces();
    }
}
