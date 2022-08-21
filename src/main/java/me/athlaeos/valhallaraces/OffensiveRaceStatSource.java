package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.statsources.AccumulativeStatSource;
import me.athlaeos.valhallammo.statsources.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class OffensiveRaceStatSource extends EvEAccumulativeStatSource {
    private final String raceRequired;
    private final double value;
    public OffensiveRaceStatSource(String raceRequired, double value){
        this.raceRequired = raceRequired;
        this.value = value;
    }

    @Override
    public double add(Entity entity, Entity entity1, boolean b) {
        return 0;
    }

    @Override
    public double add(Entity entity, boolean b) {
        if (entity instanceof Player){
            Race race = RaceManager.getInstance().getRace((Player) entity);
            if (race != null){
                if (race.getName().equals(raceRequired)) return value;
            }
        }
        return 0;
    }
}
