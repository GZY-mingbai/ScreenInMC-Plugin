package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Browsers.Chromium;
import cn.mingbai.ScreenInMC.BuiltInGUIs.WebBrowser;
import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils.JsonText;
import cn.mingbai.ScreenInMC.Utils.Utils;
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

import static cn.mingbai.ScreenInMC.Main.getPluginLogger;
import static cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils.*;

public class CommandListener implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {

            if (args[0].equalsIgnoreCase("putScreen")) {
                try {
                    if (args.length != 9) {
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-arguments-count-error")
                                        .replace("%%", "8"))
                                .setColor("red")
                        );
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
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-core-not-found")
                                        .replace("%%", args[8]))
                                .setColor("red")
                        );
                        return true;
                    }
                    Screen screen = null;
                    try {
                        screen = new Screen(
                                new Location(
                                        Bukkit.getWorld(args[1]),
                                        Integer.parseInt(args[2]),
                                        Integer.parseInt(args[3]),
                                        Integer.parseInt(args[4])
                                ), Screen.Facing.valueOf(args[5]),
                                Integer.parseInt(args[6]),
                                Integer.parseInt(args[7])
                        );
                    } catch (NumberFormatException e) {
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-number")
                                        .replace("%%", e.getMessage()))
                                .setColor("red")
                        );
                        return true;
                    }
                    if (screen == null) return true;
                    screen.setCore(core);
                    try {
                        screen.putScreen();
                    } catch (Screen.FacingNotSupportedException exception) {
                        getPluginLogger().warning("The screen (" + screen.getUUID() + ") cannot be placed, because placing it in the " + exception.getFacing().name() + " direction is not supported in versions below 1.12.2.");
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-screen-facing-not-supported")
                                        .replace("%%", exception.getFacing().getTranslatedFacingName()))
                                .setColor("red")
                        );
                        return true;
                    }
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-success-put-screen")
                                    .replace("%%", screen.getUUID().toString()))
                            .setColor("green")
                    );

                } catch (Exception e) {
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-error-unknown")
                                    .replace("%%", e.getMessage()))
                            .setColor("red")
                    );
                    e.printStackTrace();
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("removeScreen")) {
                if (args.length == 2) {
                    Screen screen = null;
                    try {
                        screen = Screen.getScreenFromUUID(UUID.fromString(args[1]));
                    } catch (Exception e) {
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-unknown")
                                        .replace("%%", e.getMessage()))
                                .setColor("red")
                        );
                        e.printStackTrace();
                    }
                    if (screen != null) {
                        Screen.removeScreen(screen);
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-success-remove-screen")
                                        .replace("%%", args[1]))
                                .setColor("green")
                        );
                    } else {
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-screen-not-found")
                                        .replace("%%", args[1]))
                                .setColor("red")
                        );
                    }
                } else {
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-error-arguments-count-error")
                                    .replace("%%", "1"))
                            .setColor("red")
                    );
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("listDevices")) {
                String[] platforms = getOpenCLPlatforms();
                for (int i = 0; i < platforms.length; i++) {
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-success-found-opencl-device")
                                    .replace("%%", i + " " + platforms[i]))
                            .setColor("green")
                    );
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("setPieceSize")) {
                if (args.length != 2) {
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-error-arguments-count-error")
                                    .replace("%%", "1"))
                            .setColor("red")
                    );
                    return true;
                }
                try {
                    setPieceSize(Integer.parseInt(args[1]));
                } catch (NumberFormatException e) {
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-error-number")
                                    .replace("%%", e.getMessage()))
                            .setColor("red")
                    );
                    return true;
                }
                Main.sendMessage(sender, new JsonText(
                        LangUtils.getText("command-success-set-piece-size")
                                .replace("%%", args[1]))
                        .setColor("green")
                );
                return true;
            }
            if (args[0].equalsIgnoreCase("browser")) {
                Screen screen = null;
                try {
                    screen = Screen.getScreenFromUUID(UUID.fromString(args[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (screen != null) {
                    if (screen.getCore() instanceof WebBrowser) {
                        if (args.length < 3) {
                            Main.sendMessage(sender, new JsonText(
                                    LangUtils.getText("command-error-arguments-count-error")
                                            .replace("%%", "2-3"))
                                    .setColor("red")
                            );
                            return true;
                        }
                        if (args[2].equalsIgnoreCase("openurl")) {
                            if (args.length != 4) {
                                Main.sendMessage(sender, new JsonText(
                                        LangUtils.getText("command-error-arguments-count-error")
                                                .replace("%%", "3"))
                                        .setColor("red")
                                );
                                return true;
                            }
                            ((WebBrowser) screen.getCore()).getBrowser().openURL(screen, args[3]);
                            Main.sendMessage(sender, new JsonText(
                                    LangUtils.getText("command-success-web-browser-openurl"))
                                    .setColor("green")
                            );
                        } else if (args[2].equalsIgnoreCase("refresh")) {
                            ((WebBrowser) screen.getCore()).getBrowser().refreshPage(screen);
                            Main.sendMessage(sender, new JsonText(
                                    LangUtils.getText("command-success-web-browser-refresh"))
                                    .setColor("green")
                            );
                        }
                    } else {
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-core-is-not-web-browser"))
                                .setColor("red")
                        );
                    }
                } else {
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-error-screen-not-found")
                                    .replace("%%", args[1]))
                            .setColor("red")
                    );
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("input")) {
                Screen screen = null;
                try {
                    screen = Screen.getScreenFromUUID(UUID.fromString(args[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (screen != null) {
                    if (args.length < 3) {
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-arguments-count-error")
                                        .replace("%%", "1-2+"))
                                .setColor("red")
                        );
                        return true;
                    }
                    if (args.length == 3) {
                        String[] newString = Arrays.copyOfRange(args,2,args.length);
                        screen.getCore().onTextInput(String.join(" ",newString));
                    } else {
                        screen.getCore().onTextInput("");
                    }
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-success-input"))
                            .setColor("green")
                    );
                } else {
                    Main.sendMessage(sender, new JsonText(
                            LangUtils.getText("command-error-screen-not-found")
                                    .replace("%%", args[1]))
                            .setColor("red")
                    );
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("controller")) {
                if (args.length >= 3) {
                    try {
                        for (Screen i : Screen.getAllScreens()) {
                            if (i.getEditGUI().getOpenedPlayer().equals(sender)) {
                                String[] newArgs = Arrays.copyOfRange(args, 2, args.length);
                                UUID uuid = UUID.fromString(args[1]);
                                Function<String, Boolean> function = i.getEditGUI().getControllerCommandCallback(uuid);
                                try {
                                    function.apply(String.join(" ", newArgs));
                                } catch (Exception e) {
                                    EditGUI.forceClose((Player) sender);
                                    e.printStackTrace();
                                    return false;
                                }
                            }
                        }
                    } catch (Throwable e) {
                        EditGUI.forceClose((Player) sender);
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    if (sender instanceof Player) {
                        Item.giveItem(((Player) sender));
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-success-controller"))
                                .setColor("green")
                        );
                    } else {
                        Main.sendMessage(sender, new JsonText(
                                LangUtils.getText("command-error-is-not-player"))
                                .setColor("red")
                        );
                    }
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("help")){
                String[] helps = LangUtils.getText("command-help").split("\n");
                for(String i:helps){
                    Main.sendMessage(sender, new JsonText(i)
                            .setColor("white")
                    );
                }
                return true;
            }
//            if (args[0].equalsIgnoreCase("installChromium")) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        new Chromium().installCore();
//                    }
//                }.start();
//            }
        }
        catch (Error e){
            e.printStackTrace();
            Main.sendMessage(sender, new JsonText(
                    LangUtils.getText("command-error-unknown")
                            .replace("%%", e.getMessage()))
                    .setColor("red")
            );
        }
        catch (RuntimeException e){
            e.printStackTrace();
            Main.sendMessage(sender, new JsonText(
                    LangUtils.getText("command-error-unknown")
                            .replace("%%", e.getMessage()))
                    .setColor("red")
            );
        }
        catch (Throwable e){
            e.printStackTrace();
            Main.sendMessage(sender, new JsonText(
                    LangUtils.getText("command-error-unknown")
                            .replace("%%", e.getMessage()))
                    .setColor("red")
            );
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp()) {
            if (args.length == 1) {
                String[] sub1 = {"help", "setPieceSize", "listDevices", "putScreen", "input", "removeScreen", "controller", "browser"};
                return Arrays.stream(sub1).filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            List<String> sub2 = new ArrayList<>();
            if (args.length == 2) {
                switch (args[0]) {
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
                        if(CraftUtils.minecraftVersion<=12){
                            if(i== Screen.Facing.UP||i== Screen.Facing.DOWN){
                                continue;
                            }
                        }
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
