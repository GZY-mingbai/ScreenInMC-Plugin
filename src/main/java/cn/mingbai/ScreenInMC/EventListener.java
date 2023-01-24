package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import static cn.mingbai.ScreenInMC.Controller.Item.CONTROLLER;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (Screen i : Screen.getAllScreens()) {
            i.sendPutScreenPacket(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e) {
        for (Screen i : Screen.getAllScreens()) {
            i.sendPutScreenPacket(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Utils.MouseClickType type = Utils.MouseClickType.LEFT;
        if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            type = Utils.MouseClickType.LEFT;
        } else if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            type = Utils.MouseClickType.RIGHT;
        }
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item!=null&&item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
            if(meta.hasCustomModelData()&&meta.getCustomModelData()==CONTROLLER){
                Item.onPlayerClick(player,item,type);
                e.setCancelled(true);
                return;
            }
        }
        for (Screen i : Screen.getAllScreens()) {
            Location screenLocation = i.getLocation().clone();
            Utils.ScreenClickResult result = Utils.getScreenClickAt(player.getEyeLocation(),screenLocation,i.getFacing(),i.getWidth(),i.getHeight(),1024);
            if(result.isClicked()){
                i.getCore().onMouseClick((int) (result.getMouseX() * 128), (int) (result.getMouseY() * 128),type);
                return;
            }
        }
    }

}
