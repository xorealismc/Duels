package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class KitManager {
    private final XorealisDuels plugin;

    public KitManager(XorealisDuels plugin) {
        this.plugin = plugin;
    }

    public void loadKits() {
        kitTemplates.clear();
        File kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            plugin.getLogger().warning("Kits file not found");
            return;
        }

        FileConfiguration kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);

        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
        if (kitsSection == null) {
            plugin.getLogger().warning("Kits section not found");
            return;
        }
        for (String kitId : kitsSection.getKeys(false)) {
            ConfigurationSection currentKitSection = kitsSection.getConfigurationSection(kitId);
            if (currentKitSection == null) continue;

            try {
                String displayName = ChatColor.translateAlternateColorCodes('&', currentKitSection.getString("display-name", "&cUnnamed Kit"));

                Material iconMaterial = Material.matchMaterial(currentKitSection.getString("icon-material", "STONE"));
                ItemStack icon = new ItemStack(iconMaterial != null ? iconMaterial : Material.BARRIER);


                ItemStack[] inventoryContents = parseInventoryContents(currentKitSection.getConfigurationSection("items"));
                ItemStack[] armorContents = parseArmorContents(currentKitSection.getConfigurationSection("armor"));

                Kit kit = new Kit(kitId, displayName, icon, inventoryContents, armorContents);
                kitTemplates.put(kitId, kit);
                plugin.getLogger().info("Успешно загружен кит-шаблон: " + kitId);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка при загрузке кита с ID: " + kitId, e);
            }
        }
    }
    private ItemStack[] parseInventoryContents(ConfigurationSection itemsSection) {
        ItemStack[] contents = new ItemStack[36];

        if (itemsSection == null) return contents; // Возвращаем пустой инвентарь, если секции нет

        for (String slotKey : itemsSection.getKeys(false)) {
            try {
                int slot = Integer.parseInt(slotKey);
                if (slot >= 0 && slot < contents.length) {
                    ConfigurationSection itemData = itemsSection.getConfigurationSection(slotKey);
                    contents[slot] = parseItemStack(itemData);
                } else {
                    plugin.getLogger().warning("Неверный слот '" + slotKey + "' в ките. Слот должен быть от 0 до 35.");
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Неверный ключ слота '" + slotKey + "' в ките. Используйте только числа.");
            }
        }
        return contents;
    }


    private ItemStack[] parseArmorContents(ConfigurationSection armorSection) {
        ItemStack[] armor = new ItemStack[4];

        if (armorSection == null) return armor;

        armor[0] = parseItemStack(armorSection.getConfigurationSection("boots"));
        armor[1] = parseItemStack(armorSection.getConfigurationSection("leggings"));
        armor[2] = parseItemStack(armorSection.getConfigurationSection("chestplate"));
        armor[3] = parseItemStack(armorSection.getConfigurationSection("helmet"));

        return armor;
    }


    private ItemStack parseItemStack(ConfigurationSection itemSection) {
        if (itemSection == null) return null;

        String materialName = itemSection.getString("material");
        if (materialName == null) return null;

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Неизвестный материал: " + materialName);
            return null;
        }

        int amount = itemSection.getInt("amount", 1);
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            List<String> enchantments = itemSection.getStringList("enchantments");
            for (String enchString : enchantments) {
                String[] parts = enchString.split(":");
                if (parts.length == 2) {
                    Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                    if (enchantment != null) {
                        try {
                            int level = Integer.parseInt(parts[1]);
                            meta.addEnchant(enchantment, level, true);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            item.setItemMeta(meta);
        }

        return item;
    }


    public Optional<Kit> getKitTemplate(String id) {
        return Optional.ofNullable(kitTemplates.get(id));
    }


    public Collection<Kit> getAllKitTemplates() {
        return Collections.unmodifiableCollection(kitTemplates.values());
    }
}
