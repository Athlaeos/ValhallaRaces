package me.athlaeos.valhallaraces.hooks;

import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallaraces.Class;
import me.athlaeos.valhallaraces.ClassManager;
import me.athlaeos.valhallaraces.Race;
import me.athlaeos.valhallaraces.RaceManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class RacesPlaceholderExpansion extends PlaceholderExpansion {
    @Override
    public String getAuthor() {
        return "Athlaeos";
    }

    @Override
    public String getIdentifier() {
        return "valhallaraces";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player.isOnline()){
            if(params.equalsIgnoreCase("racename")){
                Race race = RaceManager.getInstance().getRace((Player) player);
                return race == null ? "" : Utils.chat(race.getDisplayName());
            } else if(params.equalsIgnoreCase("classname")){
                Class playerClass = ClassManager.getInstance().getClass((Player) player);
                return playerClass == null ? "" : Utils.chat(playerClass.getDisplayName());
            } else if(params.equalsIgnoreCase("raceprefix")){
                Race race = RaceManager.getInstance().getRace((Player) player);
                return race == null ? "" : Utils.chat(race.getChatPrefix());
            } else if(params.equalsIgnoreCase("classprefix")){
                Class playerClass = ClassManager.getInstance().getClass((Player) player);
                return playerClass == null ? "" : Utils.chat(playerClass.getChatPrefix());
            }
        }
        return null; // Placeholder is unknown by the Expansion
    }
}
