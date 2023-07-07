package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static cn.mingbai.ScreenInMC.Controller.Item.CONTROLLER;
import static cn.mingbai.ScreenInMC.Controller.Item.onPlayerSwitchMode;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        PacketListener.addGlobalListener(e.getPlayer());
        for (Screen i : Screen.getAllScreens()) {
            if(i.getLocation().getWorld().equals(e.getPlayer().getWorld())){
                i.sendPutScreenPacket(e.getPlayer());
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        EditGUI.forceClose(e.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e) {
        for (Screen i : Screen.getAllScreens()) {
            if(i.getLocation().getWorld().equals(e.getPlayer().getWorld())){
                i.sendPutScreenPacket(e.getPlayer());
            }
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if(e.getFrom().distance(e.getTo())>1){
            EditGUI.forceClose(e.getPlayer());
        }
    }
    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent e){
        synchronized (RedstoneBridge.outputBlocks) {
            for(Location i:RedstoneBridge.outputBlocks){
                if(e.getBlock().getLocation().equals(i)){
                    e.setNewCurrent(e.getOldCurrent());
                }
            }
        }
    }
    private List<PacketListener> packetListeners = new ArrayList<>();
    public void loadPacketListener(Player player){
//        packetListeners.add(PacketListener.addListener())
    }
    public void unloadPacketListener() {
        synchronized (packetListeners){
            for(PacketListener i:packetListeners){
                PacketListener.removeListener(i);
                packetListeners.remove(i);
            }
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
        e.setCancelled(handleClick(e.getPlayer(),type));

    }
    private boolean handleClick(Player player,Utils.MouseClickType type){
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasCustomModelData() && meta.getCustomModelData() == CONTROLLER) {
                if(Item.onPlayerClick(player, item, type)) {
                    return true;
                }
            }
        }
        for (Screen i : Screen.getAllScreens()) {
            Location screenLocation = i.getLocation().clone();
            Utils.ScreenClickResult result = Utils.getScreenClickAt(player.getEyeLocation(), screenLocation, i.getFacing(), i.getWidth(), i.getHeight(), 1024);
            if (result.isClicked()) {
                int x = (int) (result.getMouseX() * 128);
                int y =  (int) (result.getMouseY() * 128);
                if (item != null && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasCustomModelData() && meta.getCustomModelData() == CONTROLLER) {
                        Item.onPlayerClickScreen(player,item,type,x,y,i);
                        return false;
                    }
                }
                i.getCore().onMouseClick(x,y, type);
                return false;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        if (e.getPreviousSlot() == e.getNewSlot() || !e.getPlayer().isSneaking()) {
            return;
        }
        ItemStack item = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasCustomModelData() && meta.getCustomModelData() == CONTROLLER) {
//                e.getPlayer().getInventory().setHeldItemSlot(e.getPreviousSlot());
                if (e.getPreviousSlot() - e.getNewSlot() == -1) {
                    onPlayerSwitchMode(e.getPlayer(), item, true, e.getPreviousSlot());
                } else if (e.getPreviousSlot() - e.getNewSlot() == 1) {
                    onPlayerSwitchMode(e.getPlayer(), item, false, e.getPreviousSlot());
                } else if (e.getPreviousSlot() == 0 && e.getNewSlot() == 8) {
                    onPlayerSwitchMode(e.getPlayer(), item, false, e.getPreviousSlot());
                } else if (e.getPreviousSlot() == 8 && e.getNewSlot() == 0) {
                    onPlayerSwitchMode(e.getPlayer(), item, true, e.getPreviousSlot());
                }
//                e.getPlayer().getInventory().setItem(e.getPreviousSlot(),item);
                e.setCancelled(true);
                return;
            }
        }
    }


}
