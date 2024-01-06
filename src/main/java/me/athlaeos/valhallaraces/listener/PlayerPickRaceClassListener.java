package me.athlaeos.valhallaraces.listener;

import me.athlaeos.valhallammo.menus.PlayerMenuUtilManager;
import me.athlaeos.valhallaraces.*;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerPickRaceClassListener implements Listener {

    private boolean forcePickRace = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("pick_race");
    private boolean forcePickClass = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("pick_class");
    private boolean pickOnJoin = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("on_join", false);

    public void reload(){
        forcePickRace = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("pick_race");
        forcePickClass = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("pick_class");
        pickOnJoin = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("on_join");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if (!pickOnJoin) return;
        if (forcePickRace){
            if (RaceManager.getInstance().getRace(e.getPlayer()) == null){
                RacePickerMenu menu = new RacePickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                return;
            } else if (forcePickClass){
                if (ClassManager.getInstance().getClass(e.getPlayer()) == null){
                    ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                    ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                    return;
                }
            }
        }
        if (forcePickClass){
            if (ClassManager.getInstance().getClass(e.getPlayer()) == null){
                ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if (pickOnJoin || e.isCancelled()) return;
        if (CooldownManager.getInstance().isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_raceclasspicker")){
            CooldownManager.getInstance().setCooldown(e.getPlayer().getUniqueId(), 10000, "cooldown_raceclasspicker");
            if (forcePickRace){
                if (RaceManager.getInstance().getRace(e.getPlayer()) == null){
                    RacePickerMenu menu = new RacePickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                    ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                    return;
                } else if (forcePickClass){
                    if (ClassManager.getInstance().getClass(e.getPlayer()) == null){
                        ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                        ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                        return;
                    }
                }
            }
            if (forcePickClass){
                if (ClassManager.getInstance().getClass(e.getPlayer()) == null){
                    ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility(e.getPlayer()));
                    ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getInventory().getHolder() instanceof RacePickerMenu || e.getInventory().getHolder() instanceof ClassPickerMenu){
            RacePickerMenu raceMenu = new RacePickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility((Player) e.getPlayer()));
            ClassPickerMenu classMenu = new ClassPickerMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility((Player) e.getPlayer()));
            if (e.getInventory().getHolder() instanceof RacePickerMenu && RaceManager.getInstance().getRace((Player) e.getPlayer()) == null && raceMenu.hasRacesAvailable()){
                // if the player hasn't picked a race yet, and they close the race picker menu, re-open race picker menu
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), raceMenu::open, 1L);
            } else if (e.getInventory().getHolder() instanceof ClassPickerMenu && ClassManager.getInstance().getClass((Player) e.getPlayer()) == null && classMenu.hasClassesAvailable()){
                // if the player hasn't picked a class yet, and they close the class picker menu, re-open class picker menu
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), classMenu::open, 1L);
            } else if (e.getInventory().getHolder() instanceof RacePickerMenu && ClassManager.getInstance().getClass((Player) e.getPlayer()) == null && classMenu.hasClassesAvailable()){
                // if the player has no class, and they just closed the race picker menu, open the class picker menu afterwards
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), classMenu::open, 1L);
            }
        }
    }
}
