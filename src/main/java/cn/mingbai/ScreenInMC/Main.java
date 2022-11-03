package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.BrowserCoreInitialization.ChromiumCore;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
        thisPlugin=Bukkit.getServer().getPluginManager().getPlugin("ScreenInMC");
        logger=thisPlugin.getLogger();
        thisPlugin.saveDefaultConfig();
        config = thisPlugin.getConfig();
        LangUtils.setLanguage(config.getString("language"));
        isEnabled=true;
        ChromiumCore core = new ChromiumCore();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                core.installCore();
            }
        };
        runnable.runTaskAsynchronously(thisPlugin);
    }
    public static Plugin thisPlugin(){
        return thisPlugin;
    }
    public static FileConfiguration getConfiguration(){
        return config;
    }
    public static Logger getPluginLogger(){
        return logger;
    }
}
