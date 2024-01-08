package me.athlaeos.valhallaraces.listener;

import me.athlaeos.valhallammo.event.ValhallaUpdatedStatsEvent;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallaraces.*;
import me.athlaeos.valhallaraces.Class;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class PlayerPickRaceClassListener implements Listener {

    private boolean forcePickRace = ConfigManager.getConfig("config.yml").get().getBoolean("pick_race");
    private boolean forcePickClass = ConfigManager.getConfig("config.yml").get().getBoolean("pick_class");
    private boolean pickOnJoin = ConfigManager.getConfig("config.yml").get().getBoolean("on_join", false);

    public void reload(){
        forcePickRace = ConfigManager.getConfig("config.yml").get().getBoolean("pick_race");
        forcePickClass = ConfigManager.getConfig("config.yml").get().getBoolean("pick_class");
        pickOnJoin = ConfigManager.getConfig("config.yml").get().getBoolean("on_join");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if (!pickOnJoin) return;
        if (forcePickRace){
            if (RaceManager.getRace(e.getPlayer()) == null){
                RacePickerMenu menu = new RacePickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()));
                if (menu.getAvailableRaces().isEmpty()) return;
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                return;
            } else if (forcePickClass){
                if (ClassManager.getClasses(e.getPlayer()).isEmpty()){
                    ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()));
                    if (menu.getAvailableClasses().isEmpty()) return;
                    ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                    return;
                }
            }
        }
        if (forcePickClass){
            if (ClassManager.getClasses(e.getPlayer()).isEmpty()){
                ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()));
                if (menu.getAvailableClasses().isEmpty()) return;
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
            }
        }
    }

    @EventHandler
    public void onValhallaLoadStats(ValhallaUpdatedStatsEvent e){
        for (Class c : ClassManager.getClasses(e.getPlayer()).values()){
            for (PerkReward reward : c.getPerkRewards()) reward.apply(e.getPlayer());
        }
        Race race = RaceManager.getRace(e.getPlayer());
        if (race == null) return;
        for (PerkReward reward : race.getPerkRewards()) reward.apply(e.getPlayer());
    }

    private final Collection<UUID> excludedFromRaceSelection = new HashSet<>();
    private final Collection<UUID> excludedFromClassSelection = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if (pickOnJoin || e.isCancelled()) return;
        if (CooldownManager.getInstance().isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_raceclasspicker")){
            CooldownManager.getInstance().setCooldown(e.getPlayer().getUniqueId(), 10000, "cooldown_raceclasspicker");
            if (forcePickRace){
                if (RaceManager.getRace(e.getPlayer()) == null && !excludedFromRaceSelection.contains(e.getPlayer().getUniqueId())){
                    RacePickerMenu menu = new RacePickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()));
                    if (menu.getAvailableRaces().isEmpty()) {
                        excludedFromRaceSelection.add(e.getPlayer().getUniqueId());
                        return;
                    }
                    ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                    return;
                } else if (forcePickClass && !excludedFromClassSelection.contains(e.getPlayer().getUniqueId())){
                    if (ClassManager.getClasses(e.getPlayer()).isEmpty()){
                        ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()));
                        if (menu.getAvailableClasses().isEmpty()) {
                            excludedFromClassSelection.add(e.getPlayer().getUniqueId());
                            return;
                        }
                        ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                        return;
                    }
                }
            }
            if (forcePickClass && !excludedFromClassSelection.contains(e.getPlayer().getUniqueId())){
                if (ClassManager.getClasses(e.getPlayer()).isEmpty()){
                    ClassPickerMenu menu = new ClassPickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()));
                    if (menu.getAvailableClasses().isEmpty()) {
                        excludedFromClassSelection.add(e.getPlayer().getUniqueId());
                        return;
                    }
                    ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), menu::open, 20L);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getInventory().getHolder() instanceof RacePickerMenu || e.getInventory().getHolder() instanceof ClassPickerMenu){
            RacePickerMenu raceMenu = new RacePickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getPlayer()));
            ClassPickerMenu classMenu = new ClassPickerMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getPlayer()));
            if (e.getInventory().getHolder() instanceof RacePickerMenu && RaceManager.getRace((Player) e.getPlayer()) == null && !raceMenu.getAvailableRaces().isEmpty()){
                // if the player hasn't picked a race yet, and they close the race picker menu, re-open race picker menu
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), raceMenu::open, 1L);
            } else if (e.getInventory().getHolder() instanceof ClassPickerMenu && ClassManager.getClasses((Player) e.getPlayer()).isEmpty() && !classMenu.getAvailableClasses().isEmpty()){
                // if the player hasn't picked a class yet, and they close the class picker menu, re-open class picker menu
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), classMenu::open, 1L);
            } else if (e.getInventory().getHolder() instanceof RacePickerMenu && ClassManager.getClasses((Player) e.getPlayer()).isEmpty() && !classMenu.getAvailableClasses().isEmpty()){
                // if the player has no class, and they just closed the race picker menu, open the class picker menu afterwards
                ValhallaRaces.getPlugin().getServer().getScheduler().runTaskLater(ValhallaRaces.getPlugin(), classMenu::open, 1L);
            }
        }
    }
}
