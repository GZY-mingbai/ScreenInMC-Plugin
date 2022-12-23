package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Core implements Cloneable{
    private static final List<Core> allCores = Collections.synchronizedList(new ArrayList<>());
    private String coreName;

    public String getCoreName() {
        return coreName;
    }
    public Core(String name){
        this.coreName=name;
    }

    public static void addCore(Core core){
        if(core.screen!=null){
            throw new RuntimeException("This core has been loaded.");
        }
        allCores.add(core);
    }
    public static Core[] getAllCore(){
        Core[] result = new Core[allCores.size()];
        for(int i=0;i< allCores.size();i++){
            result[i]=allCores.get(i);
        }
        return result;
    }
    public static void removeCore(Core core){
        allCores.remove(core);
    }
    protected Screen screen;

    public void create(Screen screen) {
        this.screen = screen;
        screen.setCore(this);
        onCreate();
    }
    public void unload(){
        if(screen!=null){
            onUnload();
        }
    }

    public abstract void onCreate();
    public abstract void onUnload();

    public abstract void onMouseClick(int x, int y, Utils.MouseClickType type);
    public abstract void onTextInput(String text);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
