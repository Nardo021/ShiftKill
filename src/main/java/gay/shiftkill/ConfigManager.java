package gay.shiftkill;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            // 如果配置文件不存在，创建一个默认的配置文件
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            setupConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config != null && configFile != null) {
            try {
                getConfig().save(configFile);
            } catch (IOException e) {
                plugin.getLogger().warning("无法保存配置文件.");
            }
        }
    }

    public void setDefaultConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void setDropMaterial(int itemID) {
        Material dropMaterial = itemID == 2 ? Material.END_ROD : Material.MILK_BUCKET;
        getConfig().set("item", itemID);
        saveConfig();
    }

    public Material getDropMaterial() {
        int itemID = getConfig().getInt("item", 1);
        return itemID == 2 ? Material.END_ROD : Material.MILK_BUCKET;
    }
}
