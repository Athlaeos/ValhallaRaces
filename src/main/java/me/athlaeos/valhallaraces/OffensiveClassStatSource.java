package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.statsources.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class OffensiveClassStatSource extends EvEAccumulativeStatSource {
    private final String classRequired;
    private final double value;
    public OffensiveClassStatSource(String classRequired, double value){
        this.classRequired = classRequired;
        this.value = value;
    }

    @Override
    public double add(Entity entity, Entity entity1, boolean b) {
        return 0;
    }

    @Override
    public double add(Entity entity, boolean b) {
        if (entity instanceof Player){
            Class playerClass = ClassManager.getInstance().getClass((Player) entity);
            if (playerClass != null){
                if (playerClass.getName().equals(classRequired)) return value;
            }
        }
        return 0;
    }
}
