package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class OffensiveClassStatSource implements AccumulativeStatSource, EvEAccumulativeStatSource, ClassSource {
    private final String classRequired;
    private final double value;
    public OffensiveClassStatSource(String classRequired, double value){
        this.classRequired = classRequired;
        this.value = value;
    }

    @Override
    public double fetch(Entity entity, Entity entity1, boolean b) {
        if (entity1 instanceof Player){
            Collection<Class> classes = ClassManager.getClasses((Player) entity1).values();
            if (classes.stream().anyMatch(c -> c.getName().equals(classRequired))) return value;
        }
        return 0;
    }

    @Override
    public double fetch(Entity entity, boolean b) {
        return 0;
    }
}
