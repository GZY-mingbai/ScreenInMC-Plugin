package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.JSONUtils.JSONUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class Core implements Cloneable {
    public interface StoredData{
        StoredData clone();
    }
    private static final List<Core> allCores = Collections.synchronizedList(new ArrayList<>());
    private String coreName;
    private StoredData storedData;
    private Screen screen;

    public Core(String name) {
        this.coreName = name;
    }

    public static void addCore(Core core) {
        if (core.screen != null) {
            throw new RuntimeException("This core has been loaded.");
        }
        allCores.add(core);
        core.storedData = core.createStoredData();
        core.registerRedstoneBridge();
        core.addToEditGUI();
    }

    public void reRender(){
        if(screen!=null){
            screen.clearScreen();
        }
    }
    public static Core[] getAllCore() {
        Core[] result = new Core[allCores.size()];
        for (int i = 0; i < allCores.size(); i++) {
            result[i] = allCores.get(i);
        }
        return result;
    }
    public static Core createCore(String name){
        for (int i = 0; i < allCores.size(); i++) {
            if(allCores.get(i).getCoreName().equals(name)){
                try {
                    return (Core) allCores.get(i).clone();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public static void removeCore(Core core) {
        allCores.remove(core);
    }

    public static Core getCoreFromData(CoreData data) {
        for (Core i : allCores) {
            if (i.getClass().getName().equals(data.coreClassName)) {
                try {
                    Core core = (Core) i.clone();
                    if(data.data!=null&&core.storedData!=null) {
                        core.storedData = (StoredData) ((JSONUtils.JSONObject)data.data).write(core.storedData.getClass());
                    }
                    for(CoreData.RedstoneBridgeData r: data.redstone){
                        for(Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface> f : core.redstoneBridge.getRedstoneSignalInterfaces()){
                            if(f.getKey().equals(r.id)){
                                try {
                                    f.getValue().connect(new Location(Bukkit.getWorld(r.blockWorld),r.blockX,r.blockY,r.blockZ));
                                }catch (RedstoneBridge.RedstoneSignalInterface.ConnectException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    return core;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("Wrong data.");
    }

    public CoreData getCoreData() {
        CoreData data = new CoreData();
        data.coreClassName = this.getClass().getName();
        data.data = storedData;
        List<CoreData.RedstoneBridgeData> redstones = new ArrayList<>();
        for(Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface> i:redstoneBridge.getRedstoneSignalInterfaces()){
            if(i.getValue().isConnected()) {
                CoreData.RedstoneBridgeData bridgeData = new CoreData.RedstoneBridgeData();
                bridgeData.blockWorld = i.getValue().getBlockLocation().getWorld().getName();
                bridgeData.blockX = i.getValue().getBlockLocation().getBlockX();
                bridgeData.blockY = i.getValue().getBlockLocation().getBlockY();
                bridgeData.blockZ = i.getValue().getBlockLocation().getBlockZ();
                bridgeData.id = i.getKey();
                redstones.add(bridgeData);
            }
        }
        data.redstone = redstones.toArray(new CoreData.RedstoneBridgeData[0]);
        return data;
    }

    public StoredData getStoredData() {
        return storedData;
    }
    public abstract StoredData createStoredData();

    public String getCoreName() {
        return coreName;
    }

    public Screen getScreen() {
        return screen;
    }

    public void create(Screen screen) {
        isUnloaded=false;
        this.screen = screen;
        screen.clearScreen();
        screen.setCore(this);
        onCreate();
    }
    private boolean isUnloaded = false;
    public void unload() {
        if (screen != null) {
            isUnloaded=true;
            for(Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface> i: redstoneBridge.getRedstoneSignalInterfaces()){
                i.getValue().disconnect(false);
            }
            onUnload();
        }
    }

    public boolean isUnloaded() {
        return isUnloaded;
    }

    public abstract void onCreate();

    public abstract void onUnload();

    public abstract void onMouseClick(int x, int y, Utils.MouseClickType type);

    public abstract void onTextInput(String text);
    private RedstoneBridge redstoneBridge = new RedstoneBridge(this);
    public void registerRedstoneBridge(){

    }
    public void addToEditGUI(){
        EditGUI.registerCoreInfo(new EditGUI.EditGUICoreInfo(
                this.coreName,
                this,
                this.getClass().getName(),
                "gold",
                Material.BARRIER,
                new HashMap<>()));
    }
    public Object getEditGUISettingValue(String name){
        return null;
    }
    public void setEditGUISettingValue(String name,Object value){
    }

    public RedstoneBridge getRedstoneBridge() {
        return redstoneBridge;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Core core = (Core) super.clone();
        if(core.storedData!=null) {
            core.storedData = core.storedData.clone();
        }
        core.redstoneBridge = (RedstoneBridge) core.redstoneBridge.clone(core);
        return core;
    }

    public static class CoreData {
        public static class RedstoneBridgeData{
            public String blockWorld;
            public int blockX;
            public int blockY;
            public int blockZ;
            public String id;
        }
        public String coreClassName;
        public Object data;
        public RedstoneBridgeData[] redstone;
    }
}
