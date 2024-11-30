package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class Race {
    private final String name;
    private final String displayName;
    private final String chatPrefix;
    private ItemStack icon;
    private ItemStack lockedIcon;
    private final int guiPosition;
    private final Collection<PerkReward> perkRewards;
    private final Collection<String> commands;
    private final Collection<String> undoCommands;
    private final Location citySpawn;
    private final String permissionRequired;

    public Race(String name, String prefix, Location citySpawn, ItemStack icon, ItemStack lockedIcon, int guiPosition, String permissionRequired, Collection<String> commands, Collection<String> undoCommands, Collection<PerkReward> perkRewards){
        this.name = name;
        this.chatPrefix = prefix;
        this.guiPosition = guiPosition;
        this.perkRewards = perkRewards;
        this.permissionRequired = permissionRequired;
        this.commands = commands;
        this.undoCommands = undoCommands;
        this.icon = icon;
        this.lockedIcon = lockedIcon;
        this.citySpawn = citySpawn;

        this.displayName = ItemUtils.getItemName(ItemUtils.getItemMeta(icon)).trim();
    }

    public Collection<String> getCommands() { return commands; }
    public Collection<String> getUndoCommands() {return undoCommands;}
    public String getPermissionRequired() { return permissionRequired; }
    public String getName() { return name; }
    public String getChatPrefix() { return Utils.chat(chatPrefix); }
    public int getGuiPosition() { return guiPosition; }
    public ItemStack getIcon() { return icon; }
    public Collection<PerkReward> getPerkRewards() { return perkRewards; }
    public Location getCitySpawn() { return citySpawn; }
    public ItemStack getLockedIcon() { return lockedIcon; }
    public void setIcon(ItemStack icon) { this.icon = icon; }
    public void setLockedIcon(ItemStack lockedIcon) { this.lockedIcon = lockedIcon; }
    public String getDisplayName() { return displayName; }
}
