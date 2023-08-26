package cn.mingbai.ScreenInMC.Controller;

import cn.mingbai.ScreenInMC.Controller.EditGUI.EditGUICoreInfo.EditGUICoreSettingsList;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.*;
import cn.mingbai.ScreenInMC.RedstoneBridge;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.InWindowClickPacket.ClickType;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils.JsonText;
import cn.mingbai.ScreenInMC.Utils.Utils;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Function;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.NMSItemStack.*;
import static cn.mingbai.ScreenInMC.Utils.LangUtils.JsonText.*;

public class EditGUI {
    public static class EditGUICoreInfo{
        public interface EditGUICoreSettingsList{
            String[] getList();
        }
        private String name = "Core";
        private String details = "Core";
        private String themeColor = "gold";
        private Material icon = Material.STONE;
        private Core core;
        //Support: Integer Double Boolean String String[] Location(Vector) EditGUICoreSettingsList
        private Map<String,Class> supportedSettings = new HashMap<>();
        public EditGUICoreInfo(String name,Core core,String details,String themeColor,Material icon,Map<String,Class> supportedSettings){
            this.name = name;
            this.core = core;
            this.details = details;
            this.themeColor = themeColor;
            this.icon = icon;
            if(supportedSettings!=null){
                this.supportedSettings = supportedSettings;
            }

        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EditGUICoreInfo) {
                return core.getClass().equals(((EditGUICoreInfo)obj).core.getClass());
            }else{
                return false;
            }
        }
    }
    private static List<EditGUICoreInfo> registeredCoreInfos = new ArrayList<>();
    public static EditGUICoreInfo getCoreInfoFromCore(Core core){
        synchronized (registeredCoreInfos){
            for(EditGUICoreInfo i : registeredCoreInfos){
                if(i.core.getClass().equals(core.getClass())){
                    return i;
                }
            }
        }
        return null;
    }
    public static void registerCoreInfo(EditGUICoreInfo info){
        synchronized (registeredCoreInfos){
            for(EditGUICoreInfo i : registeredCoreInfos){
                if(i.equals(info)){
                    throw new RuntimeException("A core cannot be registered multiple times.");
                }
            }
            registeredCoreInfos.add(info);
        }
    }
    public static void unregisterCoreInfo(EditGUICoreInfo info){
        synchronized (registeredCoreInfos){
            registeredCoreInfos.remove(info);
        }
    }
    private Player openedPlayer = null;
    //The last 2 digits in decimal form of the SHA1 value of "ScreenInMC".
    private static byte lastContainerID = 86;
    private Screen screen;
    private int containerID;
    private int stateID = 0;
    public EditGUI(Screen screen){
        this.screen = screen;
        containerID=lastContainerID;
        lastContainerID++;
    }
    private void switchCore(int index){
        EditGUICoreInfo info;
        synchronized (registeredCoreInfos){
            if(registeredCoreInfos.size()<=index){
                return;
            }
            info = registeredCoreInfos.get(index);
        }
        try {
            Main.sendMessage(openedPlayer,LangUtils.getText("controller-place-start"));
            screen.getCore().unload();
            Core newCore = (Core)info.core.clone();
            screen.setCore(newCore);
            newCore.create(screen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void sendSwitchModeSound(){
        if(openedPlayer==null){
            return;
        }
        try {
            CraftUtils.playSoundPling(openedPlayer);
        }catch (Exception e){
            e.printStackTrace();
            forceClose();
        }
    }
    private static final String BookAuthor = "ScreenInMC";
    private NMSBook getBasicBook(){
        NMSBook book = new NMSBook();
        book.setAuthor(BookAuthor);
        book.setTitle(LangUtils.getText("controller-editor-gui-title"));
        book.setGeneration(0);
        book.setResolved(true);
        book.setName(
                new JsonText(LangUtils.getText("controller-editor-gui-title"))
                        .setColor("gold")
        );
        return book;
    }

    public static abstract class EditGUISubWindow{
        private static Map<Player,List<EditGUISubWindow>> openedWindows = new HashMap<>();
        private ImmediatelyCancellableBukkitRunnable runnable;
        private boolean opened;

        public boolean isOpened() {
            return opened;
        }

        public abstract void openWindow(Player player);
        public abstract void reopenWindow(Player player);
        public abstract void closeWindow(Player player);
        public static void openWindow(Player player,EditGUISubWindow window){
            if(player==null||!player.isOnline()){
                return;
            }
            List<EditGUISubWindow> list;
            synchronized (openedWindows) {
                if (openedWindows.containsKey(player)) {
                    list = openedWindows.get(player);
                } else {
                    list = new ArrayList<>();
                    openedWindows.put(player, list);
                }
            }
            if(list.size()==0){
                final Player finalPlayer = player;
                window.runnable = new ImmediatelyCancellableBukkitRunnable() {
                    @Override
                    public void run() {
                        if(finalPlayer.isOnline()&&finalPlayer!=null) {
                            List<EditGUISubWindow> list = openedWindows.get(finalPlayer);
                            if(list==null) {this.cancel();return;}
                            synchronized (list) {
                                list.get(list.size() - 1).reopenWindow(finalPlayer);
                            }
                        }else if(finalPlayer!=null){
                            closeAllWindows(finalPlayer);
                        }else{
                            this.cancel();
                        }
                    }
                };
                window.runnable.runTaskTimerAsynchronously(Main.thisPlugin(),0,1L);
            }else{
                window.runnable = list.get(list.size()-1).runnable;
            }
            synchronized (list) {
                if(list.size()>0){
                    EditGUISubWindow lastWindow = list.get(list.size()-1);
                    lastWindow.closeWindow(player);
                }
                list.add(window);
                window.openWindow(player);
            }
            window.opened=true;
        }
        public static void closeAllWindows(Player player){
            if(player==null){
                return;
            }
            if(openedWindows.containsKey(player)) {
                try {
                    List list;
                    synchronized (openedWindows) {
                        list = openedWindows.get(player);
                    }
                    while (list.size() > 0) {
                        closeTopWindow(player);
                    }
                }catch (Exception e){}
            }
        }
        public static void closeTopWindow(Player player){
            if(player==null){
                return;
            }
            EditGUISubWindow window;
            List<EditGUISubWindow> list = openedWindows.get(player);
            synchronized (list){
                if(list.size()==0){
                    return;
                }
                if(list.size()==1){
                    list.get(0).runnable.cancel();
                }
            window = list.get(list.size()-1);
                try {
                    window.closeWindow(player);
                }catch (Exception e){
                }
                window.opened=false;
                list.remove(list.size()-1);
            }
            if(list.size()==0){
                synchronized (openedWindows) {
                    openedWindows.remove(player);
                }
            }else{
                EditGUISubWindow nextWindow;
                synchronized (list) {
                    nextWindow=list.get(list.size()-1);
                    nextWindow.openWindow(player);
                }
            }
            synchronized (window){
                window.notifyAll();
            }
        }
    }
    private Map<UUID,Function<String,Boolean>> controllerCommandCallbacks = new HashMap<>();
    public Function<String,Boolean> getControllerCommandCallback(UUID uuid){
        return controllerCommandCallbacks.get(uuid);
    }
    public void addControllerCommandCallback(UUID uuid,Function<String,Boolean> callback){
        controllerCommandCallbacks.put(uuid,callback);
    }
    public void removeControllerCommandCallback(UUID uuid){
        controllerCommandCallbacks.remove(uuid);
    }

    public UUID generateNewControllerCommandCallbackUUID(){
        UUID uuid = UUID.randomUUID();
        boolean same = true;
        while (same){
            same=false;
            for(UUID i:controllerCommandCallbacks.keySet()){
                if(uuid.equals(i)){
                    same=true;
                }
            }
            uuid = UUID.randomUUID();
        }
        return uuid;
    }
    private class NumberInputWindow extends EditGUISubWindow{
        int nowIntValue = 0;
        double nowDoubleValue = 0;


        public Object getNowValue() {
            if (isDouble) {
                return nowDoubleValue;
            }else{
                return nowIntValue;
            }
        }

        boolean isDouble = false;
        public NumberInputWindow(Object originalValue,boolean isDouble){
            this.isDouble=isDouble;
            if(isDouble){
                nowDoubleValue = (double) originalValue;
            }else{
                nowIntValue = (int) originalValue;
            }
        }

        UUID uuid = generateNewControllerCommandCallbackUUID();
        @Override
        public void openWindow(Player player) {
            addControllerCommandCallback(uuid, new Function<String, Boolean>() {
                @Override
                public Boolean apply(String s) {
                    String[] command = s.split(" ");
                    if(command[0].equals("close")){
                        closeTopWindow(player);
                    }
                    if(command[0].equals("input")) {
                        ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
                            @Override
                            public void run() {
                                String str = askForString(isDouble?String.valueOf(nowDoubleValue):String.valueOf(nowIntValue));
                                try {
                                    if(isDouble) {
                                        nowDoubleValue = Double.parseDouble(str);
                                    }else{
                                        nowIntValue = Integer.parseInt(str);
                                    }
                                    openWindow(player);
                                }catch (Exception e){
                                    Main.sendMessage((Player) player,e.getMessage());
                                }
                            }
                        };
                        runnable.runTaskAsynchronously(Main.thisPlugin());
                    }
                    if(command.length==2) {
                        if (command[0].equals("add")) {
                            try {
                                if(isDouble) {
                                    nowDoubleValue +=Double.parseDouble(command[1]);
                                }else{
                                    nowIntValue +=Integer.parseInt(command[1]);
                                }
                                openWindow(player);
                            }catch (Exception e){
                                Main.sendMessage((Player) player,e.getMessage());
                            }
                        }
                        if (command[0].equals("remove")) {
                            try {
                                if(isDouble) {
                                    nowDoubleValue -=Double.parseDouble(command[1]);
                                }else{
                                    nowIntValue -=Integer.parseInt(command[1]);
                                }
                                openWindow(player);
                            }catch (Exception e){
                                Main.sendMessage((Player) player,e.getMessage());
                            }
                        }
                        if (command[0].equals("set")) {
                            try {
                                if(isDouble) {
                                    nowDoubleValue =Double.parseDouble(command[1]);
                                }else{
                                    nowIntValue =Integer.parseInt(command[1]);
                                }
                                openWindow(player);
                            }catch (Exception e){
                                Main.sendMessage((Player) player,e.getMessage());
                            }
                        }
                    }
                    return true;
                }
            });
            NMSBook book = getBasicBook();
            String[] levels;
            if(isDouble){
                levels = new String[]{"1","0.1","0.01"};
            }else{
                levels = new String[]{"100","10","1"};
            }
            book.setContent(new JsonText[]{
                            new JsonText(
                                    LangUtils.getText("ask-type")
                                            .replace("%%",
                                                    isDouble ? LangUtils.getText("type-double") : LangUtils.getText("type-int")
                                            ) + "\n"
                            ).setColor("black")
                                    .addExtra(
                                            new JsonText("[-" + levels[0] + "]")
                                                    .setClickEvent(new ClickEvent(
                                                            "run_command",
                                                            "/screen controller " + uuid.toString() + " remove " + levels[0]
                                                    ))
                                                    .setColor("dark_purple")
                                    ).addExtra(
                                            new JsonText("[-" + levels[1] + "]")
                                                    .setClickEvent(new ClickEvent(
                                                            "run_command",
                                                            "/screen controller " + uuid.toString() + " remove " + levels[1]
                                                    ))
                                                    .setColor("light_purple")
                                    )
                                    .addExtra(
                                            new JsonText("[-" + levels[2] + "]")
                                                    .setClickEvent(new ClickEvent(
                                                            "run_command",
                                                            "/screen controller " + uuid.toString() + " remove " + levels[2]
                                                    ))
                                                    .setColor("blue")
                                    )
                                    .addExtra(
                                            new JsonText("\n" + (isDouble ? (String.format("%.4f", getNowValue())) : (getNowValue())) + "\n")
                                                    .setColor("dark_red")
                                    )
                                    .addExtra(
                                            new JsonText("[+" + levels[2] + "]")
                                                    .setClickEvent(new ClickEvent(
                                                            "run_command",
                                                            "/screen controller " + uuid.toString() + " add " + levels[2]
                                                    ))
                                                    .setColor("blue")
                                    ).addExtra(
                                            new JsonText("[+" + levels[1] + "]")
                                                    .setClickEvent(new ClickEvent(
                                                            "run_command",
                                                            "/screen controller " + uuid.toString() + " add " + levels[1]
                                                    ))
                                                    .setColor("light_purple")
                                    )
                                    .addExtra(
                                            new JsonText("[+" + levels[0] + "]")
                                                    .setClickEvent(new ClickEvent(
                                                            "run_command",
                                                            "/screen controller " + uuid.toString() + " add " + levels[0]
                                                    ))
                                                    .setColor("dark_purple")
                                    )
                                    .addExtra(new JsonText("\n"))
                                    .addExtra(
                                            new JsonText("[" + LangUtils.getText("input-by-hand") + "]")
                                                    .setClickEvent(new ClickEvent(
                                                            "run_command",
                                                            "/screen controller " + uuid.toString() + " input"
                                                    ))
                                                    .setColor("dark_purple")
                                    )
                                    .addExtra(new JsonText("\n"))
                                    .addExtra(
                                    new JsonText("[" + LangUtils.getText("complete") + "]")
                                            .setClickEvent(new ClickEvent(
                                                    "run_command",
                                                    "/screen controller " + uuid.toString() + " close"
                                            ))
                                            .setColor("dark_purple")
                            )
                    }
            );
            Object packet1 = OutSetSlotPacket.create(-2, openedPlayer.getInventory().getHeldItemSlot(), book.getItemStack());
            CraftUtils.sendPacket(player,packet1);
        }

        @Override
        public void reopenWindow(Player player) {
            Object packet = OutOpenBookPacket.create();
            CraftUtils.sendPacket(player,packet);
    }

    @Override
        public void closeWindow(Player player) {
            removeControllerCommandCallback(uuid);
        }
    }
    private PacketListener addReOpenPacketListener(int containerID,Player player,Runnable setReOpen){
        return PacketListener.addListener(new PacketListener(player, InWindowClosePacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener,Player player, InPacket p) {
                if(p instanceof InWindowClosePacket){
                    InWindowClosePacket packet  = (InWindowClosePacket) p;
                    if(packet.getContainerId()==containerID){
                        setReOpen.run();
                        return true;
                    }
                }
                return false;
            }
        }));
    }
    private class BooleanInputWindow extends EditGUISubWindow{
        private static final int containerID = 85;
        private int stateID = 0;
        private PacketListener listener1;
        private PacketListener listener2;
        boolean reopen = true;
        private boolean nowBooleanValue = false;

        public boolean getNowBooleanValue() {
            return nowBooleanValue;
        }

        @Override
        public void openWindow(Player player) {
            reopen=true;
            listener1 =PacketListener.addListener(new PacketListener(player, InWindowClickPacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener, Player player,InPacket p) {
                    if(p instanceof InWindowClickPacket){
                        InWindowClickPacket packet  = (InWindowClickPacket) p;
                        if(packet.getContainerId()==containerID){
                            updateInventory();
                            resetItems(player);
                            if(packet.getSlotNum()==0){
                                nowBooleanValue=false;
                                closeTopWindow(player);
                            }
                            if(packet.getSlotNum()==4){
                                nowBooleanValue=true;
                                closeTopWindow(player);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            }));
            listener2 = addReOpenPacketListener(containerID,player,()->{reopen=true;});
            reopenWindow(player);
            resetItems(player);
        }

        @Override
        public void reopenWindow(Player player) {
            if(reopen){
                Object packet = OutOpenWindowPacket.create(
                        containerID,
                        "hopper",
                        new JsonText(LangUtils.getText("ask-type")
                                .replace("%%",
                                        LangUtils.getText("type-boolean")+"(×/√)"
                                )).setColor("gold").toComponent()
                );
                CraftUtils.sendPacket(player,packet);
                reopen=false;
                resetItems(player);
            }
        }
        private void resetItems(Player player){
            stateID = stateID + 1 & 32767;
            List<NMSItemStack> list = new ArrayList<>();
            list.add(0,NMSItemStack.EMPTY);
            list.add(0,NMSItemStack.EMPTY);
            list.add(0,NMSItemStack.EMPTY);
            list.add(0,NMSItemStack.EMPTY);
            list.add(0,NMSItemStack.EMPTY);
            NMSItemStack itemStack = new NMSItemStack(new String[]{"red_wool","wool"},1,COLOR_RED);
            itemStack.setName(new JsonText("false (×)").setColor("red"));
            list.set(0,itemStack);
            itemStack = new NMSItemStack(new String[]{"lime_wool","wool"},1,COLOR_LIME);
            itemStack.setName(new JsonText("true (√)")
                    .setColor("green"));
            list.set(1,NMSItemStack.EMPTY);
            list.set(2,NMSItemStack.EMPTY);
            list.set(3,NMSItemStack.EMPTY);
            list.set(4,itemStack);
            Object packet = OutWindowItemsPacket.create(
                    containerID,
                    stateID,list
            );
            CraftUtils.sendPacket(player,packet);
            packet = OutWindowDataPacket.create(containerID,0,0);
            CraftUtils.sendPacket(player,packet);
        }
        @Override
        public void closeWindow(Player player) {
            if(listener1 !=null){
                PacketListener.removeListener(listener1);
            }
            reopen=false;
            if(listener2 !=null) {
                PacketListener.removeListener(listener2);
            }
        }
    }
    private class StringInputWindow extends EditGUISubWindow {
        private static final int containerID = 85;
        private int stateID = 0;
        String nowStringValue="";
        private PacketListener listener1;
        private PacketListener listener2;
        private PacketListener listener3;
        public StringInputWindow(String originalValue){
            this.nowStringValue = originalValue;
        }



        public String getNowStringValue() {
            return nowStringValue;
        }
        private boolean reopen =true;
        @Override
        public void openWindow(Player player) {
            reopen=true;
            listener1 =PacketListener.addListener(new PacketListener(player, InWindowClickPacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener,Player player, InPacket p) {
                    if(p instanceof InWindowClickPacket){
                        InWindowClickPacket packet  = (InWindowClickPacket) p;
                        if(packet.getContainerId()==containerID){
                            if(packet.getSlotNum()==0){
                                nowStringValue="";
                            }
                            updateInventory();
                            resetItems(player);
                            if(packet.getSlotNum()==2){
                                closeTopWindow(player);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            }));
            listener2 = addReOpenPacketListener(containerID,player,()->{reopen=true;});
            listener3 = PacketListener.addListener(new PacketListener(player, InAnvilRenamePacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener,Player player, InPacket p) {
                    if(p instanceof InAnvilRenamePacket){
                        InAnvilRenamePacket packet  = (InAnvilRenamePacket) p;
                        nowStringValue = packet.getName();
                    }
                    return false;
                }
            }));
            reopenWindow(player);
            resetItems(player);
        }
        private void resetItems(Player player){
            stateID = stateID + 1 & 32767;
            List<NMSItemStack> list = new ArrayList<>();
            list.add(0,NMSItemStack.EMPTY);
            list.add(0,NMSItemStack.EMPTY);
            list.add(0,NMSItemStack.EMPTY);
            NMSItemStack itemStack = new NMSItemStack(new String[]{"light_gray_stained_glass_pane","stained_glass_pane"},1, NMSItemStack.COLOR_SILVER);
            itemStack.setName(new JsonText(nowStringValue).disableRichStringInOldVersion());
            list.set(0,itemStack);
            itemStack = new NMSItemStack(new String[]{"lime_stained_glass_pane","stained_glass_pane"},1, COLOR_LIME);
            itemStack.setName(new JsonText(LangUtils.getText("complete"))
                    .setColor("green"));
            list.set(1,NMSItemStack.EMPTY);
            list.set(2,itemStack);
            Object packet = OutWindowItemsPacket.create(
                    containerID,
                    stateID,list
            );
            CraftUtils.sendPacket(player,packet);
            packet = OutWindowDataPacket.create(containerID,0,0);
            CraftUtils.sendPacket(player,packet);

        }

        @Override
        public void reopenWindow(Player player) {
            if(reopen){
                Object packet = OutOpenWindowPacket.create(
                        containerID,
                        "anvil",
                        new JsonText(LangUtils.getText("ask-type")
                                .replace("%%",
                                        LangUtils.getText("type-string")
                                )).setColor("gold").toComponent()
                );
                CraftUtils.sendPacket(player,packet);
                reopen=false;
            }
            resetItems(player);

        }

        @Override
        public void closeWindow(Player player) {
            if(listener1 !=null){
                PacketListener.removeListener(listener1);
            }
            reopen=false;
            if(listener2 !=null) {
                PacketListener.removeListener(listener2);
            }
            if(listener3 !=null) {
                PacketListener.removeListener(listener3);
            }
        }
    }
    private class LocationInputWindow extends EditGUISubWindow{

        World world = Bukkit.getWorld("world");
        double x = 0;
        double y =0;
        double z = 0;

        public Object getNowValue() {
            if (isVector) {
                return new Vector(x,y,z);
            }else{
                return new Location(world,x,y,z);
            }
        }

        boolean isVector = false;
        public LocationInputWindow(Object originalValue,boolean isVector){
            this.isVector=isVector;
            if(isVector){
                Vector vector = (Vector) originalValue;
                x=vector.getX();
                y=vector.getY();
                z=vector.getZ();
            }else{
                Location location = (Location) originalValue;
                world = location.getWorld();
                x=location.getX();
                y=location.getY();
                z=location.getZ();
            }
        }

        UUID uuid = generateNewControllerCommandCallbackUUID();

        @Override
        public void openWindow(Player player) {
            addControllerCommandCallback(uuid, new Function<String, Boolean>() {
                @Override
                public Boolean apply(String s) {
                    String[] command = s.split(" ");
                    if(command[0].equals("close")){
                        closeTopWindow(player);
                    }
                    if(command.length==2) {
                        if (command[0].equals("now")) {
                            world = player.getWorld();
                            if(command[1].equals("block")){
                                x = player.getLocation().getBlockX();
                                y = player.getLocation().getBlockY();
                                z = player.getLocation().getBlockZ();
                                openWindow(player);
                            }
                            if(command[1].equals("player")){
                                x = player.getLocation().getX();
                                y = player.getLocation().getY();
                                z = player.getLocation().getZ();
                                openWindow(player);
                            }
                        }
                        if (command[0].equals("set")) {
                            ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
                                @Override
                                public void run() {
                                    if(command[1].equals("world")){
                                        List<World> worlds = Bukkit.getWorlds();
                                        String[] str = new String[worlds.size()];
                                        for(int i=0;i<worlds.size();i++){
                                            str[i] = worlds.get(i).getName();
                                        }
                                        int i = askForStringFromList(str);
                                        world = Bukkit.getWorld(str[i]);
                                        openWindow(player);
                                    }
                                    if(command[1].equals("x")){
                                        x = askForDouble(x);
                                        openWindow(player);
                                    }
                                    if(command[1].equals("y")){
                                        y = askForDouble(y);
                                        openWindow(player);
                                    }
                                    if(command[1].equals("z")){
                                        z = askForDouble(z);
                                        openWindow(player);
                                    }
                                    if(command[1].equals("xyz")){
                                        try {
                                            String[] xyz = askForString(
                                                    String.format("%.4f",x)+" "
                                                    +String.format("%.4f",y)+ " "
                                                    +String.format("%.4f",z)
                                            ).split("[,\\s]+");
                                            x = Double.parseDouble(xyz[0]);
                                            y = Double.parseDouble(xyz[1]);
                                            z = Double.parseDouble(xyz[2]);
                                        }catch (Exception e){
                                            Main.sendMessage(player,e.getMessage());
                                        }
                                        openWindow(player);
                                    }
                                }
                            };
                            runnable.runTaskAsynchronously(Main.thisPlugin());
                        }
                    }
                    return true;
                }
            });
            NMSBook book = getBasicBook();
            JsonText jsonText = new JsonText(
                    LangUtils.getText("ask-type")
                            .replace("%%",
                                    LangUtils.getText("type-location")
                            )+ "\n"
            ).setColor("black");
            if(!isVector){
                jsonText.addExtra(new JsonText(LangUtils.getText("ask-world")+" ").setColor("dark_blue"));
                jsonText.addExtra(new JsonText(world.getName()+" ").setColor("blue"));
                jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+"]").setColor("gold").setClickEvent(new ClickEvent(
                        "run_command",
                        "/screen controller "+uuid.toString()+" set world"
                )));
                jsonText.addExtra(new JsonText("\n"));
            }
            jsonText.addExtra(new JsonText("X: ").setColor("dark_red"));
            jsonText.addExtra(new JsonText(String.format("%.4f",x)+" ").setColor("red"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    "run_command",
                    "/screen controller "+uuid.toString()+" set x"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("Y: ").setColor("dark_red"));
            jsonText.addExtra(new JsonText(String.format("%.4f",y)+" ").setColor("red"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    "run_command",
                    "/screen controller "+uuid.toString()+" set y"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("Z: ").setColor("dark_red"));
            jsonText.addExtra(new JsonText(String.format("%.4f",z)+" ").setColor("red"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    "run_command",
                    "/screen controller "+uuid.toString()+" set z"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+" XYZ]").setColor("gold").setClickEvent(new ClickEvent(
                    "run_command",
                    "/screen controller "+uuid.toString()+" set xyz"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("use-now-player-location")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    "run_command",
                    "/screen controller "+uuid.toString()+" now player"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("use-now-block-location")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    "run_command",
                    "/screen controller "+uuid.toString()+" now block"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("complete")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    "run_command",
                    "/screen controller "+uuid.toString()+" close"
            )));
            book.setContent(new JsonText[]{jsonText});
            Object packet1 = OutSetSlotPacket.create(-2, openedPlayer.getInventory().getHeldItemSlot(), book.getItemStack());
            CraftUtils.sendPacket(player,packet1);
        }

        @Override
        public void reopenWindow(Player player) {
            Object packet = OutOpenBookPacket.create();
            CraftUtils.sendPacket(player,packet);
        }

        @Override
        public void closeWindow(Player player) {
            removeControllerCommandCallback(uuid);
        }
    }
    private abstract class PageWindow extends EditGUISubWindow{
        public abstract class PageWindowCallBackRunnable{
            private InWindowClickPacket packet;
            protected InWindowClickPacket getPacket(){
                return packet;
            }
            public abstract void run();
        }
        private boolean completeButton = true;

        public void setCompleteButton(boolean completeButton) {
            this.completeButton = completeButton;
        }

        private static final int containerID = 85;
        private int stateID = 0;
        private PacketListener listener1;
        private PacketListener listener2;
        boolean reopen = true;
        public abstract List<NMSItemStack> getItems(Player player,int startIndex,int count);
        public abstract List<PageWindowCallBackRunnable> getCallbacks(Player player,int startIndex,int count);

        public abstract int getCount();
        @Override
        public void openWindow(Player player) {
            reopen=true;
            listener1 =PacketListener.addListener(new PacketListener(player, InWindowClickPacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener, Player player,InPacket p) {
                    if(p instanceof InWindowClickPacket){
                        InWindowClickPacket packet  = (InWindowClickPacket) p;
                        if(packet.getContainerId()==containerID){
                            int count = getCount();
                            if(completeButton&&packet.getSlotNum()==49){
                                closeTopWindow(player);
                                return true;
                            }
                            totalPage = calcPage(count,45);
                            nowPage = calcNowPage(nowPage,totalPage);
                            if(packet.getSlotNum()==45&&nowPage>0){
                                nowPage--;
                            }
                            if(packet.getSlotNum()==53&&nowPage<totalPage-1){
                                nowPage++;
                            }
                            List<PageWindowCallBackRunnable> callbacks = getCallbacks(player,nowPage*45,45);
                            if(packet.getSlotNum()>=0&&packet.getSlotNum()<45&&
                                    callbacks.size()>packet.getSlotNum()
                            ){
                                PageWindowCallBackRunnable runnable = callbacks.get(packet.getSlotNum());
                                runnable.packet=packet;
                                runnable.run();
                            }
                            updateInventory();
                            resetItems(player);
                            return true;
                        }
                    }
                    return false;
                }
            }));
            listener2 = addReOpenPacketListener(containerID,player,()->{reopen=true;});
            reopenWindow(player);
            resetItems(player);
        }
        protected void reload(Player player){
            updateInventory();
            resetItems(player);
        }

        @Override
        public void reopenWindow(Player player) {
            if(reopen){
                Object packet = OutOpenWindowPacket.create(
                        containerID,
                        "generic_9x6",
                        title.toComponent()
                );
                CraftUtils.sendPacket(player,packet);
                reopen=false;
                resetItems(player);
            }
        }
        private JsonText title = new JsonText("");

        public void setTitle(JsonText title) {
            this.title = title;
        }

        private int nowPage = 0;
        private int totalPage = 0;

        private void resetItems(Player player){
            stateID = stateID + 1 & 32767;
            List<NMSItemStack> list = new ArrayList<>();
            for(int i=0;i<54;i++){
                list.add(0,NMSItemStack.EMPTY);
            }
            totalPage = calcPage(getCount(),45);
            nowPage = calcNowPage(nowPage,totalPage);
            List<NMSItemStack> items = getItems(player,nowPage*45,45);
            for(int i=0;i<45;i++){
                if(i>items.size()-1){
                    break;
                }
                list.set(i,items.get(i));
            }
            if(nowPage<totalPage-1){
                list.set(53,getItemSwitchPage(true,nowPage,totalPage));
            }
            if(nowPage>0){
                list.set(45,getItemSwitchPage(false,nowPage,totalPage));
            }
            if(completeButton){
                NMSItemStack stack = new NMSItemStack(new String[]{"lime_wool","wool"},COLOR_LIME);
                stack.setName(new JsonText(LangUtils.getText("complete"))
                        .setColor("green")
                );
                list.set(49,stack);
            }
            Object packet = OutWindowItemsPacket.create(
                    containerID,
                    stateID,list
            );
            CraftUtils.sendPacket(player,packet);
            packet = OutWindowDataPacket.create(containerID,0,0);
            CraftUtils.sendPacket(player,packet);
        }
        @Override
        public void closeWindow(Player player) {
            if(listener1 !=null){
                PacketListener.removeListener(listener1);
            }
            reopen=false;
            if(listener2 !=null) {
                PacketListener.removeListener(listener2);
            }
        }
    }

    private class StringArrayInputWindow extends PageWindow{
        public StringArrayInputWindow(String[] originalValue){
            for(int i=0;i<originalValue.length;i++){
                strings.add(originalValue[i]);
            }
            setTitle(new JsonText(LangUtils.getText("ask-type")
                    .replace("%%",
                            LangUtils.getText("type-string-array")
                    )).setColor("gold"));
        }
        List<String> strings = new ArrayList<>();

        public String[] getNowValue() {
            return strings.toArray(new String[0]);
        }


        private List<Utils.Pair<Integer,Integer>> getTypes(){
            List<Utils.Pair<Integer,Integer>> types = new ArrayList<>();
            int count = strings.size();
            if(count==0){
                types.add(new Utils.Pair<Integer,Integer>(-1,0));
            }else{
                for(int i=0;i<strings.size();i++){
                    types.add(new Utils.Pair<Integer,Integer>(-1,i));
                    types.add(new Utils.Pair<Integer,Integer>(i,null));
                }
                types.add(new Utils.Pair<Integer,Integer>(-1,strings.size()));
            }
            return types;
        }


        @Override
        public List<NMSItemStack> getItems(Player player,int startIndex,int count) {
            List<NMSItemStack> list = new ArrayList<>();
            List<Utils.Pair<Integer,Integer>> types = getTypes();
            for(int i=startIndex;i<startIndex+count;i++){
                if(i>types.size()-1){
                    break;
                }
                boolean isInsert = types.get(i).getKey()==-1;
                NMSItemStack stack = new NMSItemStack(new String[]{isInsert?"gold_block":"diamond_block"},1);
                if(isInsert){
                    stack.setName(new JsonText(
                            LangUtils.getText("insert"))
                            .setColor("gold"));
                }else {
                    String[] throwToRemoveStrings = LangUtils.getText("throw-to-remove").split("%%");
                    stack.setName(new JsonText(
                            types.get(i).getKey() + " " + LangUtils.getText("click-to-set") + " " + throwToRemoveStrings[0])
                            .addExtra(new JsonText().setKeybind("key.drop").setColor("dark_aqua"))
                            .addExtra(new JsonText(throwToRemoveStrings[1]).setColor("blue"))
                            .setColor("blue")
                    );
                }
                if(!isInsert){
                    JsonText[] lore = new JsonText[1];
                    try {
                        lore[0]=new JsonText(strings.get(types.get(i).getKey()))
                                .setItalic(false)
                                .setColor("white");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    stack.setLore(lore);
                }
                list.add(stack);
            }
            return list;
        }
        @Override
        public List<PageWindowCallBackRunnable> getCallbacks(Player player,int startIndex,int count) {
            List<PageWindowCallBackRunnable> list = new ArrayList<>();
            List<Utils.Pair<Integer,Integer>> types = getTypes();
            for(int i=startIndex;i<startIndex+count;i++){
                if(i>types.size()-1){
                    break;
                }
                Utils.Pair<Integer,Integer> pair = types.get(i);
               if(types.get(i).getKey()==-1){
                   list.add(new PageWindowCallBackRunnable() {
                       @Override
                       public void run() {
                           strings.add(pair.getValue(), "");
                       }
                   });
               }else{
                   list.add(new PageWindowCallBackRunnable() {
                       @Override
                       public void run() {
                           if(getPacket().getClickType().equals(ClickType.THROW)) {
                                strings.remove((int)pair.getKey());
                           }else{
                               ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
                                   @Override
                                   public void run() {
                                       String str = askForString(strings.get(pair.getKey()));
                                       strings.set(pair.getKey(), str);
                                       reload(player);
                                   }
                               };
                               runnable.runTaskAsynchronously(Main.thisPlugin());
                           }
                       }
                   });

               }
            }
            return list;
        }

        @Override
        public int getCount() {
            return getTypes().size();
        }
    }
    private class StringSelectWindow extends PageWindow{
        String[] strings;
        int index = 0;
        public int getNowValue(){
            return index;
        }
        public StringSelectWindow(String[] list){
            if(list.length==0){
                throw new RuntimeException("Array length cannot be zero.");
            }
            strings = list.clone();
            setCompleteButton(false);
            setTitle(new JsonText(LangUtils.getText("type-list")).setColor("gold"));
        }

        @Override
        public List<NMSItemStack> getItems(Player player, int startIndex, int count) {
            List<NMSItemStack> list = new ArrayList<>();
            for(int i=startIndex;i<startIndex+count;i++){
                if(i>strings.length-1){
                    break;
                }
                NMSItemStack stack = new NMSItemStack(new String[]{"diamond_block"},1);
                stack.setName(new JsonText(
                        strings[i]
                )
                        .setColor("blue"));
                list.add(stack);
            }
            return list;
        }

        @Override
        public List<PageWindowCallBackRunnable> getCallbacks(Player player, int startIndex, int count) {
            List<PageWindowCallBackRunnable> list = new ArrayList<>();
            for(int i=startIndex;i<startIndex+count;i++){
                if(i>strings.length-1){
                    break;
                }
                final int finalIndex = i;
                list.add(new PageWindowCallBackRunnable(){
                    @Override
                    public void run() {
                        index = finalIndex;
                        closeTopWindow(player);
                    }
                });

            }
            return list;
        }

        @Override
        public int getCount() {
            return strings.length;
        }
    }


    private int askForInteger(int originalValue){
        NumberInputWindow window = new NumberInputWindow(originalValue,false);
        openAndWaitWindowClose(window);
        return (int)window.getNowValue();
    }

    public Player getOpenedPlayer() {
        return openedPlayer;
    }

    private double askForDouble(double originalValue){
        NumberInputWindow window = new NumberInputWindow(originalValue,true);
        openAndWaitWindowClose(window);
        return (double)window.getNowValue();
    }
    private boolean askForBoolean(){
        BooleanInputWindow window = new BooleanInputWindow();
        openAndWaitWindowClose(window);
        return window.getNowBooleanValue();
    }
    private String askForString(String originalValue){
        StringInputWindow window = new StringInputWindow(originalValue);
        openAndWaitWindowClose(window);
        return window.getNowStringValue();
    }
    private void openAndWaitWindowClose(EditGUISubWindow window){
        EditGUISubWindow.openWindow(openedPlayer,window);
        while (window.isOpened()) {
            try {
                synchronized (window) {
                    window.wait();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
    private String[] askForStringArray(String[] originalValue){
        StringArrayInputWindow window = new StringArrayInputWindow(originalValue);
        openAndWaitWindowClose(window);
        return window.getNowValue();
    }
    private int askForStringFromList(String[] list){
        if(list.length==0){
            return -1;
        }
        StringSelectWindow window = new StringSelectWindow(list);
        openAndWaitWindowClose(window);
        return window.getNowValue();
    }
    private Location askForLocation(Location originalValue){
        LocationInputWindow window = new LocationInputWindow(originalValue,false);
        openAndWaitWindowClose(window);
        return (Location) window.getNowValue();
    }
    private Vector askForVector(Vector originalValue){
        LocationInputWindow window = new LocationInputWindow(originalValue,true);
        openAndWaitWindowClose(window);
        return (Vector) window.getNowValue();
    }
    private void openSetting(int index){
        if(screen.getCore()==null){
            return;
        }
        EditGUICoreInfo info = getCoreInfoFromCore(screen.getCore());
        List<Utils.Pair<String,Class>> list = getSupportedSettings(info);
        if(list.size()<=index){
            return;
        }

        ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
            @Override
            public void run() {
                synchronized (this){
                    if(EditGUISubWindow.openedWindows.containsKey(openedPlayer)){
                        List<EditGUISubWindow> windows = EditGUISubWindow.openedWindows.get(openedPlayer);
                        synchronized (windows){
                            windows.get(windows.size()-1).closeWindow(openedPlayer);
                            windows.get(windows.size()-1).openWindow(openedPlayer);
                        }
                        return;
                    }
                }
                Utils.Pair<String,Class> item = list.get(index);
                String key = item.getKey();
                if (item.getValue().equals(Integer.class)) {
                    Integer originalValue = null;
                    try {
                        originalValue = (Integer) screen.getCore().getEditGUISettingValue(key);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                    if(originalValue==null){
                        originalValue=0;
                    }
                    int i = askForInteger(originalValue);
                    try {
                        screen.getCore().setEditGUISettingValue(key,i);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                } else if (item.getValue().equals(Double.class) || item.getValue().equals(Float.class)) {
                    Double originalValue = null;
                    try {
                        originalValue = (Double) screen.getCore().getEditGUISettingValue(key);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                    if(originalValue==null){
                        originalValue=0d;
                    }
                    double d = askForDouble(originalValue);
                    try {
                        screen.getCore().setEditGUISettingValue(key, d);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                } else if (item.getValue().equals(Boolean.class)) {
                    boolean b = askForBoolean();
                    try {
                        screen.getCore().setEditGUISettingValue(key, b);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                } else if (item.getValue().equals(String.class)) {
                    String originalValue = null;
                    try {
                        originalValue = (String) screen.getCore().getEditGUISettingValue(key);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                    if(originalValue==null){
                        originalValue="";
                    }
                    String str = askForString(originalValue);
                    try {
                        screen.getCore().setEditGUISettingValue(key, str);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                } else if (item.getValue().equals(String[].class)) {
                    String[] originalValue = null;
                    try {
                        originalValue = (String[]) screen.getCore().getEditGUISettingValue(key);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                    if(originalValue==null){
                        originalValue=new String[0];
                    }
                    String[] a = askForStringArray(originalValue);
                    try {
                        screen.getCore().setEditGUISettingValue(key, a);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                } else if (item.getValue().equals(Location.class)) {
                    Location originalValue = null;
                    try {
                        originalValue = (Location) screen.getCore().getEditGUISettingValue(key);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                    if(originalValue==null){
                        originalValue=new Location(Bukkit.getWorld("world"),0,0,0);
                    }
                    Location l = askForLocation(originalValue);
                    try {
                        screen.getCore().setEditGUISettingValue(key, l);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                } else if (item.getValue().equals(Vector.class)) {
                    Vector originalValue = null;
                    try {
                        originalValue = (Vector) screen.getCore().getEditGUISettingValue(key);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                    if(originalValue==null){
                        originalValue=new Vector(0,0,0);
                    }
                    Vector v = askForVector(originalValue);
                    try {
                        screen.getCore().setEditGUISettingValue(key,v);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                } else if (EditGUICoreSettingsList.class.isAssignableFrom(item.getValue())) {
                    try {
                        EditGUICoreSettingsList l = (EditGUICoreSettingsList) item.getValue().getDeclaredConstructor().newInstance();
                        int i = askForStringFromList(l.getList());
                        screen.getCore().setEditGUISettingValue(key,i);
                    } catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    catch (Error e){
                        e.printStackTrace();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                }
                try {
                    reOpenContainer();
                    setBaseItems();
                    updateInventory();
                }catch (Exception e){
                    e.printStackTrace();
                    forceClose();
                }
            }
        };
        runnable.runTaskAsynchronously(Main.thisPlugin());

    }
    private void reOpenContainer() {
        if (openedPlayer != null) {
            JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-gui-title"));
            Object packet = OutOpenWindowPacket.create(containerID, "generic_9x6", jsonText.toComponent());
            CraftUtils.sendPacket(openedPlayer, packet);
        }
    }
    private Integer clickX;
    private Integer clickY;
    private Utils.MouseClickType clickType;
    private void updateInventory(){
        openedPlayer.updateInventory();
        Object setSlotPacket = OutSetSlotPacket.create(-1,-1, NMSItemStack.bukkitToNmsItemStack(openedPlayer.getItemOnCursor()));
        CraftUtils.sendPacket(openedPlayer,setSlotPacket);
    }
    PacketListener listener1;
    PacketListener listener2;
    private boolean startConnectRedstone(int index){
        if(screen.getCore()==null){
            return false;
        }
        List<Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface>> list = screen.getCore().getRedstoneBridge().getRedstoneSignalInterfaces();
        if(list.size()<=index){
            return false;
        }
        org.bukkit.inventory.ItemStack itemStack = Item.getItemFromPlayer(openedPlayer);
        if(itemStack!=null){
            Item.ItemData data = Item.getData(itemStack);
            data.conn = new Item.ConnectModeData();
            data.conn.core = screen.getCore().getCoreName();
            data.conn.id = screen.getUUID().toString();
            data.conn.i = index;
            try {
                NMSItemStack.writeScreenInMCData(itemStack,Item.getDataJson(data));
                Item.switchMode(openedPlayer,Item.CONNECT_MODE);
            } catch (Exception e) {
                sendClosePacket();
                onClose();
                e.printStackTrace();
                return true;
            }
            String[] message = LangUtils.getText("controller-connect-start").split("%right-click%");
            Main.sendMessage(openedPlayer,new LangUtils.JsonText(message[0]).setColor("white")
                    .addExtra(new JsonText().setKeybind("key.use").setColor("yellow"))
                    .addExtra(new JsonText(message[1]).setColor("white"))
            );
        }
        sendClosePacket();
        onClose();
        return true;
    }
    private void openContainer(){
        listener1 = PacketListener.addListener(new PacketListener(openedPlayer, InWindowClickPacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener,Player player, InPacket p) {
                if (p instanceof InWindowClickPacket) {
                    InWindowClickPacket packet = (InWindowClickPacket) p;
                    if (packet.getContainerId() == containerID) {
                        updateInventory();
                        if(packet.getSlotNum()==3||packet.getSlotNum()==12){
                            Screen.removeScreen(screen);
                            forceClose();
                            return true;
                        }
                        if(packet.getSlotNum()>=0&&packet.getSlotNum()<=2){
                            nowMode = (short) packet.getSlotNum();
                            nowPage=0;
                            sendSwitchModeSound();
                        }
                        if(packet.getSlotNum()>=9&&packet.getSlotNum()<=11){
                            nowMode = (short) (packet.getSlotNum()-9);
                            nowPage=0;
                            sendSwitchModeSound();
                        }
                        if(nowMode==0||nowMode==1||nowMode==2) {
                            if (packet.getSlotNum() == 45 && nowPage > 0) {
                                nowPage--;
                                sendSwitchModeSound();
                            }
                            if (packet.getSlotNum() == 53 && nowPage < totalPage - 1) {
                                nowPage++;
                                sendSwitchModeSound();
                            }
                        }
                        if(nowMode==0){
                            if(packet.getSlotNum()>=18 && packet.getSlotNum()<=44) {
                                int index = nowPage * 27 + packet.getSlotNum() - 18;
                                openSetting(index);
                                sendSwitchModeSound();
                            }
                        }
                        if(nowMode==1){
                            if(packet.getSlotNum()>=18 && packet.getSlotNum()<=44) {
                                int index = nowPage * 27 + packet.getSlotNum() - 18;
                                switchCore(index);
                                sendSwitchModeSound();
                            }
                        }
                        if(nowMode==2){
                            if(packet.getSlotNum()>=18 && packet.getSlotNum()<=44) {
                                int index = nowPage * 27 + packet.getSlotNum() - 18;
                                if(startConnectRedstone(index)){
                                    return true;
                                }
                                sendSwitchModeSound();
                            }
                        }

                        try {
                            setBaseItems();
                        }catch (Exception e){
                            e.printStackTrace();
                            forceClose();
                        }
                        return true;
                    }
                }
                return false;
            }

        }));
        listener2 =  PacketListener.addListener(new PacketListener(openedPlayer, InWindowClosePacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener, Player player,InPacket p) {
                if (p instanceof InWindowClosePacket) {
                    InWindowClosePacket packet = (InWindowClosePacket) p;
                    if (packet.getContainerId() == containerID) {
                        onClose();
                        return true;
                    }
                }
                return false;
            }
        }));
        reOpenContainer();

    }
    public void forceClose() {
        Player player = openedPlayer;
        onClose();
        if(player!=null&&player.isOnline()) {
            Object packet = OutCloseWindowPacket.create(containerID);
            CraftUtils.sendPacket(player, packet);
        }
    }
    public static void forceClose(Player player){
        for(Screen i:Screen.getAllScreens()){
            if(player.equals(i.getEditGUI().openedPlayer)){
                i.getEditGUI().forceClose();
            }
        }
        EditGUISubWindow.closeAllWindows(player);
    }
    private void sendClosePacket(){
        Object closePacket = OutCloseWindowPacket.create(containerID);
        CraftUtils.sendPacket(openedPlayer,closePacket);
    }
    private void onClose(){
        try {
            PacketListener.removeListener(listener1);
            PacketListener.removeListener(listener2);
            List<EditGUISubWindow> list = EditGUISubWindow.openedWindows.get(openedPlayer);
            if(list!=null){
                EditGUISubWindow.closeAllWindows(openedPlayer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        openedPlayer=null;
    }
    private NMSItemStack getItem0(){
        NMSItemStack stack = new NMSItemStack(new String[]{"black_glazed_terracotta","black_wool","wool"},1, COLOR_BLACK);
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-title")).setColor("gold");
        JsonText[] lore = new JsonText[]{
            new JsonText(LangUtils.getText("controller-editor-id"))
                    .addExtra(new JsonText(screen.getUUID().toString()).setColor("yellow").setItalic(false))
                    .setItalic(false)
                    .setColor("gold"),
            new JsonText(LangUtils.getText("controller-editor-location"))
                    .addExtra(
                            new JsonText(screen.getLocation().getWorld().getName() + " X:" +
                                    screen.getLocation().getBlockX() + " Y:" +
                                    screen.getLocation().getBlockY() + " Z:" +
                                    screen.getLocation().getBlockZ()
                            ).setColor("yellow").setItalic(false)
                    )
                    .setItalic(false)
                    .setColor("gold"),
            new JsonText(LangUtils.getText("controller-editor-facing"))
                    .addExtra(new JsonText(screen.getFacing().getTranslatedFacingName()).setColor("yellow").setItalic(false))
                    .setItalic(false)
                    .setColor("gold"),
            new JsonText(LangUtils.getText("controller-editor-size"))
                    .addExtra(new JsonText(screen.getWidth() + "x" + screen.getHeight()).setColor("yellow").setItalic(false))
                    .setItalic(false)
                    .setColor("gold"),
            new JsonText(LangUtils.getText("controller-editor-core"))
                    .addExtra(new JsonText(screen.getCore().getCoreName()).setColor("yellow").setItalic(false))
                    .setItalic(false)
                    .setColor("gold"),
            new JsonText(LangUtils.getText("controller-editor-clicked"))
                    .addExtra(new JsonText(clickType.getTranslatedName() + " X:" + clickX + " Y:" + clickY).setColor("yellow").setItalic(false))
                    .setItalic(false)
                    .setColor("gold")
        };
        stack.setName(jsonText);
        stack.setLore(lore);
        return stack;
    }
    private NMSItemStack getItem1(){
        NMSItemStack stack = new NMSItemStack(new String[]{"magenta_glazed_terracotta","magenta_wool","wool"},1, COLOR_MAGENTA);
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-replace-core-title")).setColor("light_purple");
        JsonText[] lore = new JsonText[]{
            new JsonText(LangUtils.getText("controller-editor-replace-core-info"))
                .setItalic(false)
                .setColor("light_purple")
        };
        stack.setName(jsonText);
        stack.setLore(lore);
        return stack;
    }
    private NMSItemStack getItem2(){
        NMSItemStack stack = new NMSItemStack(new String[]{"red_glazed_terracotta","red_wool","wool"},1,COLOR_RED);
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-redstone-connect-title")).setColor("red");
        JsonText[] lore = new JsonText[]{
            new JsonText(LangUtils.getText("controller-editor-redstone-connect-info"))
                    .setItalic(false)
                    .setColor("red")
        };
        stack.setName(jsonText);
        stack.setLore(lore);
        return stack;
    }
    private NMSItemStack getItem3(){
        NMSItemStack stack = new NMSItemStack(new String[]{"tnt"},1);
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-redstone-remove-title")).setColor("red");
        stack.setName(jsonText);
        return stack;
    }
    private NMSItemStack getItem2NotSupported(){
        NMSItemStack stack = new NMSItemStack(new String[]{"red_stained_glass","stained_glass"},1,COLOR_RED);
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-redstone-connect-not-supported")).setColor("red");
        stack.setName(jsonText);
        return stack;
    }
    private NMSItemStack getCoreInfoItem(EditGUICoreInfo info){
        NMSItemStack stack = new NMSItemStack(new String[]{info.icon.name().toLowerCase()},1);
        String name = info.name;
        if(name.startsWith("@")){
            name=LangUtils.getText(name.substring(1));
        }
        JsonText jsonText = new JsonText(name).setColor(info.themeColor);
        List<JsonText> lore = new ArrayList<>();
        String details = info.details;
        if(details.startsWith("@")){
            details=LangUtils.getText(details.substring(1));
        }
        for(String i:details.split("\n")){
            lore.add(new JsonText(i)
                    .setItalic(false)
                    .setColor(info.themeColor));
        }
        try {
            if(info.core.getClass().equals(screen.getCore().getClass())){
                lore.add(new JsonText(""));
                lore.add(new JsonText(LangUtils.getText("current-selection"))
                        .setItalic(false)
                        .setColor("gold"));
            }
        }catch (Exception e){}
        stack.setName(jsonText);
        stack.setLore(lore.toArray(new JsonText[0]));
        return stack;
    }
    private short nowMode=0; //0=Info Mode //1=Select Core Mode
    private int nowPage=0;
    private int totalPage = 0;
    private NMSItemStack getItem9To17(int i){
        String type = "white_stained_glass_pane";
        short oldId = COLOR_WHITE;
        if(i==nowMode){
            if(nowMode==0){
                type = "yellow_stained_glass_pane";
                oldId = COLOR_YELLOW;
            }else{
                type = "lime_stained_glass_pane";
                oldId = COLOR_LIME;
            }
        }
        NMSItemStack stack = new NMSItemStack(new String[]{type,"stained_glass_pane"},1,oldId);
        JsonText jsonText = new JsonText("");
        stack.setName(jsonText);
        return stack;
    }
    private static NMSItemStack getItemSwitchPage(boolean next,int nowPage,int totalPage){
        NMSItemStack stack = new NMSItemStack(new String[]{"stone_button"},1);
        JsonText jsonText = new JsonText(next?
                LangUtils.getText("controller-editor-next-page"):
                LangUtils.getText("controller-editor-previous-page")
                ).setColor("gold");
        JsonText[] lore = new JsonText[]{
                new JsonText(
                LangUtils.getText("controller-editor-all-page")
                        .replace("%now%",String.valueOf(nowPage+1))
                        .replace("%all%",String.valueOf(totalPage))
                )
                .setItalic(false)
                .setColor("yellow")
        };
        stack.setName(jsonText);
        stack.setLore(lore);
        return stack;
    }
    public NMSItemStack getCoreSettingItem(Utils.Pair<String, Class> settings){
        NMSItemStack stack = new NMSItemStack(new String[]{"redstone_block"},1);
        String settingName = settings.getKey();
        if(settingName.startsWith("@")){
            settingName = LangUtils.getText(settingName.substring(1));
        }
        JsonText jsonText = new JsonText(settingName).setColor("red");
        List<JsonText> lore = new ArrayList<>();
        String type;
        if (settings.getValue().equals(Integer.class)) {
            type = LangUtils.getText("type-int");
        } else if (settings.getValue().equals(Double.class) || settings.getValue().equals(Float.class)) {
            type = LangUtils.getText("type-double");
        } else if (settings.getValue().equals(Boolean.class)) {
            type = LangUtils.getText("type-boolean")+"(×/√)";
        } else if (settings.getValue().equals(String.class)) {
            type = LangUtils.getText("type-string");
        } else if (settings.getValue().equals(String[].class)) {
            type = LangUtils.getText("type-string-array");
        } else if (settings.getValue().equals(Location.class) || settings.getValue().equals(Vector.class)) {
            type = LangUtils.getText("type-location");
        } else if (EditGUICoreSettingsList.class.isAssignableFrom(settings.getValue())) {
            type = LangUtils.getText("type-list");
        } else {
            type = LangUtils.getText("type-unknown");
        }
        lore.add(new JsonText(
                LangUtils.getText("type")+" "+type
                )
                .setItalic(false)
                .setColor("yellow"));
        try{
            Object nowValue=null;
            try {
                nowValue = screen.getCore().getEditGUISettingValue(settings.getKey());
            }catch (RuntimeException e){
                e.printStackTrace();
            }
            catch (Error e){
                e.printStackTrace();
            }
            catch (Throwable e){
                e.printStackTrace();
            }
            List<String> texts = new ArrayList<>();
            if(nowValue!=null){
                if (settings.getValue().equals(Integer.class)) {
                    texts.add(String.valueOf((int)nowValue));
                } else if (settings.getValue().equals(Double.class) || settings.getValue().equals(Float.class)) {
                    texts.add(String.valueOf((double)nowValue));
                } else if (settings.getValue().equals(Boolean.class)) {
                    texts.add(String.valueOf((boolean)nowValue));
                } else if (settings.getValue().equals(String.class)) {
                    texts.add((String) nowValue);
                } else if (settings.getValue().equals(String[].class)) {
                    String[] array = (String[]) nowValue;
                    texts.add("");
                    for(String i : array){
                        texts.add(i);
                    }
                } else if (settings.getValue().equals(Location.class)) {
                    Location location = (Location) nowValue;
                    texts.add(location.getWorld().getName()+" X:"+location.getX()+" Y:"+location.getY()+" Z:"+location.getZ());
                } else if (settings.getValue().equals(Vector.class)) {
                    Vector vector = (Vector) nowValue;
                    texts.add("none X:"+vector.getX()+" Y:"+vector.getY()+" Z:"+vector.getZ());
                } else if (EditGUICoreSettingsList.class.isAssignableFrom(settings.getValue())) {
                    try {
                        EditGUICoreSettingsList list = (EditGUICoreSettingsList) settings.getValue().getDeclaredConstructor().newInstance();
                        String[] strings = list.getList();
                        if(strings.length!=0) {
                            if((int)nowValue!=-1) {
                                texts.add(strings[(int)nowValue]);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
            for(int i=0;i<texts.size();i++) {
                lore.add(
                        new JsonText(i==0?
                                LangUtils.getText("now-value")+" "+texts.get(i):
                                texts.get(i)
                        ).setColor("yellow").setItalic(false)
                );
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        stack.setName(jsonText);
        stack.setLore(lore.toArray(new JsonText[0]));
        return stack;
    }
    private List<Utils.Pair<String, Class>> getSupportedSettings(EditGUICoreInfo coreInfo){
        List<Utils.Pair<String, Class>> settings = new ArrayList<>();
        synchronized (coreInfo.supportedSettings) {
            for (String key : coreInfo.supportedSettings.keySet()) {
                settings.add(new Utils.Pair<>(key, coreInfo.supportedSettings.get(key)));
            }
        }
        return settings;
    }
    private NMSItemStack getRedstoneConnectItem(Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface> info){
        NMSItemStack stack = new NMSItemStack(new String[]{"redstone","redstone_wire"},1);
        String text = info.getKey();
        if(text.startsWith("@")){
            text=LangUtils.getText(text.substring(1));
        }
        JsonText jsonText = new JsonText(text+" ("+(info.getValue().isInput()?LangUtils.getText("input"):LangUtils.getText("output"))+")").setColor("red");
        if(info.getValue().isConnected()){
            Location loc = info.getValue().getBlockLocation();
            JsonText lore = new JsonText(LangUtils.getText("redstone-bridge-connected").replace("%%",loc.getWorld().getName()+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()));
            lore.setColor("red");
            stack.setLore(new JsonText[]{lore});
        }
        stack.setName(jsonText);
        return stack;
    }

    private void setBaseItems(){
        stateID = stateID + 1 & 32767;
        List<NMSItemStack> list = new ArrayList<>();
        for(int i=0;i<54;i++){
            list.add(0,NMSItemStack.EMPTY);
        }
        list.set(0,getItem0());
        list.set(1,getItem1());
        list.set(2,getItem2());
        list.set(3,getItem3());


        for(int i=9;i<18;i++){
            list.set(i,getItem9To17(i-9));
        }
        if(nowMode==0){
            EditGUICoreInfo coreInfo = getCoreInfoFromCore(screen.getCore());
            if(coreInfo!=null) {
                List<Utils.Pair<String, Class>> settings = getSupportedSettings(coreInfo);
                totalPage = calcPage(settings.size(),27);
                nowPage = calcNowPage(nowPage,totalPage);
                removeByPage(nowPage,settings,27);
                for (int i = 18; i < 45; i++) {
                    if (settings.size() > 0) {
                        list.set(i, getCoreSettingItem(settings.get(0)));
                        settings.remove(0);
                    } else {
                        break;
                    }
                }
            }

        }
        if(nowMode==1){
            List<EditGUICoreInfo> infos = new ArrayList<>();
            synchronized (registeredCoreInfos){
                for(EditGUICoreInfo i:registeredCoreInfos){
                    infos.add(i);
                }
            }
            totalPage = calcPage(infos.size(),27);
            nowPage = calcNowPage(nowPage,totalPage);
            removeByPage(nowPage,infos,27);
            for (int i = 18; i < 45; i++) {
                if (infos.size() > 0) {
                    list.set(i, getCoreInfoItem(infos.get(0)));
                    infos.remove(0);
                }else{
                    break;
                }
            }

        }
        if(nowMode==2){
            List<Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface>> objects = new ArrayList<>();
            if(screen.getCore()!=null){
                RedstoneBridge bridge = screen.getCore().getRedstoneBridge();
                List<Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface>> bridges = bridge.getRedstoneSignalInterfaces();
                for(Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface> i:bridges){
                    objects.add(i);
                }
            }
            if(screen.getCore()==null||objects.size()==0){
                totalPage=1;
                nowPage=0;
                list.set(18,getItem2NotSupported());
            }else{
                totalPage = calcPage(objects.size(),27);
                nowPage = calcNowPage(nowPage,totalPage);
                removeByPage(nowPage,objects,27);
                for (int i = 18; i < 45; i++) {
                    if (objects.size() > 0) {
                        list.set(i, getRedstoneConnectItem(objects.get(0)));
                        objects.remove(0);
                    }else{
                        break;
                    }
                }
            }
        }
        if(nowMode==0||nowMode==1||nowMode==2){
            if(nowPage>0){
                list.set(45, getItemSwitchPage(false,nowPage,totalPage));
            }
            if(nowPage<totalPage-1){
                list.set(53,  getItemSwitchPage(true,nowPage,totalPage));
            }
        }
        Object setContentPacket = OutWindowItemsPacket.create(containerID,stateID,list);
        CraftUtils.sendPacket(openedPlayer,setContentPacket);

    }

    private static int calcPage(int size,int eachPage) {
        return size %eachPage==0? size /eachPage: size /eachPage+1;
    }
    private static int calcNowPage(int nowPage,int totalPage){
        if(nowPage>=totalPage){
            nowPage = totalPage-1;
        }
        if(nowPage<0){
            nowPage = 0;
        }
        return nowPage;
    }
    private static void removeByPage(int page,List list,int eachPage){
        for(int p=0;p<page;p++){
            for (int i = 0; i < eachPage; i++) {
                list.remove(0);
            }
        }
    }

    public void openGUI(Player player, Integer clickX, Integer clickY, Utils.MouseClickType clickType) {
        if(openedPlayer!=null&& !openedPlayer.isOnline()){
            openedPlayer=null;
        }
        if(openedPlayer!=null){
            return;
        }
        if (player != openedPlayer) {
            openedPlayer = player;
            this.clickX=clickX;
            this.clickY=clickY;
            this.clickType=clickType;
            try {
                openContainer();
                setBaseItems();
            }catch (Exception e){
                forceClose();
            }
        }
    }
}
