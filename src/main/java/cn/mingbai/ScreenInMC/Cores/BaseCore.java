package cn.mingbai.ScreenInMC.Cores;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.function.Function;

public class BaseCore extends Core {
    public BaseCore(Function<Screen,Void> onRender){
        this.onRender = onRender;
    }
    public BaseCore(){
    }
    private Function<Screen,Void> onRender;

    public void setOnRender(Function<Screen, Void> onRender) {
        this.onRender = onRender;
    }

    @Override
    public void onCreate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                onRender.apply(screen);
            }
        }.runTaskTimerAsynchronously(Main.thisPlugin(),0,1L);
    }

    @Override
    public void onMouseClick(int x, int y) {

    }
}
