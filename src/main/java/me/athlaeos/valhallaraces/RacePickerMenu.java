package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class RacePickerMenu extends Menu {
    private static final Collection<UUID> stillInSelection = new HashSet<>();
    private static final NamespacedKey RACE_ID_KEY = new NamespacedKey(ValhallaRaces.getPlugin(), "valhallaraces_racebutton");
    private static Map<Integer, ItemStack> decorativeItems = populateDecorativeItems();
    private static String title = ConfigManager.getConfig("config.yml").get().getString("menus.races_title", "");
    private static String completed = ConfigManager.getConfig("config.yml").get().getString("confirm_message_race");
    private static int slots = Math.max(0, Math.min(54, ConfigManager.getConfig("config.yml").get().getInt("menus.races_slots", 54)));
    private Race selectedRace = null;
    private final Collection<Race> availableRaces = new HashSet<>();

    public static void reload(){
        decorativeItems = populateDecorativeItems();
        title = ConfigManager.getConfig("config.yml").get().getString("menus.races_title", "");
        completed = ConfigManager.getConfig("config.yml").get().getString("confirm_message_race");
        slots = Math.max(0, Math.min(54, ConfigManager.getConfig("config.yml").get().getInt("menus.races_slots", 54)));
    }

    public RacePickerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        for (Race r : RaceManager.getRegisteredRaces().values()){
            if (r.getPermissionRequired() != null && !playerMenuUtility.getOwner().hasPermission(r.getPermissionRequired())) continue;
            if (r.getGuiPosition() >= slots) continue;
            if (ItemUtils.isEmpty(r.getIcon())) continue;

            availableRaces.add(r);
        }
        if (!availableRaces.isEmpty()) stillInSelection.add(playerMenuUtility.getOwner().getUniqueId());
    }

    public static boolean isInSelection(Player p){
        return stillInSelection.contains(p.getUniqueId());
    }

    @Override
    public String getMenuName() {
        return Utils.chat(title);
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (ItemUtils.isEmpty(clicked)) return;
        ItemMeta clickedMeta = ItemUtils.getItemMeta(clicked);
        String clickedRace = clickedMeta.getPersistentDataContainer().get(RACE_ID_KEY, PersistentDataType.STRING);
        if (clickedRace == null) {
            if (!clickedMeta.getPersistentDataContainer().has(RaceManager.CONFIRM_BUTTON, PersistentDataType.INTEGER)) return;

            if (selectedRace == null) return;
            stillInSelection.remove(playerMenuUtility.getOwner().getUniqueId());
            RaceManager.setRace(playerMenuUtility.getOwner(), selectedRace);
            playerMenuUtility.getOwner().sendMessage(Utils.chat(completed.replace("%race%", selectedRace.getDisplayName().trim())));
            playerMenuUtility.getOwner().closeInventory();
            if (selectedRace.getCitySpawn() != null) playerMenuUtility.getOwner().teleport(selectedRace.getCitySpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            return;
        }
        Race selectedRace = RaceManager.getRegisteredRaces().get(clickedRace);
        if (selectedRace == null) return;
        this.selectedRace = selectedRace;

        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        for (int deco : decorativeItems.keySet()){
            if (deco < slots) inventory.setItem(deco, decorativeItems.get(deco));
        }

        for (Race r : RaceManager.getRegisteredRaces().values()){
            if (r.getPermissionRequired() != null && !playerMenuUtility.getOwner().hasPermission(r.getPermissionRequired())) continue;
            if (r.getGuiPosition() >= slots) continue;
            if (ItemUtils.isEmpty(r.getIcon())) continue;
            ItemStack icon = r.getIcon().clone();
            ItemMeta iconMeta = icon.getItemMeta();
            if (iconMeta == null) continue;

            iconMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS);
            iconMeta.getPersistentDataContainer().set(RACE_ID_KEY, PersistentDataType.STRING, r.getName());
            icon.setItemMeta(iconMeta);
            inventory.setItem(r.getGuiPosition(), icon);
        }
        if (selectedRace != null) inventory.setItem(RaceManager.getConfirmButtonPosition(), RaceManager.getConfirmButton());
    }

    private static Map<Integer, ItemStack> populateDecorativeItems(){
        YamlConfiguration config = ConfigManager.getConfig("config.yml").get();
        Map<Integer, ItemStack> items = new HashMap<>();
        ConfigurationSection itemSection = config.getConfigurationSection("menus.races_decoration");
        if (itemSection != null){
            for (String spot : itemSection.getKeys(false)){
                try {
                    String value = config.getString("menus.races_decoration." + spot, "");
                    int modelData = -1;
                    String[] args = value.split(";");
                    Material material = Material.valueOf(args[0]);
                    if (args.length > 1){
                        modelData = Integer.parseInt(args[1]);
                    }
                    int slot = Integer.parseInt(spot);
                    items.put(slot, new ItemBuilder(material)
                            .data(modelData)
                            .name("&r")
                            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE)
                            .get()
                    );
                } catch (IllegalArgumentException ignored){
                    ValhallaRaces.getPlugin().getServer().getLogger().warning("Invalid args given at menus.races_decoration." + spot);
                }
            }
        }
        return items;
    }

    public Collection<Race> getAvailableRaces() {
        return availableRaces;
    }
}
