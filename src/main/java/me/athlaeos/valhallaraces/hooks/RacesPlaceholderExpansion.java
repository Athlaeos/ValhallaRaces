package me.athlaeos.valhallaraces.hooks;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallaraces.Class;
import me.athlaeos.valhallaraces.ClassManager;
import me.athlaeos.valhallaraces.Race;
import me.athlaeos.valhallaraces.RaceManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RacesPlaceholderExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getAuthor() {
        return "Athlaeos";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "valhallaraces";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player.isOnline()){
            if (params.equalsIgnoreCase("racename")){
                Race race = RaceManager.getRace((Player) player);
                return race == null ? "" : Utils.chat(race.getDisplayName());
            } else if (params.startsWith("classname_")){
                String stringGroup = params.replace("classname_", "");
                int group = Catch.catchOrElse(() -> Integer.parseInt(stringGroup), -1);
                if (group < 0) return "";
                Class playerClass = ClassManager.getClasses((Player) player).get(group);
                return playerClass == null ? "" : Utils.chat(playerClass.getDisplayName());
            } else if (params.equalsIgnoreCase("raceprefix")){
                Race race = RaceManager.getRace((Player) player);
                return race == null ? "" : Utils.chat(race.getChatPrefix());
            } else if (params.startsWith("classprefix_")){
                String stringGroup = params.replace("classprefix_", "");
                int group = Catch.catchOrElse(() -> Integer.parseInt(stringGroup), -1);
                if (group < 0) return "";
                Class playerClass = ClassManager.getClasses((Player) player).get(group);
                return playerClass == null ? "" : Utils.chat(playerClass.getChatPrefix());
            }
        }
        return null; // Placeholder is unknown by the Expansion
    }
}
