package cn.mingbai.ScreenInMC;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Plugin thisPlugin;
    private static boolean isEnabled = false;
    private static FileConfiguration config;
    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getServer().getPluginManager().getPlugin("ScreenInMC");
        config = thisPlugin.getConfig();
        isEnabled=true;
    }
    public static Plugin thisPlugin(){
        return thisPlugin;
    }
    public static FileConfiguration getConfiguration(){
        return config;
    }
}
