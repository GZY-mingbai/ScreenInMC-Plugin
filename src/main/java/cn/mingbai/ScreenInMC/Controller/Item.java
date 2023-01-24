package cn.mingbai.ScreenInMC.Controller;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.common.xml.XmlEscapers;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.chat.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static org.bukkit.inventory.ItemFlag.*;

public class Item {
    public static final int CONTROLLER = 1575771175;
    public static final int PLACE_MODE = 0; //放置模式
    public static class ItemData{
        public int nowMode = PLACE_MODE;
        public String world = null;
        public Integer p1x=null,p1y=null,p1z=null;
        public Integer p2x=null,p2y=null,p2z=null;
        public Integer w=null,h=null;
    }
    private static BukkitRunnable runnable=null;
    private static Color BLUE;
    private static Color RED;
    private static Color YELLOW;
    private static Color GREEN;
    public static void onEnable(){
        BLUE = Color.fromRGB(ChatColor.BLUE.getColor().getRed(),ChatColor.BLUE.getColor().getGreen(),ChatColor.BLUE.getColor().getBlue());
        RED = Color.fromRGB(ChatColor.RED.getColor().getRed(),ChatColor.RED.getColor().getGreen(),ChatColor.RED.getColor().getBlue());
        YELLOW = Color.fromRGB(ChatColor.YELLOW.getColor().getRed(),ChatColor.YELLOW.getColor().getGreen(),ChatColor.YELLOW.getColor().getBlue());
        GREEN = Color.fromRGB(ChatColor.GREEN.getColor().getRed(),ChatColor.GREEN.getColor().getGreen(),ChatColor.GREEN.getColor().getBlue());

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player: Bukkit.getOnlinePlayers()){
                    if(player.isOp()){
                        ItemStack item = player.getInventory().getItemInMainHand();
                        if(item!=null&&item.hasItemMeta()){
                            ItemMeta meta = item.getItemMeta();
                            if(meta.hasCustomModelData()&&meta.getCustomModelData()==CONTROLLER){
                                if(meta.hasLocalizedName()){
                                    try{
                                        ItemData data = Main.getGson().fromJson(meta.getLocalizedName(), ItemData.class);
                                        BaseComponent extra = new TextComponent("");
                                        switch (data.nowMode){
                                            case PLACE_MODE:
                                                Location eyeLoc = player.getEyeLocation();
                                                Location closest;
                                                if(data.world!=null&&data.p1x!=null&&data.p1y!=null&&data.p1z!=null){
                                                    TextComponent tc = new TextComponent(" "+LangUtils.getText("controller-point-start").replace("%%",
                                                            data.p1x+","+data.p1y+","+data.p1z));
                                                    tc.setColor(ChatColor.BLUE);
                                                    extra.addExtra(tc);
                                                    World world = Bukkit.getWorld(data.world);
                                                    if(data.p2x!=null&&data.p2y!=null&&data.p2z!=null){
                                                        tc = new TextComponent(" "+LangUtils.getText("controller-point-end").replace("%%",
                                                                data.p2x+","+data.p2y+","+data.p2z));
                                                        tc.setColor(ChatColor.RED);
                                                        extra.addExtra(tc);
                                                        tc = new TextComponent(" "+data.w+"x"+data.h);
                                                        tc.setColor(ChatColor.YELLOW);
                                                        extra.addExtra(tc);
                                                        world.spawnParticle(Particle.REDSTONE,(double) data.p2x,(double)data.p2y,(double)data.p2z,0,0,0,0,0, new Particle.DustOptions(RED,1));
                                                        Utils.Pair<Utils.Pair<Screen.Facing,Location>,Utils.Pair<Screen.Facing,Location>> locations = getScreenLocations(world,data.p1x,data.p1y,data.p1z,data.p2x, data.p2y, data.p2z);
                                                        if(data.w!=null&&data.h!=null){
                                                            Utils.ScreenClickResult result1 = Utils.getScreenClickAt(player.getEyeLocation(),locations.getKey().getValue(),locations.getKey().getKey(),data.w,data.h,5);
                                                            Utils.ScreenClickResult result2 = Utils.getScreenClickAt(player.getEyeLocation(),locations.getValue().getValue(),locations.getValue().getKey(),data.w,data.h,5);
                                                            if(result1.isClicked()||result2.isClicked()){
                                                                spawnRectParticle(world,data.p1x,data.p1y,data.p1z,data.p2x, data.p2y, data.p2z,YELLOW);
                                                            }
                                                        }
                                                        closest=getClosestBlock(eyeLoc);
                                                    }else{
                                                        closest=getClosestPlane(eyeLoc,data.p1x,data.p1y,data.p1z);
                                                        if(closest!=null){
                                                            Utils.Pair<Integer,Integer> size = getScreenSize(new Location(world,data.p1x,data.p1y,data.p1z),closest);
                                                            tc = new TextComponent(" "+size.getKey()+"x"+size.getValue());
                                                            tc.setColor(ChatColor.YELLOW);
                                                            extra.addExtra(tc);
                                                        }
                                                    }
                                                    world.spawnParticle(Particle.REDSTONE,(double) data.p1x,(double)data.p1y,(double)data.p1z,0,0,0,0,0, new Particle.DustOptions(BLUE,1));
                                                }else{
                                                    closest=getClosestBlock(eyeLoc);
                                                }
                                                if(closest==null) {
                                                    Vector eyeVec = eyeLoc.toVector();
                                                    Vector direction = eyeLoc.getDirection();
                                                    direction.multiply(5);
                                                    eyeVec.add(direction);
                                                    Location point = eyeVec.toLocation(eyeLoc.getWorld());
                                                    eyeLoc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, point, 0, 0, 0, 0, 0);
                                                }else{
                                                    eyeLoc.getWorld().spawnParticle(Particle.REDSTONE, closest, 0, 0, 0, 0, 0, new Particle.DustOptions(GREEN,1));
                                                }
                                                break;
                                        }
                                        TextComponent component = new TextComponent(LangUtils.getText("controller-item-now-mode")+" "+getModeName(data.nowMode));
                                        component.addExtra(extra);
                                        component.setColor(ChatColor.GOLD);
                                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,component);

                                    }catch (Exception e){
                                        e.printStackTrace();
                                        player.getInventory().setItemInMainHand(null);
                                    }
                                }else{
                                    player.getInventory().setItemInMainHand(null);
                                }
                            }
                        }
                    }

                }
            }
        };
        runnable.runTaskTimer(Main.thisPlugin(),0L,1L);
    }
    public static void onDisable(){
        if(runnable!=null){
            runnable.cancel();
        }
    }
    public static void giveItem(Player player){
        ItemStack item = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6"+LangUtils.getText("controller-item-name"));
        meta.setCustomModelData(CONTROLLER);
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.DURABILITY,1,false);
        meta.addItemFlags(HIDE_ENCHANTS,
                HIDE_ATTRIBUTES,
                HIDE_UNBREAKABLE,
                HIDE_DESTROYS,
                HIDE_PLACED_ON,
                HIDE_POTION_EFFECTS,
                HIDE_DYE);
        meta.setLocalizedName(Main.getGson().toJson(new ItemData()));
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }
    public static String getModeName(int id){
        switch (id){
            case PLACE_MODE:
                return LangUtils.getText("controller-item-place-mode");
        }
        return LangUtils.getText("controller-item-unknown-mode");
    }
    private static void spawnLineParticle(World world,int x1,int y1,int z1,int x2,int y2,int z2,Color color){
        Vector v1 = new Vector(x1,y1,z1);
        Vector v2 = new Vector(x2,y2,z2);
        Vector v3 = v2.clone().subtract(v1);
        double d1 = v1.distance(v2);
        v3.divide(new Vector(d1,d1,d1));
        v3.multiply(0.1);
        for(double i=0;i<d1;i+=0.1){
            world.spawnParticle(Particle.REDSTONE, v1.getX(),v1.getY(),v1.getZ(),0,0,0,0,0, new Particle.DustOptions(color,1));
            v1.add(v3);
        }
    }
    private static void spawnRectParticle(World world,int x1,int y1,int z1,int x2,int y2,int z2,Color color){
        if(x1==x2){
            spawnLineParticle(world,x1,y1,z1,x1,y1,z2,color);
            spawnLineParticle(world,x1,y1,z1,x1,y2,z1,color);
            spawnLineParticle(world,x1,y2,z2,x1,y2,z1,color);
            spawnLineParticle(world,x1,y2,z2,x1,y1,z2,color);
        }
        if(y1==y2){
            spawnLineParticle(world,x1,y1,z1,x1,y1,z2,color);
            spawnLineParticle(world,x1,y1,z1,x2,y1,z1,color);
            spawnLineParticle(world,x2,y1,z2,x2,y1,z1,color);
            spawnLineParticle(world,x2,y1,z2,x1,y1,z2,color);
        }
        if(z1==z2){
            spawnLineParticle(world,x1,y1,z1,x2,y1,z1,color);
            spawnLineParticle(world,x1,y1,z1,x1,y2,z1,color);
            spawnLineParticle(world,x2,y2,z1,x1,y2,z1,color);
            spawnLineParticle(world,x2,y2,z1,x2,y1,z1,color);
        }
    }
    private static void ForInRange(int f, int t, IntConsumer consumer){
        if(f<=t){
            for(int i=f;i<t+1;i++){
                consumer.accept(i);
            }
        }else{
            for(int i=f;i>t-1;i--){
                consumer.accept(i);
            }
        }
    }
    public static void onPlayerClick(Player player, ItemStack item, Utils.MouseClickType type){
        try {
            ItemMeta meta = item.getItemMeta();
            ItemData data = Main.getGson().fromJson(meta.getLocalizedName(), ItemData.class);
            if (data.nowMode==PLACE_MODE) {
                if(type.equals(Utils.MouseClickType.LEFT)) {
                    Location eyeLoc = player.getEyeLocation();
                    Location closest = getClosestBlock(eyeLoc);
                    if (closest == null) {
                        Main.sendMessage(player, LangUtils.getText("controller-block-not-found"));
                        clearPoints(player, data);
                    } else {
                        data.world = closest.getWorld().getName();
                        data.p1x = (int) closest.getX();
                        data.p1y = (int) closest.getY();
                        data.p1z = (int) closest.getZ();
                        data.p2x=null;
                        data.p2y=null;
                        data.p2z=null;
                        Main.sendMessage(player, "§9"+LangUtils.getText("controller-set-point-start").replace("%%",
                                (int)closest.getX()+","+(int)closest.getY()+","+(int)closest.getZ()));
                    }
                    meta.setLocalizedName(Main.getGson().toJson(data));
                    item.setItemMeta(meta);
                    return;
                }
                if(type.equals(Utils.MouseClickType.RIGHT)) {
                    if(data.world==null||data.p1x==null||data.p1y==null||data.p1z==null){
                        return;
                    }
                    Location eyeLoc = player.getEyeLocation();
                    Location closest = getClosestPlane(eyeLoc,data.p1x,data.p1y,data.p1z);
                    if (closest == null) {
                        Main.sendMessage(player, LangUtils.getText("controller-block-not-found"));
                        clearPoints(player, data);
                    }else {
                        String worldName = closest.getWorld().getName();
                        if (!data.world.equals(worldName)) {
                            Main.sendMessage(player, LangUtils.getText("controller-points-different-worlds"));
                            clearPoints(player, data);
                        } else {
                            data.world = worldName;
                            data.p2x = (int) closest.getX();
                            data.p2y = (int) closest.getY();
                            data.p2z = (int) closest.getZ();
                            Utils.Pair<Integer,Integer> size = getScreenSize(new Location(closest.getWorld(),data.p1x,data.p1y,data.p1z),closest);
                            data.w = size.getKey();
                            data.h = size.getValue();
                            Main.sendMessage(player, "§c"+LangUtils.getText("controller-set-point-end").replace("%%",
                                    (int)closest.getX()+","+(int)closest.getY()+","+(int)closest.getZ()));
                        }
                    }
                    meta.setLocalizedName(Main.getGson().toJson(data));
                    item.setItemMeta(meta);
                    return;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            player.getInventory().setItemInMainHand(null);
        }
    }

    private static void clearPoints(Player player, ItemData data) {
        data.world = null;
        data.p1x=null;
        data.p1y=null;
        data.p1z=null;
        data.p2x=null;
        data.p2y=null;
        data.p2z=null;
    }
    private static Utils.Pair<Utils.Pair<Screen.Facing,Location>,Utils.Pair<Screen.Facing,Location>> getScreenLocations(World world,int p1x, int p1y, int p1z, int p2x, int p2y, int p2z){
        Utils.Pair<Screen.Facing,Location> l1=null,l2=null;
        Integer sx=null,sy=null,sz=null;
        if(p1x==p2x){
            l1=new Utils.Pair<>(Screen.Facing.EAST,
                    new Location(
                            world,
                            p1x,
                            Math.max(p1y,p2y)-1,
                            Math.max(p1z,p2z)-1
                    ));
            l2=new Utils.Pair<>(Screen.Facing.WEST,
                    new Location(
                            world,
                            p1x-1,
                            Math.max(p1y,p2y)-1,
                            Math.min(p1z,p2z)
                    ));
        }
        if(p1y==p2y){
            l1=new Utils.Pair<>(Screen.Facing.UP,
                    new Location(
                            world,
                            Math.min(p1x,p2x),
                            p1y,
                            Math.min(p1z,p2z)
                    ));
            l2=new Utils.Pair<>(Screen.Facing.DOWN,
                    new Location(
                            world,
                            Math.min(p1x,p2x),
                            p1y-1,
                            Math.max(p1z,p2z)-1
                    ));
        }
        if(p1z==p2z){
            l1=new Utils.Pair<>(Screen.Facing.SOUTH,
                    new Location(
                            world,
                            Math.min(p1x,p2x),
                            Math.max(p1y,p2y)-1,
                            p1z
                    ));
            l2=new Utils.Pair<>(Screen.Facing.NORTH,
                    new Location(
                            world,
                            Math.max(p1x,p2x)-1,
                            Math.max(p1y,p2y)-1,
                            p1z-1
                    ));
        }
        return new Utils.Pair<>(l1,l2);
    }

    private static Location getClosestBlock(Location eyeLoc) {
        for(int i = 1; i<21; i++){
            Vector eyeVec = eyeLoc.toVector();
            Vector direction = eyeLoc.getDirection();
            direction.multiply(i*0.25);
            eyeVec.add(direction);
            Location point = eyeVec.toLocation(eyeLoc.getWorld());
            Block block = point.getBlock();
            if(block!=null&&!block.getType().isAir()){
                int x = (int) Math.round(point.getX());
                int y = (int) Math.round(point.getY());
                int z = (int) Math.round(point.getZ());
                return new Location(eyeLoc.getWorld(),x,y,z);
            }
        }
        return null;
    }
    private static Location getClosestPlane(Location eyeLoc,int locX,int locY,int locZ) {
        for(int i = 1; i<21; i++){
            Vector eyeVec = eyeLoc.toVector();
            Vector direction = eyeLoc.getDirection();
            direction.multiply(i*0.25);
            eyeVec.add(direction);
            Location point = eyeVec.toLocation(eyeLoc.getWorld());
            int x = (int) Math.round(point.getX());
            int y = (int) Math.round(point.getY());
            int z = (int) Math.round(point.getZ());
            int j = 0;
            if(x==locX){
                j++;
            }
            if(y==locY){
                j++;
            }
            if(z==locZ){
                j++;
            }
            Block block = point.getBlock();
            if(j==1&&block!=null&&!block.getType().isAir()){
                return new Location(eyeLoc.getWorld(),x,y,z);
            }
        }
        return null;
    }
    private static Utils.Pair<Integer,Integer> getScreenSize(Location l1,Location l2){
        int w=0,h=0;
        if((int)l1.getX() == (int)l2.getX()){
            w = (int) Math.abs(l1.getZ()-l2.getZ());
            h = (int) Math.abs(l1.getY()-l2.getY());
        }
        if((int)l1.getY() == (int)l2.getY()){
            w = (int) Math.abs(l1.getX()-l2.getX());
            h = (int) Math.abs(l1.getZ()-l2.getZ());
        }
        if((int)l1.getZ() == (int)l2.getZ()){
            w = (int) Math.abs(l1.getX()-l2.getX());
            h = (int) Math.abs(l1.getY()-l2.getY());
        }
        return new Utils.Pair<>(w,h);
    }
}
