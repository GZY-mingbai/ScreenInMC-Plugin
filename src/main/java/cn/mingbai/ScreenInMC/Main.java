package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.BuiltInGUIs.ImageViewer;
import cn.mingbai.ScreenInMC.BuiltInGUIs.VNCClient;
import cn.mingbai.ScreenInMC.BuiltInGUIs.Welcome;
import cn.mingbai.ScreenInMC.BuiltInGUIs.speedTest;
import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.*;

public class Main extends JavaPlugin {
    public static final String PluginFilesPath = "./plugins/ScreenInMC/";
    private static Plugin thisPlugin;
    private static boolean isEnabled = false;
    private static Logger logger;
    private static FileConfiguration config;
    private static Gson gson = new Gson();
    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    static {
        Utils.Pair<String, String> typeArch = Utils.getSystem();
        String prefix = "screen-in-mc-" + typeArch.getKey() + "-" + typeArch.getValue();
        String suffix = Utils.getLibraryPrefix(typeArch.getKey());
        String fileName = prefix + suffix;
        try {
            File file = File.createTempFile(prefix, suffix);
            InputStream stream = Main.class.getResourceAsStream("/lib/" + fileName);
            ReadableByteChannel inputChannel = Channels.newChannel(stream);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
            fileChannel.close();
            fileOutputStream.close();
            inputChannel.close();
            stream.close();
            System.load(file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public void onDisable() {
        for(Screen i:Screen.getAllScreens()){
            if(i.getCore() instanceof MGUICore){
                MGUICore core = (MGUICore) i.getCore();
                core.unload();
            }
        }
        saveScreens();
        Item.onDisable();
    }

    @Override
    public void onEnable() {
        Core.addCore(new Welcome());
        Core.addCore(new ImageViewer());
        Core.addCore(new VNCClient());
        Core.addCore(new speedTest());
        ImageUtils.initImageUtils();
        thisPlugin = Bukkit.getServer().getPluginManager().getPlugin("ScreenInMC");
        logger = thisPlugin.getLogger();
        thisPlugin.saveDefaultConfig();
        config = thisPlugin.getConfig();
        LangUtils.setLanguage(config.getString("language"));
        Bukkit.getServer().getPluginCommand("screen").setExecutor(new CommandListener());
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), thisPlugin);
        int device=config.getInt("opencl-device");
        if(device==-3){
            String[] plats = getPlatforms();
            String[] suggestions = new String[]{"openclon","nvidia","intel(r)cpu","intel(r)opencl"};
            boolean found = false;
            for(int i=0;i<suggestions.length;i++) {
                for (int j = 0; j < plats.length; j++) {
                    String plat = plats[j].replace(" ", "").toLowerCase();
                    if(plat.startsWith(suggestions[i])){
                        device=j;
                        found=true;
                    }
                }
                if(found){
                    break;
                }
            }
            if(!found && plats.length>0){
                device = 0;
            }
            if(plats.length==0){
                device=-1;
            }
        }
        if (device >= -1) {
            ImageUtils.setUseOpenCL(true);
            int[] p = getPalette();
            if(!GPUDither.init(device, p, p.length,getPieceSize())){
                ImageUtils.setUseOpenCL(false);
            }
        }
        int pieceSize = config.getInt("piece-size");
        if(pieceSize==1||pieceSize==2||pieceSize==4||pieceSize==8||pieceSize==16){
            setPieceSize(pieceSize);
        }else{
            config.set("piece-size",4);
        }
        readScreens();
        Item.onEnable();
        isEnabled = true;
    }

    public static Gson getGson() {
        return gson;
    }
    private static final File screensFile = new File(PluginFilesPath+"screens.json");
    public static void saveScreens(){
        Screen[] screens = Screen.getAllScreens();
        Screen.ScreenData[] data = new Screen.ScreenData[screens.length];
        for(int i=0;i<screens.length;i++){
            try {
                data[i] = screens[i].getScreenData();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String json = gson.toJson(data);
        try {
            FileOutputStream outputStream = new FileOutputStream(screensFile);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream,StandardCharsets.UTF_8);
            writer.write(json);
            writer.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void readScreens(){
        if(!screensFile.exists()){
            return;
        }
        FileInputStream inputStream=null;
        InputStreamReader reader=null;
        try {
            inputStream = new FileInputStream(screensFile);
            reader = new InputStreamReader(inputStream,StandardCharsets.UTF_8);
            Screen.ScreenData[] screenData = gson.fromJson(reader,Screen.ScreenData[].class);
            for(Screen.ScreenData i :screenData) {
                if(i==null){
                    continue;
                }
                try {
                    Screen screen = new Screen(i);
                    screen.putScreen();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
       try {
           if(reader!=null){
               reader.close();
           }
           if(inputStream!=null){
               inputStream.close();
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }
    public static void sendMessage(Player player,String message){
        player.sendMessage("§6[ScreenInMC] §f"+message);
    }

}
