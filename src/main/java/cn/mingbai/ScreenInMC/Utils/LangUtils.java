package cn.mingbai.ScreenInMC.Utils;

import cn.mingbai.ScreenInMC.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LangUtils {
    private static FileConfiguration config = null;

    public static void setLanguage(String language) {
        try {
            InputStream path = Main.class.getResourceAsStream("/lang/" + language + ".yml");
            InputStreamReader reader = new InputStreamReader(path, StandardCharsets.UTF_8);
            config = YamlConfiguration.loadConfiguration(reader);
            reader.close();
            path.close();
        } catch (Exception e) {
            throw (RuntimeException) e;
        }
    }

    public static String getText(String path) {
        if (config == null) {
            throw new RuntimeException("Language has not been set yet.");
        }
        return config.getString(path);
    }

}
