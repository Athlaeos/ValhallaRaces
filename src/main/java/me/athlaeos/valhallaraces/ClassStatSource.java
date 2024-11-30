package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ClassStatSource implements AccumulativeStatSource, ClassSource {
    private final String classRequired;
    private final double value;
    public ClassStatSource(String classRequired, double value){
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
}
