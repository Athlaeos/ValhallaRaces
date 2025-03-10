package me.athlaeos.valhallaraces;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private static CooldownManager manager = null;

    private final Map<String, Map<UUID, Long>> allCooldowns = new HashMap<>();

    public static CooldownManager getInstance(){
        if (manager == null){
            manager = new CooldownManager();
        }
        return manager;
    }

    public void setCooldown(UUID player, int timems, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        allCooldowns.get(cooldownKey).put(player, System.currentTimeMillis() + timems);
    }

    public long getCooldown(UUID player, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        if (allCooldowns.get(cooldownKey).containsKey(player)){
            return allCooldowns.get(cooldownKey).get(player) - System.currentTimeMillis();
        }
        return 0;
    }

    public Map<UUID, Long> getCooldowns(String cooldownKey){
        if (allCooldowns.containsKey(cooldownKey)){
            return allCooldowns.get(cooldownKey);
        }
        return new HashMap<>();
    }

    public boolean isCooldownPassed(UUID player, String cooldownKey){
        if (!allCooldowns.containsKey(cooldownKey)) allCooldowns.put(cooldownKey, new HashMap<>());
        if (allCooldowns.get(cooldownKey).containsKey(player)){
            return allCooldowns.get(cooldownKey).get(player) <= System.currentTimeMillis();
        }
        return true;
    }
}
