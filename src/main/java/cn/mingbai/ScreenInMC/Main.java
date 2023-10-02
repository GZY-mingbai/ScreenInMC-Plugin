package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Browsers.Browser;
import cn.mingbai.ScreenInMC.Browsers.Chromium;
import cn.mingbai.ScreenInMC.BuiltInGUIs.*;
import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.*;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.OutSystemMessagePacket;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.PacketListener;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ConfigPaletteLoader;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.DitheringProcessor;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.JSONUtils.JSONUtils;
import cn.mingbai.ScreenInMC.Utils.JSONUtils.JSONUtils.JSONArray;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.logging.Logger;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils.*;

public class Main extends JavaPlugin {
    public static int nowVersion = 4;
    public static int defaultFrameRateLimit = 18;
    public static int renderDistanceLimit = 32;
    public static final String PluginFilesPath = "plugins/ScreenInMC/";
    private static final File screensFile = new File(PluginFilesPath + "screens.json");
    private static Plugin thisPlugin;
    private static boolean isEnabled = false;
    private static Logger logger;
    private static FileConfiguration config;
    private static JSONUtils json = JSONUtils.create();
    private static JSONUtils saveJson = JSONUtils.create(true);

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
            file.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e){
            e.printStackTrace();
        }catch (Throwable e){
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

    public static JSONUtils getJSONUtils() {
        return json;
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
        String json = saveJson.toJson(JSONUtils.JSON.create(data));
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
            String text = new String(IOUtils.readInputStream(inputStream),StandardCharsets.UTF_8);
            Screen.ScreenData[] screenData = (Screen.ScreenData[]) json.fromJson(text).write(Screen.ScreenData[].class);
            for (Screen.ScreenData i : screenData) {
                if (i == null) {
                    continue;
                }
                try {
                    Screen screen = new Screen(i);
                    if(screen.getCore()==null) continue;
                    try{
                        screen.putScreen();
                    }catch (Screen.FacingNotSupportedException e){
                        getPluginLogger().warning("The screen ("+screen.getUUID()+") cannot be placed, because placing it in the "+e.getFacing().name()+" direction is not supported in versions below 1.12.2.");
                    }catch (Exception e){

                    }
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

    public static void sendMessage(CommandSender player, String message) {
        player.sendMessage("§6[ScreenInMC] §f" + message);
    }
    public static void sendMessage(CommandSender player, LangUtils.JsonText message) {
        LangUtils.JsonText jsonText = new LangUtils.JsonText("[ScreenInMC] ").setColor("gold").addExtra(message);
        if(player instanceof Player) {
            Object packet = OutSystemMessagePacket.create(jsonText);
            CraftUtils.sendPacket((Player) player, packet);
        }else{
            player.sendMessage(jsonText.toRichString());
        }
    }

    @Override
    public void onDisable() {
        saveScreens();
        for (Screen i : Screen.getAllScreens()) {
            i.disableScreen();
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
        thisPlugin = Bukkit.getServer().getPluginManager().getPlugin("ScreenInMC");
        logger = thisPlugin.getLogger();
        ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
            @Override
            public void run() {
                String version ="";
                try {
                    version=FileUtils.getString("https://raw.githubusercontent.com/GZY-mingbai/ScreenInMC-Plugin/master/.github/version.yml","");
                }catch (Throwable e){
                    logger.warning("检查更新失败: "+e.getMessage());
                }
                if(version==null||version.length()==0) return;
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(version.getBytes(StandardCharsets.UTF_8));
                    InputStreamReader reader =new InputStreamReader(inputStream,StandardCharsets.UTF_8);
                    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
                    int latest = configuration.getInt("latest-version");
                    if(latest>nowVersion){
                        logger.warning("检查到插件更新: 最新版本: "+latest+" 当前版本: "+nowVersion);
                    }else {
                        logger.info("未检查到更新, 当前版本为最新版本");
                    }
                    reader.close();
                    inputStream.close();
                }catch (Exception e){
                    logger.warning("检查更新失败: "+e.getMessage());
                }
            }
        };
        runnable.runTaskAsynchronously(thisPlugin());
        try {
            CraftUtils.init();
        } catch (Exception e) {
            e.printStackTrace();
            getPluginLogger().warning("ScreenInMC load failed.");
            return;
        }
        ImageUtils.initImageUtils(new ConfigPaletteLoader().get(),new DitheringProcessor.JavaFastDitheringProcessor());
        thisPlugin.saveDefaultConfig();
        config = thisPlugin.getConfig();
        defaultFrameRateLimit = config.getInt("default-fps-limit");
        renderDistanceLimit = config.getInt("render-distance-limit");
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
        EventListener.init();
        int device = config.getInt("opencl-device");
        if (device == -3) {
            try {
                device = ImageUtils.getBestOpenCLDevice();
            }catch (Exception e){
                ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaFastDitheringProcessor());
            }
        }
        if (device >= -1) {
            DitheringProcessor.OpenCLDitheringProcessor processor = new DitheringProcessor.OpenCLDitheringProcessor();
            ImageUtils.setDitheringProcessor(processor);
            int[] p = getPalette();
            try {
                if (!GPUDither.init(device, p, p.length, getPieceSize(),ImageUtils.getOpenCLCode())) {
                    ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaFastDitheringProcessor());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }catch (Error e){
                e.printStackTrace();
            }catch (Throwable e){
                e.printStackTrace();
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
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), thisPlugin);
    }
}
