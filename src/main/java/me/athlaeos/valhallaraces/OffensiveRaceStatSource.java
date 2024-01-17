package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class OffensiveRaceStatSource implements AccumulativeStatSource, EvEAccumulativeStatSource, RaceSource {
    private final String raceRequired;
    private final double value;
    public OffensiveRaceStatSource(String raceRequired, double value){
        this.raceRequired = raceRequired;
        this.value = value;
    }

    @Override
    public double fetch(Entity entity, boolean b) {
        return 0;
    }

    @Override
    public double fetch(Entity entity, Entity entity1, boolean b) {
        if (entity1 instanceof Player){
            Race race = RaceManager.getRace((Player) entity1);
            if (race != null && race.getName().equals(raceRequired)) return value;
        }
        return 0;
    }
}
