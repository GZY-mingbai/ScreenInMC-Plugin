package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Browsers.Browser;
import cn.mingbai.ScreenInMC.Browsers.Chromium;
import cn.mingbai.ScreenInMC.BuiltInGUIs.*;
import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.FileUtils;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.DitheringProcessor;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.GameCodePaletteLoader;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.logging.Logger;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils.*;

public class Main extends JavaPlugin {
    public static final String PluginFilesPath = "plugins/ScreenInMC/";
    private static final File screensFile = new File(PluginFilesPath + "screens.json");
    private static Plugin thisPlugin;
    private static boolean isEnabled = false;
    private static Logger logger;
    private static FileConfiguration config;
    private static Gson gson = new Gson();
    private static Gson saveGson = new GsonBuilder().setPrettyPrinting().create();

    private static Random random = new Random();

    static {
        Utils.Pair<String, String> typeArch = Utils.getSystem();
        String prefix = "screen-in-mc-" + typeArch.getKey() + "-" + typeArch.getValue();
        String suffix = Utils.getLibraryPrefix(typeArch.getKey());
        String fileName = prefix + suffix;
        try {
            File file = File.createTempFile(prefix, suffix);
            InputStream stream = Main.class.getResourceAsStream("/lib/" + fileName);
            FileUtils.streamToFile(stream, file);
            System.load(file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Random getRandom() {
        return random;
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

    public static Gson getGson() {
        return gson;
    }

    public static void saveScreens() {
        Screen[] screens = Screen.getAllScreens();
        Screen.ScreenData[] data = new Screen.ScreenData[screens.length];
        for (int i = 0; i < screens.length; i++) {
            try {
                data[i] = screens[i].getScreenData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String json = saveGson.toJson(data);
        try {
            FileOutputStream outputStream = new FileOutputStream(screensFile);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(json);
            writer.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readScreens() {
        if (!screensFile.exists()) {
            return;
        }
        FileInputStream inputStream = null;
        InputStreamReader reader = null;
        try {
            inputStream = new FileInputStream(screensFile);
            reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Screen.ScreenData[] screenData = gson.fromJson(reader, Screen.ScreenData[].class);
            for (Screen.ScreenData i : screenData) {
                if (i == null) {
                    continue;
                }
                try {
                    Screen screen = new Screen(i);
                    screen.putScreen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (reader != null) {
                reader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage("§6[ScreenInMC] §f" + message);
    }
    public static void sendMessage(Player player, LangUtils.JsonText message) {
        LangUtils.JsonText jsonText = new LangUtils.JsonText("[ScreenInMC] ").setColor("gold").addExtra(message);
        ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(jsonText.toComponent(),false);
        CraftUtils.sendPacket(player,packet);
    }

    @Override
    public void onDisable() {
        saveScreens();
        for (Screen i : Screen.getAllScreens()) {
            i.getCore().unload();
        }
        for (Browser i : Browser.getAllBrowsers()) {
            if(i.getCoreState()==Browser.LOADED) {
                i.unloadCore();
            }
        }
        Item.onDisable();
        for(Player i :  Bukkit.getOnlinePlayers()){
            PacketListener.removeGlobalListener(i);
        }
//        Utils.unloadJars();
    }

    @Override
    public void onEnable() {
        ImageUtils.initImageUtils(new GameCodePaletteLoader().get(),new DitheringProcessor.JavaFastDitheringProcessor());
        thisPlugin = Bukkit.getServer().getPluginManager().getPlugin("ScreenInMC");
        logger = thisPlugin.getLogger();
        thisPlugin.saveDefaultConfig();
        config = thisPlugin.getConfig();
        try{
            new File(PluginFilesPath+"Files").mkdirs();
        }catch (Exception e){
            e.printStackTrace();
        }
        LangUtils.setLanguage(config.getString("language"));
        Browser.addBrowser(new Chromium());
        Core.addCore(new Welcome());
        Core.addCore(new ImageViewer());
        Core.addCore(new VNCClient());
        Core.addCore(new VideoPlayer());
        Core.addCore(new WebBrowser());
        Bukkit.getServer().getPluginCommand("screen").setExecutor(new CommandListener());
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), thisPlugin);
        int device = config.getInt("opencl-device");
        if (device == -3) {
            device = ImageUtils.getBestOpenCLDevice();
        }
        if (device >= -1) {
            DitheringProcessor.OpenCLDitheringProcessor processor = new DitheringProcessor.OpenCLDitheringProcessor();
            ImageUtils.setDitheringProcessor(processor);
            int[] p = getPalette();
            if (!GPUDither.init(device, p, p.length, getPieceSize(),ImageUtils.getOpenCLCode())) {
                ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaFastDitheringProcessor());
            }
        }
        if(device==-2){
            ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaDitheringProcessor());
        }
        if(device==-4){
            ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaFastDitheringProcessor());
        }
        int pieceSize = config.getInt("piece-size");
        if (pieceSize == 1 || pieceSize == 2 || pieceSize == 4 || pieceSize == 8 || pieceSize == 16) {
            setPieceSize(pieceSize);
        } else {
            config.set("piece-size", 4);
        }
        readScreens();
        Item.onEnable();
        isEnabled = true;
        for(Player i:Bukkit.getOnlinePlayers()){
            PacketListener.addGlobalListener(i);
        }
    }
}
