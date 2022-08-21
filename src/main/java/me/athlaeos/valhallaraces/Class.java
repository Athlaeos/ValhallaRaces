package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.perkrewards.PerkReward;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Class {
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
    private final Collection<String> limitedToRaces;
    private String permissionRequired = null;

    public Class(String name, String displayName, String prefix, ItemStack trueIcon, Material icon, int modelData, int guiPosition, Collection<String> limitedToRaces, Collection<String> commands, List<String> description, String permissionRequired, Collection<PerkReward> perkRewards, Collection<PerkReward> antiPerkRewards){
        this.name = name;
        this.chatPrefix = prefix;
        this.guiPosition = guiPosition;
        this.permissionRequired = permissionRequired;
        this.perkRewards = perkRewards;
        this.commands = commands;
        this.antiPerkRewards = antiPerkRewards;
        this.limitedToRaces = limitedToRaces;
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

    public List<String> getDescription() {
        return description;
    }

    public int getModelData() {
        return modelData;
    }

    public Material getIconMaterial() {
        return iconMaterial;
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

    public String getDisplayName() {
        return Utils.chat(displayName);
    }

    public Collection<String> getLimitedToRaces() {
        return limitedToRaces;
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

    public ItemStack getIcon() {
        return icon;
    }

    public int getGuiPosition() {
        return guiPosition;
    }

    public Collection<PerkReward> getPerkRewards() {
        return perkRewards;
    }
}
