package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Controller.Item;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.*;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Diode;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

import static cn.mingbai.ScreenInMC.Controller.Item.CONTROLLER;
import static cn.mingbai.ScreenInMC.Controller.Item.onPlayerSwitchMode;
import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.getRepeaterFacing;
import static cn.mingbai.ScreenInMC.Utils.CraftUtils.NMSItemStack.getItemInHand;

public class EventListener implements Listener {
    protected static void init(){
        PacketListener.addListener(new PacketListener(null, InClickEntityPacket.class, new PacketListener.PacketHandler() {
            @Override
            public boolean handle(PacketListener listener, Player player, InPacket p) {
                if(p instanceof InClickEntityPacket){
                    InClickEntityPacket packet = (InClickEntityPacket)p;
                    int action = packet.getAction();
                    if(action==InClickEntityPacket.INTERACT||action==InClickEntityPacket.INTERACT_AT){
                        return handleClick(player, Utils.MouseClickType.RIGHT);
                    }
                    if(action==InClickEntityPacket.ATTACK){
                        return handleClick(player, Utils.MouseClickType.LEFT);
                    }
                }
                return false;
            }
        }));
    }
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
        PacketListener.removeGlobalListener(e.getPlayer());
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
        for(Screen i:Screen.getAllScreens()){
            if(!i.canSleep()){
                if(i.getLocation().getWorld().equals(e.getFrom().getWorld())&&i.getLocation().distance(e.getFrom())>Main.renderDistanceLimit&&i.getLocation().getWorld().equals(e.getTo().getWorld())&&i.getLocation().distance(e.getTo())<=Main.renderDistanceLimit){
                    if(i.getCore()!=null){
                        ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
                            @Override
                            public void run() {
                                i.getCore().reRender();
                            }
                        };
                        runnable.runTaskAsynchronously(Main.thisPlugin());
                    }
                }
            }
        }
        if(e.getFrom().distance(e.getTo())>8){
            EditGUI.forceClose(e.getPlayer());
        }
    }
    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent e){
        synchronized (RedstoneBridge.outputBlocks) {
            for(RedstoneBridge.RedstoneSignalInterface i:RedstoneBridge.outputBlocks){
                if(e.getBlock().getLocation().equals(i.getBlockLocation())){
                    if(!i.checkRedstoneRepeaterAndUpdate()) return;
//                    e.setNewCurrent(0);
                }
                BlockFace face = getRepeaterFacing(i.getBlockLocation().getBlock());
                if(face==null){
                    if(!i.checkRedstoneRepeaterAndUpdate())return;
                }else{
                    if(e.getBlock().getRelative(face).getLocation().equals(i.getBlockLocation())){
                        e.setNewCurrent(i.getNowPower());
                    }
                }
            }
        }
        synchronized (RedstoneBridge.inputBlocks) {
            for (RedstoneBridge.RedstoneSignalInterface i : RedstoneBridge.inputBlocks) {
                if (e.getBlock().getLocation().equals(i.getBlockLocation())) {
                    if (!i.checkRedstoneRepeaterAndUpdate()) return;
                }
                BlockFace face = getRepeaterFacing(i.getBlockLocation().getBlock());
                if(face==null){
                    if(!i.checkRedstoneRepeaterAndUpdate())return;
                }else{
//                    if(e.getBlock().getRelative(face).getLocation().equals(i.getBlockLocation())){
//                        e.setNewCurrent(e.getOldCurrent());
//                    }
                    if(e.getBlock().getRelative(face.getOppositeFace()).getLocation().equals(i.getBlockLocation())){
                        i.tryReceiveRedstoneSignal(e.getNewCurrent());
                    }
                }
            }
        }
    }
    private List<PacketListener> packetListeners = new ArrayList<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
//        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
//            if(CraftUtils.isRepeater(e.getClickedBlock().getType())){
//                synchronized (RedstoneBridge.outputBlocks) {
//                    for(RedstoneBridge.RedstoneSignalInterface i:RedstoneBridge.outputBlocks){
//                        if(e.getClickedBlock().getLocation().equals(i.getBlockLocation())){
//                            e.setCancelled(true);
//                        }
//                    }
//                }
//                synchronized (RedstoneBridge.inputBlocks) {
//                    for (RedstoneBridge.RedstoneSignalInterface i : RedstoneBridge.inputBlocks) {
//                        if(e.getClickedBlock().getLocation().equals(i.getBlockLocation())){
//                            e.setCancelled(true);
//                        }
//                    }
//                }
//            }
//        }

        Utils.MouseClickType type = Utils.MouseClickType.LEFT;
        if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            type = Utils.MouseClickType.LEFT;
        } else if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            type = Utils.MouseClickType.RIGHT;
        }
        boolean cancel = handleClick(e.getPlayer(),type);
        if(!e.isCancelled()) {
            e.setCancelled(cancel);
        }
    }
    private static boolean handleClick(Player player,Utils.MouseClickType type){
        ItemStack item = getItemInHand(player.getInventory());
        if (item != null && item.hasItemMeta()) {
            if (NMSItemStack.getCustomModelData(item) == CONTROLLER) {
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
                    if (NMSItemStack.getCustomModelData(item) == CONTROLLER) {
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
            if (NMSItemStack.getCustomModelData(item) == CONTROLLER) {
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
