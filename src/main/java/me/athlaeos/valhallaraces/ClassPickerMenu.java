package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.menus.Menu;
import me.athlaeos.valhallammo.menus.PlayerMenuUtility;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallaraces.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class ClassPickerMenu extends Menu {
    private static final NamespacedKey classIDKey = new NamespacedKey(ValhallaRaces.getPlugin(), "valhallaraces_classbutton");
    private static final Map<Integer, ItemStack> decorativeItems = populateDecorativeItems();
    private static final String title = initializeTitle();
    private static final String warning = initializeWarningMessage();
    private static final String completed = initializeConfirmationMessage();
    private static final int slots = initializeGuiSize();
    private Class preConfirmClass = null;
    boolean hasClassesAvailable = false;

    public ClassPickerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        for (Class r : ClassManager.getInstance().getRegisteredClasses().values()){
            if (r.getPermissionRequired() != null){
                if (!playerMenuUtility.getOwner().hasPermission(r.getPermissionRequired())) continue;
            }
            if (!r.getLimitedToRaces().isEmpty()){
                Race race = RaceManager.getInstance().getRace(playerMenuUtility.getOwner());
                if (race == null || !r.getLimitedToRaces().contains(race.getName())) continue;
            }
            if (r.getGuiPosition() >= slots) continue;

            if (Utils.isItemEmptyOrNull(r.getIcon())) continue;
            ItemStack icon = r.getIcon().clone();
            ItemMeta iconMeta = icon.getItemMeta();
            if (iconMeta == null) continue;
            hasClassesAvailable = true;
            break;
        }
    }

    public boolean hasClassesAvailable() {
        return hasClassesAvailable;
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
        if (!decorativeItems.containsKey(e.getSlot())) {
            Class clickedClass = getClickedClass(e.getCurrentItem());
            if (clickedClass != null){
                if (preConfirmClass == null || !preConfirmClass.equals(clickedClass)){
                    preConfirmClass = clickedClass;
                    setMenuItems();
                    setItemName(inventory.getItem(e.getSlot()), warning.replace("%class%", Utils.getItemName(clickedClass.getIcon())));
                } else {
                    ClassManager.getInstance().setClass(playerMenuUtility.getOwner(), clickedClass);
                    playerMenuUtility.getOwner().sendMessage(Utils.chat(completed.replace("%class%", Utils.getItemName(clickedClass.getIcon()))));
                    playerMenuUtility.getOwner().closeInventory();
                }
            }
        }
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    private Class getClickedClass(ItemStack i){
        if (Utils.isItemEmptyOrNull(i)) return null;
        ItemMeta iMeta = i.getItemMeta();
        if (iMeta == null) return null;
        if (iMeta.getPersistentDataContainer().has(classIDKey, PersistentDataType.STRING)){
            String value = iMeta.getPersistentDataContainer().get(classIDKey, PersistentDataType.STRING);
            if (value == null) return null;
            return ClassManager.getInstance().getRegisteredClasses().get(value);
        } else return null;
    }

    private void setItemName(ItemStack i, String name){
        if (Utils.isItemEmptyOrNull(i)) return;
        ItemMeta iMeta = i.getItemMeta();
        if (iMeta == null) return;
        iMeta.setDisplayName(Utils.chat(name));
        i.setItemMeta(iMeta);
    }

    @Override
    public void setMenuItems() {
        for (int deco : decorativeItems.keySet()){
            if (deco < slots){
                inventory.setItem(deco, decorativeItems.get(deco));
            }
        }

        for (Class r : ClassManager.getInstance().getRegisteredClasses().values()){
            if (r.getPermissionRequired() != null){
                if (!playerMenuUtility.getOwner().hasPermission(r.getPermissionRequired())) continue;
            }
            if (!r.getLimitedToRaces().isEmpty()){
                Race race = RaceManager.getInstance().getRace(playerMenuUtility.getOwner());
                if (race == null || !r.getLimitedToRaces().contains(race.getName())) continue;
            }
            if (r.getGuiPosition() >= slots) continue;

            if (Utils.isItemEmptyOrNull(r.getIcon())) continue;
            ItemStack icon = r.getIcon().clone();
            ItemMeta iconMeta = icon.getItemMeta();
            if (iconMeta == null) continue;
            iconMeta.getPersistentDataContainer().set(classIDKey, PersistentDataType.STRING, r.getName());
            icon.setItemMeta(iconMeta);
            inventory.setItem(r.getGuiPosition(), icon);
        }
    }

    private static Map<Integer, ItemStack> populateDecorativeItems(){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("config.yml").get();
        Map<Integer, ItemStack> items = new HashMap<>();
        ConfigurationSection itemSection = config.getConfigurationSection("menus.classes_decoration");
        if (itemSection != null){
            for (String spot : itemSection.getKeys(false)){
                try {
                    String value = config.getString("menus.classes_decoration." + spot, "");
                    int modelData = -1;
                    String[] args = value.split(";");
                    Material material = Material.valueOf(args[0]);
                    if (args.length > 1){
                        modelData = Integer.parseInt(args[1]);
                    }
                    int slot = Integer.parseInt(spot);
                    items.put(slot, modelData >= 0 ? Utils.setCustomModelData(new ItemStack(material), modelData) : new ItemStack(material));
                } catch (IllegalArgumentException ignored){
                    ValhallaRaces.getPlugin().getServer().getLogger().warning("Invalid args given at menus.classes_decoration." + spot);
                }
            }
        }
        return items;
    }

    private static String initializeTitle(){
        return ConfigManager.getInstance().getConfig("config.yml").get().getString("menus.classes_title", "");
    }

    private static int initializeGuiSize(){
        return ConfigManager.getInstance().getConfig("config.yml").get().getInt("menus.classes_slots", 54);
    }

    private static String initializeWarningMessage(){
        return ConfigManager.getInstance().getConfig("config.yml").get().getString("warning_message_class");
    }

    private static String initializeConfirmationMessage(){
        return ConfigManager.getInstance().getConfig("config.yml").get().getString("confirm_message_class");
    }
}
