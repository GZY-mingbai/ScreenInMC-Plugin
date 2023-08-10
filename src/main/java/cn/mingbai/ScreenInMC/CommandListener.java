package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Browsers.Chromium;
import cn.mingbai.ScreenInMC.BuiltInGUIs.WebBrowser;
import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils.*;

public class CommandListener implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args[0].equalsIgnoreCase("putScreen")) {
            try {
                if (args.length != 9) {
                    sender.sendMessage("Failed");
                    return true;
                }
                Core core = null;
                for (Core i : Core.getAllCore()) {
                    if (i.getCoreName().equalsIgnoreCase(args[8])) {
                        core = (Core) i.clone();
                        break;
                    }
                }
                if (core == null) {
                    sender.sendMessage("Failed");
                    return true;
                }
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
                screen.setCore(core);
                screen.putScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (args[0].equalsIgnoreCase("removeScreen")) {
            Screen screen = null;
            try{
                screen = Screen.getScreenFromUUID(UUID.fromString(args[1]));
            }catch (Exception e){
                e.printStackTrace();
            }
            if (screen!=null) {
                if (args.length == 2) {
                    Screen.removeScreen(screen);
                    sender.sendMessage("Success");
                } else {
                    sender.sendMessage("Failed");
                }
            } else {
                sender.sendMessage("Failed");
            }
        }
        if (args[0].equalsIgnoreCase("listDevices")) {
            String[] platforms = getOpenCLPlatforms();
            for (int i = 0; i < platforms.length; i++) {
                sender.sendMessage("Find device: " + i + " " + platforms[i]);
            }
        }
        if (args[0].equalsIgnoreCase("initOpenCL")) {
            int[] p = getPalette();
            if (GPUDither.init(Integer.parseInt(args[1]), p, p.length, getPieceSize(),ImageUtils.getOpenCLCode())) {
                sender.sendMessage("Success");
            } else {
                sender.sendMessage("Failed");
            }
        }
        if (args[0].equalsIgnoreCase("setPieceSize")) {
            setPieceSize(Integer.parseInt(args[1]));
            sender.sendMessage("Success");
        }
        if (args[0].equalsIgnoreCase("browser")) {
            Screen screen = null;
            try{
                screen = Screen.getScreenFromUUID(UUID.fromString(args[1]));
            }catch (Exception e){
                e.printStackTrace();
            }
            if(screen!=null&&screen.getCore() instanceof WebBrowser) {
                if (args[2].equalsIgnoreCase("openurl")) {
                    ((WebBrowser) screen.getCore()).getBrowser().openURL(screen, args[3]);
                } else if (args[2].equalsIgnoreCase("refresh")) {
                    ((WebBrowser) screen.getCore()).getBrowser().refreshPage(screen);
                }
            }
        }
        if (args[0].equalsIgnoreCase("input")) {
            Screen screen = null;
            try{
                screen = Screen.getScreenFromUUID(UUID.fromString(args[1]));
            }catch (Exception e){
                e.printStackTrace();
            }
            if (screen!=null) {
                if (args.length == 3) {
                    screen.getCore().onTextInput(args[2]);
                } else {
                    screen.getCore().onTextInput("");
                }
                sender.sendMessage("Success");
            } else {
                sender.sendMessage("Failed");
            }
        }
        if (args[0].equalsIgnoreCase("controller")) {
            if(args.length>=3){
                for(Screen i:Screen.getAllScreens()){
                    if(i.getEditGUI().getOpenedPlayer().equals(sender)){
                        String[] newArgs = Arrays.copyOfRange(args,2,args.length);
                        UUID uuid = UUID.fromString(args[1]);
                        Function<String,Boolean> function = i.getEditGUI().getControllerCommandCallback(uuid);
                        try {
                            function.apply(String.join(" ",newArgs));
                        }catch (Exception e){
                            i.getEditGUI().removeControllerCommandCallback(uuid);
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                if (sender instanceof Player) {
                    Item.giveItem(((Player) sender));
                }
            }
        }
        if (args[0].equalsIgnoreCase("installChromium")) {
            new Thread(){
                @Override
                public void run() {
                    new Chromium().installCore();
                }
            }.start();
        }
        sender.sendMessage("Success");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp()) {
            if (args.length == 1) {
                String[] sub1 = {"initOpenCL", "setPieceSize", "listDevices", "putScreen", "input", "removeScreen", "controller", "browser"};
                return Arrays.stream(sub1).filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            List<String> sub2 = new ArrayList<>();
            if (args.length == 2) {
                switch (args[0]) {
                    case "initOpenCL":
                        for (int i = 0; i < getOpenCLPlatforms().length; i++) {
                            sub2.add(Integer.toString(i));
                        }
                        break;
                    case "setPieceSize":
                        sub2.add("1");
                        sub2.add("2");
                        sub2.add("4");
                        sub2.add("8");
                        sub2.add("16");
                        break;
                    case "input":
                    case "removeScreen":
                        for (Screen i:Screen.getAllScreens()) {
                            sub2.add(i.getUUID().toString());
                        }
                        break;
                    case "browser":
                        for (Screen i:Screen.getAllScreens()) {
                            if (i.getCore() instanceof WebBrowser) {
                                sub2.add(i.getUUID().toString());
                            }
                        }
                        break;
                    case "putScreen":
                        for (World i : Bukkit.getWorlds()) {
                            sub2.add(i.getName());
                        }
                        break;
                }
            }
            if (args.length == 3 || args.length == 4 || args.length == 5) {
                if (args[0].equalsIgnoreCase("browser")&&args.length == 3) {
                    sub2.add("openurl");
                    sub2.add("refresh");
                }
                if (args[0].equalsIgnoreCase("putScreen")) {
                    if (sender instanceof Player) {
                        switch (args.length) {
                            case 3:
                                sub2.add(String.valueOf(((Player) sender).getLocation().getBlockX()));
                                break;
                            case 4:
                                sub2.add(String.valueOf(((Player) sender).getLocation().getBlockY()));
                                break;
                            case 5:
                                sub2.add(String.valueOf(((Player) sender).getLocation().getBlockZ()));
                                break;
                        }
                    } else {
                        sub2.add("0");
                    }
                }
            }
            if (args.length == 6) {
                if (args[0].equalsIgnoreCase("putScreen")) {
                    for (Screen.Facing i : Screen.Facing.values()) {
                        sub2.add(i.toString());
                    }
                }
            }
            if (args.length == 9) {
                if (args[0].equalsIgnoreCase("putScreen")) {
                    for (Core i : Core.getAllCore()) {
                        sub2.add(i.getCoreName());
                    }
                }
            }
            return Arrays.stream(sub2.toArray(new String[0])).filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());

        }
        return null;
    }
}
