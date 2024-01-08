package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class RaceStatSource implements AccumulativeStatSource {
    private final String raceRequired;
    private final double value;
    public RaceStatSource(String raceRequired, double value){
        this.raceRequired = raceRequired;
        this.value = value;
    }

    @Override
    public double fetch(Entity entity, boolean b) {
        if (entity instanceof Player){
            Race race = RaceManager.getRace((Player) entity);
            if (race != null && race.getName().equals(raceRequired)) return value;
        }
        return 0;
    }
}
