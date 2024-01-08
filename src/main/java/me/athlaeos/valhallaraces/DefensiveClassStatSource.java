package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DefensiveClassStatSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final String classRequired;
    private final double value;
    public DefensiveClassStatSource(String classRequired, double value){
        this.classRequired = classRequired;
        this.value = value;
    }

    @Override
    public double fetch(Entity entity, boolean b) {
        if (entity instanceof Player){
            Collection<Class> classes = ClassManager.getClasses((Player) entity).values();
            if (classes.stream().anyMatch(c -> c.getName().equals(classRequired))) return value;
        }
        return 0;
    }

    @Override
    public double fetch(Entity entity, Entity entity1, boolean b) {
        if (entity instanceof Player){
            Collection<Class> classes = ClassManager.getClasses((Player) entity).values();
            if (classes.stream().anyMatch(c -> c.getName().equals(classRequired))) return value;
        }
        return 0;
    }
}
