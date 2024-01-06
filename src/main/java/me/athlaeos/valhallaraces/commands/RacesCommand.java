package me.athlaeos.valhallaraces.commands;

import me.athlaeos.valhallammo.menus.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallaraces.*;
import me.athlaeos.valhallaraces.Class;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RacesCommand implements TabExecutor {
    public RacesCommand(){
        Objects.requireNonNull(ValhallaRaces.getPlugin().getCommand("races")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("valhallaraces.manager")) {
            sender.sendMessage(Utils.chat("&cYou do not have permission to use this command!"));
            return true;
        }
        if (args.length == 0){
            sender.sendMessage(Utils.chat("&8&m              [&dValhalla&5Races &7by &dAthlaeos&8&m]              "));
        } else if (args.length >= 3){
            if (args[0].equalsIgnoreCase("seticon")){
                if (sender instanceof Player){
                    ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
                    if (args[1].equalsIgnoreCase("race")){
                        Race race = RaceManager.getInstance().getRegisteredRaces().get(args[2]);
                        if (race == null) {
                            sender.sendMessage(Utils.chat("&cProvided race does not exist"));
                            return true;
                        }
                        YamlConfiguration config = ConfigManager.getInstance().getConfig("races.yml").get();
                        if (Utils.isItemEmptyOrNull(hand)){
                            sender.sendMessage(Utils.chat("&aRemoved true icon! Turned back to standard icon"));
                            race.setIcon(null);
                            config.set(race.getName() + ".true_icon", null);
                        } else {
                            sender.sendMessage(Utils.chat("&aTrue icon set!"));
                            race.setIcon(hand);
                            config.set(race.getName() + ".true_icon", hand);
                        }
                        ConfigManager.getInstance().saveConfig("races.yml");
                        return true;
                    } else if (args[1].equalsIgnoreCase("class")) {
                        Class playerClass = ClassManager.getInstance().getRegisteredClasses().get(args[2]);
                        if (playerClass == null) {
                            sender.sendMessage(Utils.chat("&cProvided class does not exist"));
                            return true;
                        }
                        YamlConfiguration config = ConfigManager.getInstance().getConfig("classes.yml").get();
                        if (Utils.isItemEmptyOrNull(hand)){
                            sender.sendMessage(Utils.chat("&aRemoved true icon! Turned back to standard icon"));
                            playerClass.setIcon(null);
                            config.set(playerClass.getName() + ".true_icon", null);
                        } else {
                            sender.sendMessage(Utils.chat("&aTrue icon set!"));
                            playerClass.setIcon(hand);
                            config.set(playerClass.getName() + ".true_icon", hand);
                        }
                        ConfigManager.getInstance().saveConfig("classes.yml");
                        return true;
                    }
                } else {
                    sender.sendMessage(Utils.chat("&cOnly players can do this, since you need to be holding an item!"));
                    return true;
                }
            } else {
                Collection<Player> targets = Utils.selectPlayers(sender, args[2]);
                if (targets.isEmpty()) {
                    sender.sendMessage(Utils.chat("&cPlayer(s) not found"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("reset")){
                    if (args[1].equalsIgnoreCase("race")){
                        for (Player target : targets){
                            RaceManager.getInstance().setRace(target, null);
                            new RacePickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(target)).open();
                        }
                        sender.sendMessage(Utils.chat("&aRace reset&7, player(s) are now picking different race"));
                        return true;
                    } else if (args[1].equalsIgnoreCase("class")) {
                        for (Player target : targets){
                            ClassManager.getInstance().setClass(target, null);
                            new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(target)).open();
                        }
                        sender.sendMessage(Utils.chat("&aClass reset&7, player is now picking different class"));
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("set")){
                    if (args.length >= 4){
                        if (args[1].equalsIgnoreCase("race")){
                            Race race = RaceManager.getInstance().getRegisteredRaces().get(args[3]);
                            if (race == null) {
                                sender.sendMessage(Utils.chat("&cProvided race does not exist"));
                                return true;
                            }
                            for (Player target : targets){
                                RaceManager.getInstance().setRace(target, race);
                            }
                            sender.sendMessage(Utils.chat("&aRace set&7, player is now a/an " + Utils.getItemName(race.getIcon())));
                            return true;
                        } else if (args[1].equalsIgnoreCase("class")) {
                            Class playerClass = ClassManager.getInstance().getRegisteredClasses().get(args[3]);
                            if (playerClass == null) {
                                sender.sendMessage(Utils.chat("&cProvided class does not exist"));
                                return true;
                            }
                            for (Player target : targets){
                                ClassManager.getInstance().setClass(target, playerClass);
                            }
                            sender.sendMessage(Utils.chat("&aClass set&7, player is now a/an " + Utils.getItemName(playerClass.getIcon())));
                            return true;
                        }
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("reload")){
            ConfigManager.getInstance().getConfig("config.yml").reload();
            ConfigManager.getInstance().getConfig("config.yml").save();
            RaceManager.getInstance().reload();
            ClassManager.getInstance().reload();
            ValhallaRaces.getPlugin().getRacePickerListener().reload();
            ClassPickerMenu.reload();
            RacePickerMenu.reload();

            sender.sendMessage(Utils.chat("&aReload complete"));
            return true;
        }
        sender.sendMessage(Utils.chat("&cInvalid command usage, /races <set/reset/reload> <class/race> [player] [class/race]"));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1){
            return Arrays.asList("reset", "set", "seticon", "reload");
        }
        if (args.length == 2){
            return Arrays.asList("class", "race");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("seticon")){
            if (args[1].equalsIgnoreCase("class")){
                return new ArrayList<>(ClassManager.getInstance().getRegisteredClasses().keySet());
            } else if (args[1].equalsIgnoreCase("race")) {
                return new ArrayList<>(RaceManager.getInstance().getRegisteredRaces().keySet());
            }
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("set")){
            if (args[1].equalsIgnoreCase("class")){
                return new ArrayList<>(ClassManager.getInstance().getRegisteredClasses().keySet());
            } else if (args[1].equalsIgnoreCase("race")) {
                return new ArrayList<>(RaceManager.getInstance().getRegisteredRaces().keySet());
            }
        }
        return null;
    }
}
