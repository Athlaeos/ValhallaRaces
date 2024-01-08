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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class ClassManager {
    private static final NamespacedKey CLASS_KEY = new NamespacedKey(ValhallaRaces.getPlugin(), "valhallaraces_class");
    public static final NamespacedKey CONFIRM_BUTTON = new NamespacedKey(ValhallaRaces.getPlugin(), "confirm_classes");

    private static final Map<UUID, Map<Integer, Class>> classCache = new HashMap<>();

    private static Map<String, Class> registeredClasses = new HashMap<>();
    private static ItemStack confirmButton = new ItemBuilder(Material.STRUCTURE_VOID).name("&aConfirm").data(-1).intTag(CONFIRM_BUTTON, 1).get();
    private static int confirmButtonPosition = 40;

    public static void loadClasses(){
        registeredClasses = new HashMap<>();
        YamlConfiguration config = ConfigManager.getConfig("classes.yml").get();
        confirmButton = new ItemBuilder(Catch.catchOrElse(() -> Material.valueOf(config.getString("confirm_button_type", "STRUCTURE_VOID:-1").split(":")[0]), Material.BOOK))
                .name(config.getString("confirm_button_name", "&aConfirm Classes"))
                .data(Catch.catchOrElse(() -> Integer.parseInt(config.getString("confirm_button_type", "STRUCTURE_VOID:-1").split(":")[1]), -1))
                .lore(config.getStringList("confirm_button_lore"))
                .intTag(CONFIRM_BUTTON, 1)
                .get();
        confirmButtonPosition = config.getInt("confirm_button_position", 40);
        ConfigurationSection classSection = config.getConfigurationSection("classes");
        if (classSection != null){
            for (String stat : AccumulativeStatManager.getSources().keySet()){
                StatCollector collector = AccumulativeStatManager.getSources().get(stat);
                for (AccumulativeStatSource source : new HashSet<>(collector.getStatSources())){
                    collector.getStatSources().remove(source);
                }
            }

            for (String c : classSection.getKeys(false)){
                int group = config.getInt("classes." + c + ".group");
                ItemStack icon = config.getItemStack("classes." + c + ".true_icon");
                if (ItemUtils.isEmpty(icon)){
                    icon = new ItemBuilder(Catch.catchOrElse(() -> Material.valueOf(config.getString("classes." + c + ".icon", "BOOK:-1").split(":")[0]), Material.BOOK))
                            .name(config.getString("classes." + c + ".display_name"))
                            .data(Catch.catchOrElse(() -> Integer.parseInt(config.getString("classes." + c + ".icon", "-1").split(":")[1]), -1))
                            .lore(config.getStringList("classes." + c + ".description"))
                            .intTag(CONFIRM_BUTTON, 1)
                            .get();
                }
                ItemStack lockedIcon = config.getItemStack("classes." + c + ".true_locked_icon");
                if (ItemUtils.isEmpty(lockedIcon)){
                    lockedIcon = new ItemBuilder(Catch.catchOrElse(() -> Material.valueOf(config.getString("classes." + c + ".icon_locked", "BOOK:-1").split(":")[0]), Material.BOOK))
                            .name(config.getString("classes." + c + ".display_name"))
                            .data(Catch.catchOrElse(() -> Integer.parseInt(config.getString("classes." + c + ".icon_locked", "-1").split(":")[1]), -1))
                            .lore(config.getStringList("classes." + c + ".description"))
                            .intTag(CONFIRM_BUTTON, 1)
                            .get();
                }
                String chatPrefix = config.getString("classes." + c + ".prefix");
                int guiPosition = config.getInt("classes." + c + ".position");
                List<String> commands = config.getStringList("classes." + c + ".commands");
                List<String> limitToRaces = config.getStringList("classes." + c + ".race_filter");

                Collection<PerkReward> perkRewards = new HashSet<>();
                ConfigurationSection rewardSection = config.getConfigurationSection("classes." + c + ".perk_rewards");
                if (rewardSection != null){
                    for (String type : rewardSection.getKeys(false)){
                        Object arg = config.get("classes." + c + ".perk_rewards." + type);
                        if (arg == null) continue;
                        PerkReward reward = PerkRewardRegistry.createReward(type, arg);
                        if (reward == null) continue;
                        perkRewards.add(reward);
                    }
                }

                ConfigurationSection statsSection = config.getConfigurationSection("classes." + c + ".stat_buffs");
                if (statsSection != null){
                    for (String statType : statsSection.getKeys(false)){
                        double buff = config.getDouble("classes." + c + ".stat_buffs." + statType);
                        if (buff != 0){
                            StatCollector collector = AccumulativeStatManager.getSources().get(statType);
                            if (collector == null) continue;

                            if (collector.getStatSources().stream().anyMatch(source -> source instanceof EvEAccumulativeStatSource)){
                                if (collector.isAttackerPossessive()){
                                    AccumulativeStatManager.register(statType, new OffensiveClassStatSource(c, buff));
                                } else {
                                    AccumulativeStatManager.register(statType, new DefensiveClassStatSource(c, buff));
                                }
                            } else {
                                AccumulativeStatManager.register(statType, new ClassStatSource(c, buff));
                            }
                        }
                    }
                }

                String permissionRequired = config.getString("classes." + c + ".permission");
                if (permissionRequired != null){
                    if (ValhallaRaces.getPlugin().getServer().getPluginManager().getPermission(permissionRequired) == null){
                        ValhallaRaces.getPlugin().getServer().getPluginManager().addPermission(new Permission(permissionRequired));
                    }
                }
                registeredClasses.put(c, new Class(c, chatPrefix, icon, lockedIcon, guiPosition, group, limitToRaces, commands, permissionRequired, perkRewards));
            }
        }
    }

    public static Map<Integer, Class> getClasses(Player p){
        if (classCache.containsKey(p.getUniqueId())) return classCache.get(p.getUniqueId());
        if (p.getPersistentDataContainer().has(CLASS_KEY, PersistentDataType.STRING)){
            Collection<String> encodedClasses = Arrays.stream(p.getPersistentDataContainer().getOrDefault(CLASS_KEY, PersistentDataType.STRING, "").split(";")).collect(Collectors.toList());
            if (encodedClasses.isEmpty()) return new HashMap<>();
            Map<Integer, Class> classes = new HashMap<>();
            encodedClasses.forEach(c -> {
                Class playerClass = registeredClasses.get(c);
                if (playerClass == null) return;
                classes.put(playerClass.getGroup(), playerClass);
            });
            classCache.put(p.getUniqueId(), classes);
            return classes;
        }
        return new HashMap<>();
    }

    public static void setClasses(Player p, Collection<Class> classes){
        Map<Integer, Class> existingClasses = getClasses(p);
        for (Class c : existingClasses.values()){
            for (PerkReward reward : c.getPerkRewards()){
                reward.remove(p);
            }
        }
        if (classes == null || classes.isEmpty()) {
            p.getPersistentDataContainer().remove(CLASS_KEY);
        } else {
            p.getPersistentDataContainer().set(CLASS_KEY, PersistentDataType.STRING, classes.stream().map(Class::getName).collect(Collectors.joining(";")));
            for (Class c : classes){
                for (PerkReward reward : c.getPerkRewards()){
                    reward.apply(p);
                }
                for (String command : c.getCommands()){
                    String delayArg = StringUtils.substringBetween(command, "<delay:", ">");
                    if (delayArg != null){
                        try {
                            int delay = Integer.parseInt(delayArg);
                            ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(
                                    ValhallaRaces.getPlugin(),
                                    () -> ValhallaRaces.getPlugin().getServer().dispatchCommand(
                                            ValhallaRaces.getPlugin().getServer().getConsoleSender(), command
                                                    .replace("%player%", p.getName())
                                                    .replace("<delay:" + delayArg + ">", ""))
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
        classCache.remove(p.getUniqueId());
    }

    public static Map<String, Class> getRegisteredClasses() { return registeredClasses; }

    public static void reload(){
        ConfigManager.getConfig("classes.yml").reload();
        ConfigManager.getConfig("classes.yml").save();
        loadClasses();
    }

    public static int getConfirmButtonPosition() { return confirmButtonPosition; }
    public static ItemStack getConfirmButton() { return confirmButton; }
}
