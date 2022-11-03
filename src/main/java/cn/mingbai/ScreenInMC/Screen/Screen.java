package cn.mingbai.ScreenInMC.Screen;

import cn.mingbai.ScreenInMC.Utils.Utils;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Screen {
    private static List<Screen> allScreens = Collections.synchronizedList(new ArrayList<>());
    public Location location;
    private Facing facing;
    private int height;
    private int width;
    private boolean placed = false;
    private ScreenPiece[][] screenPieces;
    public enum Facing{
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST
    }

    public static List<Screen> getAllScreens() {
        return allScreens;
    }

    public static Utils.Pair<Integer,Integer> getFacingPitchYaw(Facing facing){
        switch (facing){
            case UP:
                return new Utils.Pair<>(0,-90);
            case DOWN:
                return new Utils.Pair<>(0,90);
            case WEST:
                return new Utils.Pair<>(90,0);
            case SOUTH:
                return new Utils.Pair<>(0,0);
            case EAST:
                return new Utils.Pair<>(-90,0);
            case NORTH:
                return new Utils.Pair<>(-180,0);
        }
        return null;
    }
    public Screen(Location location,Facing facing,int width,int height){
        this.location=location;
        this.facing=facing;
        this.height=height;
        this.width=width;
    }
    public void sendPutScreenPacket(Player player) {
        if (placed) {
            if(!location.getWorld().equals(player.getWorld())){
                return;
            }
            ServerPlayer sp = ((CraftPlayer)player).getHandle();
            ServerPlayerConnection spc = sp.connection;
            Utils.Pair<Integer,Integer> pitchYaw = getFacingPitchYaw(facing);
            for(int x=0;x<width;x++){
                for(int y=0;y<width;y++) {
                    ScreenPiece piece = screenPieces[x][y];
                    Location loc = piece.getLocation();
                    Bukkit.broadcastMessage(piece.getEntityId()+" "+piece.getUUID()+" "+facing.ordinal()+" "+sp.displayName);
                    ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
                            piece.getEntityId(), piece.getUUID(),
                            (double) loc.getX(), (double) loc.getY(), (double) loc.getZ(),
                            pitchYaw.getKey(),pitchYaw.getValue(), EntityType.ITEM_FRAME,
                            facing.ordinal(),new Vec3(0,0,0),0
                    );
                    spc.send(packet);
                }
            }
        } else {
            throw new RuntimeException("This Screen has not been placed.");
        }
    }
    public void putScreen(){
        if(!placed) {
            screenPieces = new ScreenPiece[width][height];
            switch (facing) {
                case UP:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x,0,y));
                        }
                    }
                    break;
                case DOWN:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x,0,-y));
                        }
                    }
                    break;
                case EAST:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(0,-y,x));
                        }
                    }
                    break;
                case SOUTH:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(-x,-y,0));
                        }
                    }
                    break;
                case WEST:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(0,-y,-x));
                        }
                    }
                    break;
                case NORTH:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x,-y,0));
                        }
                    }
                    break;
            }
            placed=true;
            allScreens.add(this);
            for(Player player : location.getWorld().getPlayers()){
                sendPutScreenPacket(player);
            }
        }else{
            throw new RuntimeException("This Screen has been placed.");
        }
    }
}
