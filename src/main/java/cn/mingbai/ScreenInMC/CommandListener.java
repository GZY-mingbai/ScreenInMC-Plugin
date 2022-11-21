package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.BrowserCoreInitializations.ChromiumCoreInitialization;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.useGPU;

public class CommandListener implements TabExecutor {
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
        }
        if (args[0].equals("download")) {
            ChromiumCoreInitialization initialization = new ChromiumCoreInitialization();
            initialization.installCore();
        }
        if (args[0].equals("plat")) {
            String[] plats= GPUDither.getPlatforms();
            for(String i:plats){
                Bukkit.broadcastMessage("Find device: "+i);
            }
        }
        if (args[0].equals("init")) {
            int[] p = ImageUtils.getPalette();
            Bukkit.broadcastMessage("Init: "+GPUDither.init(Integer.parseInt(args[1]), p,p.length/3));
        }
        if(args[0].equals("switch")){
            useGPU=!useGPU;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
