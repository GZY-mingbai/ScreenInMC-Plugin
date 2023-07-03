package cn.mingbai.ScreenInMC.Controller;

import cn.mingbai.ScreenInMC.Controller.EditGUI.EditGUICoreInfo.EditGUICoreSettingsList;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.PacketListener;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils.JsonText;
import cn.mingbai.ScreenInMC.Utils.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Function;

public class EditGUI {
    public static class EditGUICoreInfo{
        public interface EditGUICoreSettingsList{
            String[] getList();
            int getNowValue();
            void setNowValue(int index);
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
        openedPlayer.playSound(openedPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS,10,2);
    }
    private static final String BookAuthor = "ScreenInMC";
    private Utils.Pair<ItemStack, CompoundTag> getBasicBook(){
        ItemStack itemStack = new ItemStack(Items.WRITTEN_BOOK,1);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString("author",BookAuthor);
        tag.putString("title",LangUtils.getText("controller-editor-gui-title"));
        tag.putInt("generation",0);
        tag.putBoolean("resolved",true);
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name",
                new JsonText(LangUtils.getText("controller-editor-gui-title"))
                        .setColor("gold")
                        .toJSONWithoutExtra()
        );
        tag.put("display",displayTag);
        return new Utils.Pair<>(itemStack,tag);
    }

    public abstract class EditGUISubWindow{
        private static Map<Player,List<EditGUISubWindow>> openedWindows = new HashMap<>();
        private BukkitRunnable runnable;
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
                window.runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(finalPlayer.isOnline()&&finalPlayer!=null) {
                            List<EditGUISubWindow> list = openedWindows.get(finalPlayer);
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
                while (openedWindows.get(player).size() > 0) {
                    closeTopWindow(player);
                }
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
        public NumberInputWindow(boolean isDouble){
            this.isDouble=isDouble;
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
                        BukkitRunnable runnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                String str = askForString();
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
            Utils.Pair<ItemStack, CompoundTag> itemAndTag = getBasicBook();
            ItemStack item = itemAndTag.getKey();
            CompoundTag tag = itemAndTag.getValue();
            ListTag pages = new ListTag();
            String[] levels;
            if(isDouble){
                levels = new String[]{"1","0.1","0.01"};
            }else{
                levels = new String[]{"100","10","1"};
            }
            pages.add(StringTag.valueOf(
                    new JsonText(
                            LangUtils.getText("ask-type")
                                    .replace("%%",
                                            isDouble?LangUtils.getText("type-double"):LangUtils.getText("type-int")
                                    )+ "\n"
                    ).setColor("black")
                            .addExtra(
                                    new JsonText("[-"+levels[0]+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" remove "+levels[0]
                                            ))
                                            .setColor("dark_purple")
                            ).addExtra(
                                    new JsonText("[-"+levels[1]+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" remove "+levels[1]
                                            ))
                                            .setColor("light_purple")
                            )
                            .addExtra(
                                    new JsonText("[-"+levels[2]+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" remove "+levels[2]
                                            ))
                                            .setColor("blue")
                            )
                            .addExtra(
                                    new JsonText("\n" + (isDouble?(String.format("%.4f",getNowValue())):(getNowValue())) + "\n")
                                            .setColor("dark_red")
                            )
                            .addExtra(
                                    new JsonText("[+"+levels[2]+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" add "+levels[2]
                                            ))
                                            .setColor("blue")
                            ).addExtra(
                                    new JsonText("[+"+levels[1]+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" add "+levels[1]
                                            ))
                                            .setColor("light_purple")
                            )
                            .addExtra(
                                    new JsonText("[+"+levels[0]+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" add "+levels[0]
                                            ))
                                            .setColor("dark_purple")
                            )
                            .addExtra(new JsonText("\n"))
                            .addExtra(
                                    new JsonText("["+LangUtils.getText("input-by-hand")+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" input"
                                            ))
                                            .setColor("dark_purple")
                            )
                            .addExtra(new JsonText("\n"))
                            .addExtra(
                                    new JsonText("["+LangUtils.getText("complete")+"]")
                                            .setClickEvent(new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/screen controller "+uuid.toString()+" close"
                                            ))
                                            .setColor("dark_purple")
                            )
                            .toJSONWithoutExtra()

            ));
            tag.put("pages", pages);
            ClientboundContainerSetSlotPacket packet1 = new ClientboundContainerSetSlotPacket(-2, 0, openedPlayer.getInventory().getHeldItemSlot(), item);
            CraftUtils.sendPacket(player,packet1);
        }

        @Override
        public void reopenWindow(Player player) {
            ClientboundOpenBookPacket packet = new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND);
            CraftUtils.sendPacket(player,packet);
    }

    @Override
        public void closeWindow(Player player) {
            removeControllerCommandCallback(uuid);
        }
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
            listener1 =PacketListener.addListener(new PacketListener(player, ServerboundContainerClickPacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener, Packet p) {
                    if(p instanceof ServerboundContainerClickPacket){
                        ServerboundContainerClickPacket packet  = (ServerboundContainerClickPacket) p;
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
            listener2 =PacketListener.addListener(new PacketListener(player, ServerboundContainerClosePacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener, Packet p) {
                    if(p instanceof ServerboundContainerClosePacket){
                        ServerboundContainerClosePacket packet  = (ServerboundContainerClosePacket) p;
                        if(packet.getContainerId()==containerID){
                            reopen=true;
                            return true;
                        }
                    }
                    return false;
                }
            }));
            reopenWindow(player);
            resetItems(player);
        }

        @Override
        public void reopenWindow(Player player) {
            if(reopen){
                Packet packet = new ClientboundOpenScreenPacket(
                        containerID,
                        MenuType.HOPPER,
                        new JsonText(LangUtils.getText("ask-type")
                                .replace("%%",
                                        LangUtils.getText("type-boolean")+"(×/√)"
                                )).setColor("gold").toComponent()
                );
                CraftUtils.sendPacket(player,packet);
                reopen=false;
            }
            resetItems(player);
        }
        private void resetItems(Player player){
            stateID = stateID + 1 & 32767;
            NonNullList list = NonNullList.create();
            list.add(0);
            list.add(0);
            list.add(0);
            list.add(0);
            list.add(0);
            ItemStack itemStack = new ItemStack(Items.RED_WOOL,1);
            CompoundTag tag = itemStack.getOrCreateTag();
            CompoundTag displayTag = new CompoundTag();
            displayTag.putString("Name",new JsonText("false (×)").setColor("red").toJSONWithoutExtra());
            tag.put("display",displayTag);
            list.set(0,itemStack);
            itemStack = new ItemStack(Items.LIME_WOOL,1);
            tag = itemStack.getOrCreateTag();
            displayTag = new CompoundTag();
            displayTag.putString("Name",new JsonText("true (√)")
                    .setColor("green").toJSONWithoutExtra());
            tag.put("display",displayTag);
            list.set(1,ItemStack.EMPTY);
            list.set(2,ItemStack.EMPTY);
            list.set(3,ItemStack.EMPTY);
            list.set(4,itemStack);
            Packet packet = new ClientboundContainerSetContentPacket(
                    containerID,
                    stateID,list,ItemStack.EMPTY
            );
            CraftUtils.sendPacket(player,packet);
            packet = new ClientboundContainerSetDataPacket(containerID,0,0);
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



        public String getNowStringValue() {
            return nowStringValue;
        }
        private boolean reopen =true;
        @Override
        public void openWindow(Player player) {
            listener1 =PacketListener.addListener(new PacketListener(player, ServerboundContainerClickPacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener, Packet p) {
                    if(p instanceof ServerboundContainerClickPacket){
                        ServerboundContainerClickPacket packet  = (ServerboundContainerClickPacket) p;
                        if(packet.getContainerId()==containerID){
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
            listener2 =PacketListener.addListener(new PacketListener(player, ServerboundContainerClosePacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener, Packet p) {
                    if(p instanceof ServerboundContainerClosePacket){
                        ServerboundContainerClosePacket packet  = (ServerboundContainerClosePacket) p;
                        if(packet.getContainerId()==containerID){
                            reopen=true;
                            return true;
                        }
                    }
                    return false;
                }
            }));
            listener3 = PacketListener.addListener(new PacketListener(player, ServerboundRenameItemPacket.class, new PacketListener.PacketHandler() {
                @Override
                public boolean handle(PacketListener listener, Packet p) {
                    if(p instanceof ServerboundRenameItemPacket){
                        ServerboundRenameItemPacket packet  = (ServerboundRenameItemPacket) p;
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
            NonNullList list = NonNullList.create();
            list.add(0);
            list.add(0);
            list.add(0);
            ItemStack itemStack = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE,1);
            CompoundTag tag = itemStack.getOrCreateTag();
            CompoundTag displayTag = new CompoundTag();
            displayTag.putString("Name",new JsonText("").toJSONWithoutExtra());
            tag.put("display",displayTag);
            list.set(0,itemStack);
            itemStack = new ItemStack(Items.LIME_STAINED_GLASS_PANE,1);
            tag = itemStack.getOrCreateTag();
            displayTag = new CompoundTag();
            displayTag.putString("Name",new JsonText(LangUtils.getText("complete"))
                    .setColor("green").toJSONWithoutExtra());
            tag.put("display",displayTag);
            list.set(1,ItemStack.EMPTY);
            list.set(2,itemStack);
            Packet packet = new ClientboundContainerSetContentPacket(
                    containerID,
                    stateID,list,ItemStack.EMPTY
            );
            CraftUtils.sendPacket(player,packet);
            packet = new ClientboundContainerSetDataPacket(containerID,0,0);
            CraftUtils.sendPacket(player,packet);

        }

        @Override
        public void reopenWindow(Player player) {
            if(reopen){
                Packet packet = new ClientboundOpenScreenPacket(
                        containerID,
                        MenuType.ANVIL,
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
        public LocationInputWindow(boolean isVector){
            this.isVector=isVector;
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
                            BukkitRunnable runnable = new BukkitRunnable() {
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
                                        x = askForDouble();
                                        openWindow(player);
                                    }
                                    if(command[1].equals("y")){
                                        y = askForDouble();
                                        openWindow(player);
                                    }
                                    if(command[1].equals("z")){
                                        z = askForDouble();
                                        openWindow(player);
                                    }
                                    if(command[1].equals("xyz")){
                                        try {
                                            String[] xyz = askForString().split("[,\\s]+");
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
            Utils.Pair<ItemStack, CompoundTag> itemAndTag = getBasicBook();
            ItemStack item = itemAndTag.getKey();
            CompoundTag tag = itemAndTag.getValue();
            ListTag pages = new ListTag();
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
                        ClickEvent.Action.RUN_COMMAND,
                        "/screen controller "+uuid.toString()+" set world"
                )));
                jsonText.addExtra(new JsonText("\n"));
            }
            jsonText.addExtra(new JsonText("X: ").setColor("dark_red"));
            jsonText.addExtra(new JsonText(String.format("%.4f",x)+" ").setColor("red"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/screen controller "+uuid.toString()+" set x"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("Y: ").setColor("dark_red"));
            jsonText.addExtra(new JsonText(String.format("%.4f",y)+" ").setColor("red"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/screen controller "+uuid.toString()+" set y"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("Z: ").setColor("dark_red"));
            jsonText.addExtra(new JsonText(String.format("%.4f",z)+" ").setColor("red"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/screen controller "+uuid.toString()+" set z"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("set")+" XYZ]").setColor("gold").setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/screen controller "+uuid.toString()+" set xyz"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("use-now-player-location")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/screen controller "+uuid.toString()+" now player"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("use-now-block-location")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/screen controller "+uuid.toString()+" now block"
            )));
            jsonText.addExtra(new JsonText("\n"));
            jsonText.addExtra(new JsonText("["+LangUtils.getText("complete")+"]").setColor("gold").setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/screen controller "+uuid.toString()+" close"
            )));
            pages.add(StringTag.valueOf(
                    jsonText.toJSONWithoutExtra()
            ));
            tag.put("pages", pages);
            ClientboundContainerSetSlotPacket packet1 = new ClientboundContainerSetSlotPacket(-2, 0, openedPlayer.getInventory().getHeldItemSlot(), item);
            CraftUtils.sendPacket(player,packet1);
        }

        @Override
        public void reopenWindow(Player player) {
            ClientboundOpenBookPacket packet = new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND);
            CraftUtils.sendPacket(player,packet);
        }

        @Override
        public void closeWindow(Player player) {
            removeControllerCommandCallback(uuid);
        }
    }

    private int askForInteger(){
        NumberInputWindow window = new NumberInputWindow(false){
            @Override
            public void closeWindow(Player player) {
                super.closeWindow(player);
                reOpenContainer();
                setBaseItems();
                updateInventory();
            }
        };
        openAndWaitWindowClose(window);
        return (int)window.getNowValue();
    }

    public Player getOpenedPlayer() {
        return openedPlayer;
    }

    private double askForDouble(){
        NumberInputWindow window = new NumberInputWindow(true){
            @Override
            public void closeWindow(Player player) {
                super.closeWindow(player);
                reOpenContainer();
                setBaseItems();
                updateInventory();
            }
        };
        openAndWaitWindowClose(window);
        return (double)window.getNowValue();
    }
    private boolean askForBoolean(){
        BooleanInputWindow window = new BooleanInputWindow(){
            @Override
            public void closeWindow(Player player) {
                super.closeWindow(player);
                reOpenContainer();
                setBaseItems();
                updateInventory();
            }
        };
        openAndWaitWindowClose(window);
        return window.getNowBooleanValue();
    }
    private String askForString(){
        StringInputWindow window = new StringInputWindow(){
            @Override
            public void closeWindow(Player player) {
                super.closeWindow(player);
                reOpenContainer();
                setBaseItems();
                updateInventory();
            }
        };
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
    private String[] askForStringList(){
        return new String[]{};
    }
    private int askForStringFromList(String[] list){
        return 0;
    }
    private Location askForLocation(){
        LocationInputWindow window = new LocationInputWindow(false){
            @Override
            public void closeWindow(Player player) {
                super.closeWindow(player);
                reOpenContainer();
                setBaseItems();
                updateInventory();
            }
        };
        openAndWaitWindowClose(window);
        return (Location) window.getNowValue();
    }
    private Vector askForVector(){
        LocationInputWindow window = new LocationInputWindow(true){
            @Override
            public void closeWindow(Player player) {
                super.closeWindow(player);
                reOpenContainer();
                setBaseItems();
                updateInventory();
            }
        };
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

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(EditGUISubWindow.openedWindows.containsKey(openedPlayer)){
                    List<EditGUISubWindow> windows = EditGUISubWindow.openedWindows.get(openedPlayer);
                    synchronized (windows){
                        windows.get(windows.size()-1).closeWindow(openedPlayer);
                        windows.get(windows.size()-1).openWindow(openedPlayer);
                    }
                    return;
                }
                Utils.Pair<String,Class> item = list.get(index);
                String key = item.getKey();
                if (item.getValue().equals(Integer.class)) {
                    int i = askForInteger();
                    screen.getCore().setEditGUISettingValue(key,i);
                } else if (item.getValue().equals(Double.class) || item.getValue().equals(Float.class)) {
                    double d = askForDouble();
                    screen.getCore().setEditGUISettingValue(key,d);
                } else if (item.getValue().equals(Boolean.class)) {
                    boolean b = askForBoolean();
                    screen.getCore().setEditGUISettingValue(key,b);
                } else if (item.getValue().equals(String.class)) {
                    String str = askForString();
                    screen.getCore().setEditGUISettingValue(key,str);
                } else if (item.getValue().equals(String[].class)) {
                    String[] a = askForStringList();
                    screen.getCore().setEditGUISettingValue(key,a);
                } else if (item.getValue().equals(Location.class)) {
                    Location l = askForLocation();
                    screen.getCore().setEditGUISettingValue(key,l);
                } else if (item.getValue().equals(Vector.class)) {
                    Vector v = askForVector();
                    screen.getCore().setEditGUISettingValue(key,v);
                } else if (EditGUICoreSettingsList.class.isAssignableFrom(item.getValue())) {
                    try {
                        EditGUICoreSettingsList l = (EditGUICoreSettingsList) item.getValue().getDeclaredConstructor().newInstance();
                        int i = askForStringFromList(l.getList());
                        screen.getCore().setEditGUISettingValue(key,i);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        runnable.runTaskAsynchronously(Main.thisPlugin());

    }
    private void reOpenContainer() {
        if (openedPlayer != null) {
            JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-gui-title"));
            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(containerID, MenuType.GENERIC_9x6, jsonText.toComponent());
            CraftUtils.sendPacket(openedPlayer, packet);
        }
    }
    private Integer clickX;
    private Integer clickY;
    private Utils.MouseClickType clickType;
    private void updateInventory(){
        openedPlayer.updateInventory();
        ClientboundContainerSetSlotPacket setSlotPacket = new ClientboundContainerSetSlotPacket(-1,0,-1, CraftUtils.itemBukkitToNMS(openedPlayer.getItemOnCursor()));
        CraftUtils.sendPacket(openedPlayer,setSlotPacket);
    }
    PacketListener listener1;
    PacketListener listener2;

    private void openContainer(){
        listener1 = PacketListener.addListener(new PacketListener(openedPlayer, ServerboundContainerClickPacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener, Packet p) {
                if (p instanceof ServerboundContainerClickPacket) {
                    ServerboundContainerClickPacket packet = (ServerboundContainerClickPacket) p;
                    if (packet.getContainerId() == containerID) {
                        updateInventory();
                        if(packet.getSlotNum()>=0&&packet.getSlotNum()<=8){
                            nowMode = (short) packet.getSlotNum();
                            nowPage=0;
                            sendSwitchModeSound();
                        }
                        if(packet.getSlotNum()>=9&&packet.getSlotNum()<=17){
                            nowMode = (short) (packet.getSlotNum()-9);
                            nowPage=0;
                            sendSwitchModeSound();
                        }
                        if(nowMode==0||nowMode==1) {
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
                        setBaseItems();
                        return true;
                    }
                }
                return false;
            }

        }));
        listener2 =  PacketListener.addListener(new PacketListener(openedPlayer, ServerboundContainerClosePacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener, Packet p) {
                if (p instanceof ServerboundContainerClosePacket) {
                    ServerboundContainerClosePacket packet = (ServerboundContainerClosePacket) p;
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
    public static void forceClose(Player player){
        for(Screen i:Screen.getAllScreens()){
            if(player.equals(i.getEditGUI().openedPlayer)){
                i.getEditGUI().onClose();
                Packet packet = new ClientboundContainerClosePacket(i.getEditGUI().containerID);
                CraftUtils.sendPacket(player,packet);
            }
        }
    }
    private void onClose(){
        PacketListener.removeListener(listener1);
        PacketListener.removeListener(listener2);
        List<EditGUISubWindow> list = EditGUISubWindow.openedWindows.get(openedPlayer);
        if(list!=null){
            EditGUISubWindow.closeAllWindows(openedPlayer);
        }
        openedPlayer=null;
    }
    private ItemStack getItem0(){
        ItemStack stack = new net.minecraft.world.item.ItemStack(Items.BLACK_GLAZED_TERRACOTTA);
        stack.setCount(1);
        CompoundTag displayTag = new CompoundTag();
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-title")).setColor("gold");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-id"))
                .addExtra(new JsonText(String.valueOf(screen.getID())).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-location"))
                .addExtra(
                        new JsonText(screen.getLocation().getWorld().getName()+" X:"+
                                screen.getLocation().getBlockX()+" Y:"+
                                screen.getLocation().getBlockY()+" Z:"+
                                screen.getLocation().getBlockZ()
                        ).setColor("yellow").setItalic(false)
                )
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-facing"))
                .addExtra(new JsonText(screen.getFacing().getTranslatedFacingName()).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-size"))
                .addExtra(new JsonText(screen.getWidth()+"x"+screen.getHeight()).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-core"))
                .addExtra(new JsonText(screen.getCore().getCoreName()).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-clicked"))
                .addExtra(new JsonText(clickType.getTranslatedName()+" X:"+clickX+" Y:"+clickY).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        displayTag.putString("Name",jsonText.toJSON());
        displayTag.put("Lore",lore);

        stack.getOrCreateTag().put("display",displayTag);
        return stack;
    }
    private ItemStack getItem1(){
        ItemStack stack = new net.minecraft.world.item.ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
        stack.setCount(1);
        CompoundTag displayTag = new CompoundTag();
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-replace-core-title")).setColor("light_purple");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-replace-core-info"))
                .setItalic(false)
                .setColor("light_purple").toJSONWithoutExtra()));
        displayTag.putString("Name",jsonText.toJSON());
        displayTag.put("Lore",lore);
        stack.getOrCreateTag().put("display",displayTag);
        return stack;
    }
    private ItemStack getCoreInfoItem(EditGUICoreInfo info){
        ItemStack stack = CraftUtils.itemBukkitToNMS(new org.bukkit.inventory.ItemStack(info.icon));
        stack.setCount(1);
        CompoundTag displayTag = new CompoundTag();
        JsonText jsonText = new JsonText(info.name).setColor(info.themeColor);
        ListTag lore = new ListTag();
        for(String i:info.details.split("\n")){
            lore.add(StringTag.valueOf(new JsonText(i)
                    .setItalic(false)
                    .setColor(info.themeColor).toJSONWithoutExtra()));
        }
        try {
            if(info.core.getClass().equals(screen.getCore().getClass())){
                lore.add(StringTag.valueOf(new JsonText("").toJSONWithoutExtra()));
                lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("current-selection"))
                        .setItalic(false)
                        .setColor("gold").toJSONWithoutExtra()));
            }
        }catch (Exception e){}
        displayTag.putString("Name",jsonText.toJSON());
        displayTag.put("Lore",lore);
        stack.getOrCreateTag().put("display",displayTag);
        return stack;
    }
    private short nowMode=0; //0=Info Mode //1=Select Core Mode
    private int nowPage=0;
    private int totalPage = 0;
    private ItemStack getItem9To17(int i){
        ItemLike type = Items.WHITE_STAINED_GLASS_PANE;
        if(i==nowMode){
            if(nowMode==0){
                type = Items.YELLOW_STAINED_GLASS_PANE;
            }else{
                type = Items.LIME_STAINED_GLASS_PANE;
            }
        }
        ItemStack stack = new net.minecraft.world.item.ItemStack(type);
        stack.setCount(1);
        CompoundTag displayTag = new CompoundTag();
        JsonText jsonText = new JsonText(" ");
        displayTag.putString("Name",jsonText.toJSON());
        stack.getOrCreateTag().put("display",displayTag);
        return stack;
    }
    private ItemStack getItemSwitchPage(boolean next){
        ItemStack stack = new net.minecraft.world.item.ItemStack(Items.STONE_BUTTON);
        stack.setCount(1);
        CompoundTag displayTag = new CompoundTag();
        JsonText jsonText = new JsonText(next?
                LangUtils.getText("controller-editor-next-page"):
                LangUtils.getText("controller-editor-previous-page")
                ).setColor("gold");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(new JsonText(
                LangUtils.getText("controller-editor-all-page")
                        .replace("%now%",String.valueOf(nowPage+1))
                        .replace("%all%",String.valueOf(totalPage))
                )
                .setItalic(false)
                .setColor("yellow").toJSONWithoutExtra()));
        displayTag.putString("Name",jsonText.toJSON());
        displayTag.put("Lore",lore);
        stack.getOrCreateTag().put("display",displayTag);
        return stack;
    }
    public ItemStack getCoreSettingItem(Utils.Pair<String, Class> settings){
        ItemStack stack = new net.minecraft.world.item.ItemStack(Items.REDSTONE_BLOCK);
        stack.setCount(1);
        CompoundTag displayTag = new CompoundTag();
        JsonText jsonText = new JsonText(settings.getKey()).setColor("red");
        ListTag lore = new ListTag();
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
        lore.add(StringTag.valueOf(new JsonText(
                LangUtils.getText("type")+" "+type
                )
                .setItalic(false)
                .setColor("yellow").toJSONWithoutExtra()));
        try{
            Object nowValue = screen.getCore().getEditGUISettingValue(settings.getKey());
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
                    EditGUICoreSettingsList list = (EditGUICoreSettingsList) nowValue;
                    texts.add(list.getList()[list.getNowValue()]);
                }
            }
            for(int i=0;i<texts.size();i++) {
                lore.add(StringTag.valueOf(
                        new JsonText(i==0?
                                LangUtils.getText("now-value")+" "+texts.get(i):
                                texts.get(i)
                        ).setColor("yellow").setItalic(false).toJSONWithoutExtra()
                ));
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        displayTag.putString("Name",jsonText.toJSON());
        displayTag.put("Lore",lore);
        stack.getOrCreateTag().put("display",displayTag);
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
    private void setBaseItems(){
        stateID = stateID + 1 & 32767;
        NonNullList list = NonNullList.create();
        for(int i=0;i<54;i++){
            list.add(0,new ItemStack(Items.AIR,1));
        }
        list.set(0,getItem0());
        list.set(1,getItem1());
        for(int i=9;i<18;i++){
            list.set(i,getItem9To17(i-9));
        }
        if(nowMode==0){
            EditGUICoreInfo coreInfo = getCoreInfoFromCore(screen.getCore());
            if(coreInfo!=null) {
                List<Utils.Pair<String, Class>> settings = getSupportedSettings(coreInfo);
                calcPage(settings.size());
                for (int p = 0; p < nowPage; p++) {
                    for (int i = 18; i < 45; i++) {
                        settings.remove(0);
                    }
                }
                for (int i = 18; i < 45; i++) {
                    if (settings.size() > 0) {
                        list.set(i, getCoreSettingItem(settings.get(0)));
                        settings.remove(0);
                    } else {
                        break;
                    }
                }
            }
            if(nowPage>0){
                list.set(45, getItemSwitchPage(false));
            }
            if(nowPage<totalPage-1){
                list.set(53,  getItemSwitchPage(true));
            }
        }
        if(nowMode==1){
            List<EditGUICoreInfo> infos = new ArrayList<>();
            synchronized (registeredCoreInfos){
                for(EditGUICoreInfo i:registeredCoreInfos){
                    infos.add(i);
                }
            }
            calcPage(infos.size());
            for(int p=0;p<nowPage;p++){
                for (int i = 18; i < 45; i++) {
                    infos.remove(0);
                }
            }
            for (int i = 18; i < 45; i++) {
                if (infos.size() > 0) {
                    list.set(i, getCoreInfoItem(infos.get(0)));
                    infos.remove(0);
                }else{
                    break;
                }
            }
            if(nowPage>0){
                list.set(45, getItemSwitchPage(false));
            }
            if(nowPage<totalPage-1){
                list.set(53,  getItemSwitchPage(true));
            }
        }
        ClientboundContainerSetContentPacket setContentPacket = new ClientboundContainerSetContentPacket(containerID,stateID,list, ItemStack.EMPTY);
        CraftUtils.sendPacket(openedPlayer,setContentPacket);

    }

    private void calcPage(int size) {
        totalPage = size %27==0? size /27: size /27+1;
        if(nowPage>=totalPage){
            nowPage = totalPage-1;
        }
        if(nowPage<0){
            nowPage = 0;
        }
    }

    public void openGUI(Player player, Integer clickX, Integer clickY, Utils.MouseClickType clickType) {
        if(openedPlayer!=null&& !openedPlayer.isOnline()){
            openedPlayer=null;
        }
        if (player != openedPlayer) {
            openedPlayer = player;
            this.clickX=clickX;
            this.clickY=clickY;
            this.clickType=clickType;
            openContainer();
            setBaseItems();
        }
    }
}
