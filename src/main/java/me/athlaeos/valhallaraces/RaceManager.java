package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.StatCollector;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
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
    private static final NamespacedKey raceKey = new NamespacedKey(ValhallaRaces.getPlugin(), "valhallaraces_race");
    public static final NamespacedKey CONFIRM_BUTTON = new NamespacedKey(ValhallaRaces.getPlugin(), "confirm_classes");
    private static Location defaultSpawnpoint = null;

    private static final Map<String, Race> registeredRaces = new HashMap<>();
    private static ItemStack confirmButton = new ItemBuilder(Material.STRUCTURE_VOID).name("&aConfirm").data(-1).intTag(CONFIRM_BUTTON, 1).get();
    private static int confirmButtonPosition = 40;

    public static void loadRaces(){
        YamlConfiguration config = ConfigManager.getConfig("races.yml").get();
        confirmButton = new ItemBuilder(Catch.catchOrElse(() -> Material.valueOf(config.getString("confirm_button_type", "STRUCTURE_VOID:-1").split(":")[0]), Material.BOOK))
                .name(config.getString("confirm_button_name", "&aConfirm Classes"))
                .data(Catch.catchOrElse(() -> Integer.parseInt(config.getString("confirm_button_type", "STRUCTURE_VOID:-1").split(":")[1]), -1))
                .lore(config.getStringList("confirm_button_lore"))
                .intTag(CONFIRM_BUTTON, 1)
                .get();
        confirmButtonPosition = config.getInt("confirm_button_position", 40);
        defaultSpawnpoint = parseLocation(config.getString("default_spawnpoint", ""));
        ConfigurationSection raceSection = config.getConfigurationSection("races");
        if (raceSection != null){
            for (String stat : AccumulativeStatManager.getSources().keySet()){
                StatCollector collector = AccumulativeStatManager.getSources().get(stat);
                for (AccumulativeStatSource source : new HashSet<>(collector.getStatSources())){
                    if (!(source instanceof RaceSource)) continue;
                    collector.getStatSources().remove(source);
                }
            }
            for (String r : raceSection.getKeys(false)){
                Location cityCenter = parseLocation(config.getString("races." + r + ".city_coordinates", ""));
                if (cityCenter == null) cityCenter = defaultSpawnpoint;
                ItemStack icon = config.getItemStack("races." + r + ".true_icon");
                if (ItemUtils.isEmpty(icon)){
                    icon = new ItemBuilder(Catch.catchOrElse(() -> Material.valueOf(config.getString("races." + r + ".icon", "BOOK:-1").split(":")[0]), Material.BOOK))
                            .name(config.getString("races." + r + ".display_name"))
                            .data(Catch.catchOrElse(() -> Integer.parseInt(config.getString("races." + r + ".icon", "-1").split(":")[1]), -1))
                            .lore(config.getStringList("races." + r + ".description"))
                            .intTag(CONFIRM_BUTTON, 1)
                            .get();
                }
                ItemStack lockedIcon = config.getItemStack("races." + r + ".true_icon");
                if (ItemUtils.isEmpty(lockedIcon)){
                    lockedIcon = new ItemBuilder(Catch.catchOrElse(() -> Material.valueOf(config.getString("races." + r + ".icon_locked", "BOOK:-1").split(":")[0]), Material.BOOK))
                            .name(config.getString("races." + r + ".display_name"))
                            .data(Catch.catchOrElse(() -> Integer.parseInt(config.getString("races." + r + ".icon_locked", "-1").split(":")[1]), -1))
                            .lore(config.getStringList("races." + r + ".description"))
                            .intTag(CONFIRM_BUTTON, 1)
                            .get();
                }
                String chatPrefix = config.getString("races." + r + ".prefix");
                int guiPosition = config.getInt("races." + r + ".position");
                List<String> commands = config.getStringList("races." + r + ".commands");
                List<String> undoCommands = config.getStringList("races." + r + ".undo_commands");
                Collection<PerkReward> perkRewards = new HashSet<>();
                ConfigurationSection rewardSection = config.getConfigurationSection("races." + r + ".perk_rewards");
                if (rewardSection != null){
                    for (String type : rewardSection.getKeys(false)){
                        Object arg = config.get("races." + r + ".perk_rewards." + type);
                        if (arg == null) continue;
                        PerkReward reward = PerkRewardRegistry.createReward(type, arg);
                        if (reward == null) continue;
                        perkRewards.add(reward);
                    }
                }

                ConfigurationSection statsSection = config.getConfigurationSection("races." + r + ".stat_buffs");
                if (statsSection != null){
                    for (String statType : statsSection.getKeys(false)){
                        double buff = config.getDouble("races." + r + ".stat_buffs." + statType);
                        if (buff != 0){
                            StatCollector collector = AccumulativeStatManager.getSources().get(statType);
                            if (collector == null) continue;
                            if (collector.getStatSources().stream().anyMatch(source -> source instanceof EvEAccumulativeStatSource)){
                                if (collector.isAttackerPossessive()){
                                    AccumulativeStatManager.register(statType, new OffensiveRaceStatSource(r, buff));
                                } else {
                                    AccumulativeStatManager.register(statType, new DefensiveRaceStatSource(r, buff));
                                }
                            } else {
                                AccumulativeStatManager.register(statType, new RaceStatSource(r, buff));
                            }
                        }
                    }
                }

                String permissionRequired = config.getString("races." + r + ".permission");
                if (permissionRequired != null){
                    if (ValhallaRaces.getPlugin().getServer().getPluginManager().getPermission(permissionRequired) == null){
                        ValhallaRaces.getPlugin().getServer().getPluginManager().addPermission(new Permission(permissionRequired));
                    }
                }
                registeredRaces.put(r, new Race(r, chatPrefix, cityCenter, icon, lockedIcon, guiPosition, permissionRequired, commands, undoCommands, perkRewards));
            }
        }
    }

    public static Race getRace(Player p){
        return registeredRaces.get(p.getPersistentDataContainer().get(raceKey, PersistentDataType.STRING));
    }

    public static void setRace(Player p, Race race){
        Race existingRace = getRace(p);
        if (existingRace != null){
            for (PerkReward reward : existingRace.getPerkRewards()){
                reward.remove(p);
            }

            for (String command : existingRace.getUndoCommands()){
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
                                }, delay);
                    } catch (IllegalArgumentException ignored){
                        ValhallaRaces.getPlugin().getServer().getLogger().warning("Race undo command execution delay in command '/" + command + "' was invalid, no command executed");
                    }
                } else {
                    ValhallaRaces.getPlugin().getServer().dispatchCommand(ValhallaRaces.getPlugin().getServer().getConsoleSender(), command.replace("%player%", p.getName()));
                }
            }
        }
        if (race == null) {
            p.getPersistentDataContainer().remove(raceKey);
        } else {
            for (PerkReward reward : race.getPerkRewards()){
                reward.apply(p);
            }
            p.getPersistentDataContainer().set(raceKey, PersistentDataType.STRING, race.getName());
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
                                }, delay);
                    } catch (IllegalArgumentException ignored){
                        ValhallaRaces.getPlugin().getServer().getLogger().warning("Race command execution delay in command '/" + command + "' was invalid, no command executed");
                    }
                } else {
                    ValhallaRaces.getPlugin().getServer().dispatchCommand(ValhallaRaces.getPlugin().getServer().getConsoleSender(), command.replace("%player%", p.getName()));
                }
            }
        }
    }

    public static Map<String, Race> getRegisteredRaces() { return registeredRaces; }

    private static Location parseLocation(String locationString){
        if (locationString == null) return null;
        String[] args = locationString.split(",");
        if (args.length >= 3){
            try {
                World w;
                double x = Double.parseDouble(args[1 - (args.length == 3 ? 1 : 0)]);
                double y = Double.parseDouble(args[2 - (args.length == 3 ? 1 : 0)]);
                double z = Double.parseDouble(args[3 - (args.length == 3 ? 1 : 0)]);
                if (args.length == 4){
                    w = ValhallaRaces.getPlugin().getServer().getWorld(args[0]);
                } else {
                    w = ValhallaRaces.getPlugin().getServer().getWorlds().get(0);
                }
                if (w == null) return null;
                return new Location(w, x, y, z);
            } catch (IllegalArgumentException ignore){
            }
        }
        return null;
    }

    public static void reload(){
        ConfigManager.getConfig("races.yml").reload();
        ConfigManager.getConfig("races.yml").save();
        RaceManager.loadRaces();
    }

    public static int getConfirmButtonPosition() { return confirmButtonPosition; }
    public static ItemStack getConfirmButton() { return confirmButton; }
    public static Location getDefaultSpawnpoint() { return defaultSpawnpoint; }
}
