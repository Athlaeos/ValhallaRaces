package me.athlaeos.valhallaraces.listener;

import me.athlaeos.valhallammo.event.ValhallaUpdatedStatsEvent;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallaraces.Class;
import me.athlaeos.valhallaraces.ClassManager;
import me.athlaeos.valhallaraces.Race;
import me.athlaeos.valhallaraces.RaceManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ValhallaLoadStatsListener implements Listener {

    @EventHandler
    public void onStatsLoad(ValhallaUpdatedStatsEvent e){
        if (!e.getLoadedProfile().equals(PowerProfile.class)) return;
        for (Class c : ClassManager.getClasses(e.getPlayer()).values()){
            for (PerkReward reward : c.getPerkRewards()) reward.apply(e.getPlayer());
        }
        Race r = RaceManager.getRace(e.getPlayer());
        if (r != null) for (PerkReward reward : r.getPerkRewards()) reward.apply(e.getPlayer());
    }
}
