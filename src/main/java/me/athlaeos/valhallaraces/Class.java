package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class Class {
    private final String name;
    private final String displayName;
    private final String chatPrefix;
    private ItemStack icon;
    private ItemStack lockedIcon;
    private final int guiPosition;
    private final Collection<PerkReward> perkRewards;
    private final Collection<String> commands;
    private final Collection<String> limitedToRaces;
    private final int group;
    private final String permissionRequired;

    public Class(String name, String prefix, ItemStack icon, ItemStack lockedIcon, int guiPosition, int group, Collection<String> limitedToRaces, Collection<String> commands, String permissionRequired, Collection<PerkReward> perkRewards){
        this.name = name;
        this.chatPrefix = prefix;
        this.guiPosition = guiPosition;
        this.permissionRequired = permissionRequired;
        this.perkRewards = perkRewards;
        this.commands = commands;
        this.limitedToRaces = limitedToRaces;
        this.icon = icon;
        this.lockedIcon = lockedIcon;
        this.group = group;

        this.displayName = ItemUtils.getItemName(ItemUtils.getItemMeta(icon)).trim();
    }

    public Collection<String> getLimitedToRaces() { return limitedToRaces; }
    public Collection<String> getCommands() { return commands; }
    public String getPermissionRequired() { return permissionRequired; }
    public String getName() { return name; }
    public String getChatPrefix() { return Utils.chat(chatPrefix); }
    public ItemStack getIcon() { return icon; }
    public int getGuiPosition() { return guiPosition; }
    public Collection<PerkReward> getPerkRewards() { return perkRewards; }
    public ItemStack getLockedIcon() { return lockedIcon; }
    public int getGroup() { return group; }
    public void setIcon(ItemStack icon) { this.icon = icon; }
    public void setLockedIcon(ItemStack lockedIcon) { this.lockedIcon = lockedIcon; }
    public String getDisplayName() { return displayName; }
}
