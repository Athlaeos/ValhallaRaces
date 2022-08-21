package me.athlaeos.valhallaraces.listener;

import me.athlaeos.valhallammo.menus.PlayerMenuUtilManager;
import me.athlaeos.valhallaraces.*;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerPickRaceClassListener implements Listener {

    private final boolean raceOnJoin = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("pick_race");
    private final boolean classOnJoin = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("pick_class");

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if (raceOnJoin){
            if (RaceManager.getInstance().getRace(e.getPlayer()) == null){
                RacePickerMenu menu = new RacePickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
            } else if (classOnJoin){
                if (ClassManager.getInstance().getClass(e.getPlayer()) == null){
                    ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                    ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                }
            }
        }
        if (classOnJoin){
            if (ClassManager.getInstance().getClass(e.getPlayer()) == null){
                ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (raceOnJoin){
            if (e.getInventory().getHolder() instanceof RacePickerMenu || e.getInventory().getHolder() instanceof ClassPickerMenu){
                if (RaceManager.getInstance().getRace((Player) e.getPlayer()) == null && RaceManager.getInstance().getRegisteredRaces().size() > 0){
                    // if the player hasn't picked a race yet, and they close the menu, re-open race picker menu
                    if (e.getInventory().getHolder() instanceof RacePickerMenu){
                        RacePickerMenu menu = new RacePickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility((Player) e.getPlayer()));
                        if (menu.hasRacesAvailable()){
                            ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 1L);
                        }
                    }
                } else if (classOnJoin && ClassManager.getInstance().getClass((Player) e.getPlayer()) == null && ClassManager.getInstance().getRegisteredClasses().size() > 0){
                    ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility((Player) e.getPlayer()));
                    if (e.getInventory().getHolder() instanceof ClassPickerMenu){
                        // if the player hasn't picked a class yet, and they close the menu, re-open class picker menu
                        if (menu.hasClassesAvailable()){
                            ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 1L);
                        }
                    } else if (e.getInventory().getHolder() instanceof RacePickerMenu) {
                        if (menu.hasClassesAvailable()){
                            ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 1L);
                        }
                    }
                }
            }
        } else if (classOnJoin){
            if (ClassManager.getInstance().getClass((Player) e.getPlayer()) == null && ClassManager.getInstance().getRegisteredClasses().size() > 0){
                if (e.getInventory().getHolder() instanceof ClassPickerMenu){
                    ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility((Player) e.getPlayer()));
                    // if the player hasn't picked a class yet, and they close the menu, re-open class picker menu
                    if (menu.hasClassesAvailable()){
                        ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 1L);
                    }
                }
            }
        }
    }
}
