package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {
    private static Plugin thisPlugin;
    private static boolean isEnabled = false;
    private static Logger logger;
    private static FileConfiguration config;
    public static final String PluginFilesPath = "./plugins/ScreenInMC/";

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        ImageUtils.initImageUtils();
        thisPlugin = Bukkit.getServer().getPluginManager().getPlugin("ScreenInMC");
        logger = thisPlugin.getLogger();
        thisPlugin.saveDefaultConfig();
        config = thisPlugin.getConfig();
        LangUtils.setLanguage(config.getString("language"));
        Bukkit.getServer().getPluginCommand("screen").setExecutor(new CommandListener());
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), thisPlugin);
        isEnabled = true;
    }

    public static Plugin thisPlugin() {
        return thisPlugin;
    }

    public static FileConfiguration getConfiguration() {
        return config;
    }

    public static Logger getPluginLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        Main.logger = logger;
    }
}
