package me.athlaeos.valhallaraces;

import me.athlaeos.valhallammo.dom.Catch;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class ClassPickerMenu extends Menu {
    private static final Collection<UUID> stillInSelection = new HashSet<>();
    private static final NamespacedKey CLASS_ID_KEY = new NamespacedKey(ValhallaRaces.getPlugin(), "valhallaraces_classbutton");
    private static Map<Integer, ItemStack> decorativeItems = populateDecorativeItems();
    private static String title = ConfigManager.getConfig("config.yml").get().getString("menus.classes_title", "");
    private static String completed = ConfigManager.getConfig("config.yml").get().getString("confirm_message_class");
    private static int slots = ConfigManager.getConfig("config.yml").get().getInt("menus.classes_slots", 54);
    private final Map<Integer, Class> pickedClasses = new HashMap<>();
    private final Map<Integer, Collection<Class>> pickableClasses = new HashMap<>();

    public static void reload(){
        decorativeItems = populateDecorativeItems();
        title = ConfigManager.getConfig("config.yml").get().getString("menus.classes_title", "");
        completed = ConfigManager.getConfig("config.yml").get().getString("confirm_message_class");
        slots = ConfigManager.getConfig("config.yml").get().getInt("menus.classes_slots", 54);
    }

    public ClassPickerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        for (Class c : ClassManager.getRegisteredClasses().values()){
            if (c.getPermissionRequired() != null && !playerMenuUtility.getOwner().hasPermission(c.getPermissionRequired())) continue;
            if (!c.getLimitedToRaces().isEmpty()){
                Race race = RaceManager.getRace(playerMenuUtility.getOwner());
                if (race == null || !c.getLimitedToRaces().contains(race.getName())) continue;
            }
            if (c.getGuiPosition() >= slots || ItemUtils.isEmpty(c.getIcon())) continue;
            Collection<Class> existingClasses = pickableClasses.getOrDefault(c.getGroup(), new HashSet<>());
            existingClasses.add(c);
            pickableClasses.put(c.getGroup(), existingClasses);
        }
        if (!pickableClasses.isEmpty()) stillInSelection.add(playerMenuUtility.getOwner().getUniqueId());
    }

    public static boolean isInSelection(Player p){
        return stillInSelection.contains(p.getUniqueId());
    }

    public Map<Integer, Collection<Class>> getAvailableClasses() {
        return pickableClasses;
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
        String clickedClass = clickedMeta.getPersistentDataContainer().get(CLASS_ID_KEY, PersistentDataType.STRING);
        if (clickedClass == null) {
            if (!clickedMeta.getPersistentDataContainer().has(ClassManager.CONFIRM_BUTTON, PersistentDataType.INTEGER)) return;
            if (pickedClasses.size() != pickableClasses.size()) return; // player hasn't picked a class in each available group yet
            stillInSelection.remove(playerMenuUtility.getOwner().getUniqueId());
            ClassManager.setClasses(playerMenuUtility.getOwner(), pickedClasses.values());
            playerMenuUtility.getOwner().sendMessage(Utils.chat(completed.replace("%class%",
                    pickedClasses.values().stream().map((c) -> {
                        ItemMeta meta = ItemUtils.getItemMeta(c.getIcon());
                        if (meta == null) return "";
                        return ItemUtils.getItemName(meta).trim();
                    }).collect(Collectors.joining(", ")))
            ));
            playerMenuUtility.getOwner().closeInventory();
            return;
        }
        Class selectedClass = ClassManager.getRegisteredClasses().get(clickedClass);
        if (selectedClass == null) return;
        pickedClasses.put(selectedClass.getGroup(), selectedClass);

        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        for (int deco : decorativeItems.keySet()){
            if (deco < slots){
                inventory.setItem(deco, decorativeItems.get(deco));
            }
        }

        for (Integer group : pickableClasses.keySet()){
            for (Class c : pickableClasses.get(group)){
                if (c.getGuiPosition() >= slots) continue;
                boolean unlocked = pickedClasses.get(group) == null || pickedClasses.get(group).equals(c);
                ItemBuilder icon = new ItemBuilder(unlocked ? c.getIcon() : c.getLockedIcon());

                icon.flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS);
                icon.stringTag(CLASS_ID_KEY, c.getName());
                inventory.setItem(c.getGuiPosition(), icon.get());
            }
        }

        if (pickedClasses.size() == pickableClasses.size()) inventory.setItem(ClassManager.getConfirmButtonPosition(), ClassManager.getConfirmButton());
    }

    private static Map<Integer, ItemStack> populateDecorativeItems(){
        YamlConfiguration config = ConfigManager.getConfig("config.yml").get();
        Map<Integer, ItemStack> items = new HashMap<>();
        ConfigurationSection itemSection = config.getConfigurationSection("menus.classes_decoration");
        if (itemSection != null){
            for (String spot : itemSection.getKeys(false)){
                Integer slot = Catch.catchOrElse(() -> Integer.parseInt(spot), null);
                if (slot == null){
                    ValhallaRaces.getPlugin().getServer().getLogger().warning("Invalid slot given at menus.classes_decoration." + spot);
                    continue;
                }
                String value = config.getString("menus.classes_decoration." + spot, "");
                int data = Catch.catchOrElse(() -> Integer.valueOf(value.split(":")[1]), -1);
                Material material = Catch.catchOrElse(() -> Material.valueOf(value.split(":")[0]), null);
                if (material == null){
                    ValhallaRaces.getPlugin().getServer().getLogger().warning("Invalid material given at menus.classes_decoration." + spot);
                    continue;
                }
                items.put(slot, new ItemBuilder(material).data(data).name("&r").get());
            }
        }
        return items;
    }
}
