package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Screen.Screen;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class CommandListener implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args[0].equals("place")){
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
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
