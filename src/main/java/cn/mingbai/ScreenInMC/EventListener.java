package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Screen.Screen;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        for(Screen i:Screen.getAllScreens()){
            i.sendPutScreenPacket(e.getPlayer());
        }
    }
    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e){
        for(Screen i:Screen.getAllScreens()){
            i.sendPutScreenPacket(e.getPlayer());
        }
    }
}
