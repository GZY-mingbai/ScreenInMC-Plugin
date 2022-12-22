package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.BrowserCoreInitializations.ChromiumCoreInitialization;
import cn.mingbai.ScreenInMC.BuiltInGUIs.VNCClient;
import cn.mingbai.ScreenInMC.BuiltInGUIs.test;
import cn.mingbai.ScreenInMC.BuiltInGUIs.test2;
import cn.mingbai.ScreenInMC.BuiltInGUIs.testMGUI;
import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.*;

public class CommandListener implements TabExecutor {
    Core test;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args[0].equals("place")) {
            Screen screen = new Screen(
                    new Location(
                            Bukkit.getWorld(args[1]),
                            Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4])
                    ), Screen.Facing.valueOf(args[5]),
                    Integer.parseInt(args[6]),
                    Integer.parseInt(args[7])
            );
            screen.putScreen();
            Core test = new test(args[8]);
            test.create(screen);
        }
        if (args[0].equals("place2")) {
            Screen screen = new Screen(
                    new Location(
                            Bukkit.getWorld(args[1]),
                            Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4])
                    ), Screen.Facing.valueOf(args[5]),
                    Integer.parseInt(args[6]),
                    Integer.parseInt(args[7])
            );
            screen.putScreen();
            test = new testMGUI();
            test.create(screen);
        }
        if (args[0].equals("place3")) {
            Screen screen = new Screen(
                    new Location(
                            Bukkit.getWorld(args[1]),
                            Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4])
                    ), Screen.Facing.valueOf(args[5]),
                    Integer.parseInt(args[6]),
                    Integer.parseInt(args[7])
            );
            screen.putScreen();
            test = new test2();
            test.create(screen);

        }
        if (args[0].equals("place4")) {
            Screen screen = new Screen(
                    new Location(
                            Bukkit.getWorld(args[1]),
                            Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]),
                            Integer.parseInt(args[4])
                    ), Screen.Facing.valueOf(args[5]),
                    Integer.parseInt(args[6]),
                    Integer.parseInt(args[7])
            );
            screen.putScreen();
            test = new VNCClient();
            test.create(screen);
        }
        if (args[0].equals("download")) {
            ChromiumCoreInitialization initialization = new ChromiumCoreInitialization();
            initialization.installCore();
        }
        if (args[0].equals("plat")) {
            String[] plats = GPUDither.getPlatforms();
            for (int i = 0; i < plats.length; i++) {
                Bukkit.broadcastMessage("Find device: " + i + " " + plats[i]);
            }
        }
        if (args[0].equals("init")) {
            int[] p = ImageUtils.getPalette();
            Bukkit.broadcastMessage("Init: " + GPUDither.init(Integer.parseInt(args[1]), p, p.length));
        }
        if (args[0].equals("switch")) {
            setUseOpenCL(!isUseOpenCL());
        }
        if (args[0].equals("size")) {
            pieceSize = Integer.parseInt(args[1]);
        }
        if(args[0].equals("crash")){
            ((MGUICore)test).crash();
        }
        if(args[0].equals("kb")){
            Screen.getAllScreens().get(Integer.parseInt(args[1])).getCore().onTextInput(args[2]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
