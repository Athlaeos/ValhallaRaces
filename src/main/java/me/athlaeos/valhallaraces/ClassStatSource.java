package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.statsources.AccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ClassStatSource extends AccumulativeStatSource {
    private final String classRequired;
    private final double value;
    public ClassStatSource(String classRequired, double value){
        this.classRequired = classRequired;
        this.value = value;
    }

    @Override
    public double add(Entity entity, boolean b) {
        if (entity instanceof Player){
            Class playerClass = ClassManager.getInstance().getClass((Player) entity);
            if (playerClass != null){
                if (playerClass.getName().equals(classRequired)) {
                    return value;
                }
            }
        }
        return 0;
    }
}
