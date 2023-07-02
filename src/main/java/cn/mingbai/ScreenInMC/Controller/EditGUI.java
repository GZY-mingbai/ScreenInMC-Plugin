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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private void sendSwitchModeSound(Player player){
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS,10,2);
    }
    public void openSetting(int index){
        if(screen.getCore()==null){
            return;
        }
        EditGUICoreInfo info = getCoreInfoFromCore(screen.getCore());
        List<Utils.Pair<String,Class>> list = getSupportedSettings(info);
        if(list.size()<=index){
            return;
        }
        Utils.Pair<String,Class> item = list.get(index);

    }
    private void openContainer(Player player, Integer clickX, Integer clickY, Utils.MouseClickType clickType){
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-gui-title"));
        PacketListener listener1 = PacketListener.addListener(new PacketListener(player, ServerboundContainerClickPacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener, Packet p) {
                if (p instanceof ServerboundContainerClickPacket) {
                    ServerboundContainerClickPacket packet = (ServerboundContainerClickPacket) p;
                    if (packet.getContainerId() == containerID) {
                        player.updateInventory();
                        ClientboundContainerSetSlotPacket setSlotPacket = new ClientboundContainerSetSlotPacket(-1,0,-1, CraftUtils.itemBukkitToNMS(player.getItemOnCursor()));
                        CraftUtils.sendPacket(player,setSlotPacket);
                        if(packet.getSlotNum()>=0&&packet.getSlotNum()<=8){
                            nowMode = (short) packet.getSlotNum();
                            nowPage=0;
                            sendSwitchModeSound(player);
                        }
                        if(packet.getSlotNum()>=9&&packet.getSlotNum()<=17){
                            nowMode = (short) (packet.getSlotNum()-9);
                            nowPage=0;
                            sendSwitchModeSound(player);
                        }
                        if(nowMode==0||nowMode==1) {
                            if (packet.getSlotNum() == 45 && nowPage > 0) {
                                nowPage--;
                                sendSwitchModeSound(player);
                            }
                            if (packet.getSlotNum() == 53 && nowPage < totalPage - 1) {
                                nowPage++;
                                sendSwitchModeSound(player);
                            }
                        }
                        if(nowMode==0){
                            if(packet.getSlotNum()>=18 && packet.getSlotNum()<=44) {
                                int index = nowPage * 27 + packet.getSlotNum() - 18;
                                openSetting(index);
                                sendSwitchModeSound(player);
                            }
                        }
                        if(nowMode==1){
                            if(packet.getSlotNum()>=18 && packet.getSlotNum()<=44) {
                                int index = nowPage * 27 + packet.getSlotNum() - 18;
                                switchCore(index);
                                sendSwitchModeSound(player);
                            }
                        }
                        setBaseItems(player, clickX, clickY, clickType);
                        return true;
                    }
                }
                return false;
            }

        }));
        PacketListener listener2 =  PacketListener.addListener(new PacketListener(player, ServerboundContainerClosePacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener, Packet p) {
                if (p instanceof ServerboundContainerClosePacket) {
                    ServerboundContainerClosePacket packet = (ServerboundContainerClosePacket) p;
                    if (packet.getContainerId() == containerID) {
                        PacketListener.removeListener(listener1);
                        PacketListener.removeListener(listener);
                        openedPlayer=null;
                        return true;
                    }
                }
                return false;
            }
        }));
        ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(containerID, MenuType.GENERIC_9x6, jsonText.toComponent());
        CraftUtils.sendPacket(player,packet);

    }
    private ItemStack getItem0(Player player,Integer clickX, Integer clickY, Utils.MouseClickType clickType){
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
            type = LangUtils.getText("type-boolean");
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
    private void setBaseItems(Player player,Integer clickX, Integer clickY, Utils.MouseClickType clickType){
        stateID = stateID + 1 & 32767;
        NonNullList list = NonNullList.create();
        for(int i=0;i<54;i++){
            list.add(0,new ItemStack(Items.AIR,1));
        }
        list.set(0,getItem0(player,clickX,clickY,clickType));
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
        CraftUtils.sendPacket(player,setContentPacket);

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
            openContainer(player, clickX, clickY, clickType);
            setBaseItems(player, clickX, clickY, clickType);
            openedPlayer = player;
        }
    }
}
