package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.perkrewards.PerkReward;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Race {
    private final String name;
    private final String chatPrefix;
    private final String displayName;
    private ItemStack icon;
    private final Material iconMaterial;
    private final List<String> description;
    private final int modelData;
    private final int guiPosition;
    private final Collection<PerkReward> perkRewards;
    private final Collection<PerkReward> antiPerkRewards;
    private final Collection<String> commands;
    private final Location citySpawn;
    private String permissionRequired = null;

    public Race(String name, String displayName, String prefix, Location citySpawn, Material icon, ItemStack trueIcon, int modelData, int guiPosition, List<String> description, String permissionRequired, Collection<String> commands, Collection<PerkReward> perkRewards, Collection<PerkReward> antiPerkRewards){
        this.name = name;
        this.chatPrefix = prefix;
        this.guiPosition = guiPosition;
        this.perkRewards = perkRewards;
        this.permissionRequired = permissionRequired;
        this.commands = commands;
        this.antiPerkRewards = antiPerkRewards;
        this.citySpawn = citySpawn;
        this.displayName = displayName;
        this.iconMaterial = icon;
        this.description = description;
        this.modelData = modelData;

        List<String> lore = new ArrayList<>();
        for (String d : description){
            lore.add(Utils.chat(d));
        }

        ItemStack i = trueIcon == null ? new ItemStack(icon) : trueIcon.clone();
        ItemMeta iMeta = i.getItemMeta();
        if (!Utils.isItemEmptyOrNull(i) && iMeta != null){
            iMeta.setDisplayName(Utils.chat(displayName));
            iMeta.setLore(lore);
            if (modelData >= 0){
                iMeta.setCustomModelData(modelData);
            }
        }
        i.setItemMeta(iMeta);
        this.icon = i;
    }

    public void setIcon(ItemStack icon) {
        List<String> lore = new ArrayList<>();
        for (String d : description){
            lore.add(Utils.chat(d));
        }

        ItemStack i = icon == null ? new ItemStack(iconMaterial) : icon.clone();
        ItemMeta iMeta = i.getItemMeta();
        if (!Utils.isItemEmptyOrNull(i) && iMeta != null){
            iMeta.setDisplayName(Utils.chat(displayName));
            iMeta.setLore(lore);
            if (modelData >= 0){
                iMeta.setCustomModelData(modelData);
            }
        }
        i.setItemMeta(iMeta);
        this.icon = i;
    }

    public List<String> getDescription() {
        return description;
    }

    public int getModelData() {
        return modelData;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public String getDisplayName() {
        return Utils.chat(displayName);
    }

    public Collection<String> getCommands() {
        return commands;
    }

    public String getPermissionRequired() {
        return permissionRequired;
    }

    public Collection<PerkReward> getAntiPerkRewards() {
        return antiPerkRewards;
    }

    public String getName() {
        return name;
    }

    public String getChatPrefix() {
        return Utils.chat(chatPrefix);
    }

    public int getGuiPosition() {
        return guiPosition;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Collection<PerkReward> getPerkRewards() {
        return perkRewards;
    }

    public Location getCitySpawn() {
        return citySpawn;
    }
}
