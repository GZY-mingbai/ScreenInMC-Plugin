package cn.mingbai.ScreenInMC.Screen;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Screen {
    private static final List<Screen> allScreens = Collections.synchronizedList(new ArrayList<>());
    private final int displayDistance = 32;
    private final Location location;
    private final Facing facing;
    private final int height;
    private final int width;
    private boolean placed = false;
    private ScreenPiece[][] screenPieces;
    private Core core;

    public Screen(Location location, Facing facing, int width, int height) {
        this.location = location;
        this.facing = facing;
        this.height = height;
        this.width = width;
    }

    public static List<Screen> getAllScreens() {
        return allScreens;
    }

    public static Utils.Pair<Integer, Integer> getFacingPitchYaw(Facing facing) {
        switch (facing) {
            case UP:
                return new Utils.Pair<>(0, -90);
            case DOWN:
                return new Utils.Pair<>(0, 90);
            case WEST:
                return new Utils.Pair<>(90, 0);
            case SOUTH:
                return new Utils.Pair<>(0, 0);
            case EAST:
                return new Utils.Pair<>(-90, 0);
            case NORTH:
                return new Utils.Pair<>(-180, 0);
        }
        return null;
    }

    public Location getLocation() {
        return location;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Facing getFacing() {
        return facing;
    }

    public void sendPutScreenPacket(Player player) {
        if (placed) {
            if (!location.getWorld().equals(player.getWorld())) {
                return;
            }
            ServerPlayer sp = ((CraftPlayer) player).getHandle();
            ServerPlayerConnection spc = sp.connection;
            Utils.Pair<Integer, Integer> pitchYaw = getFacingPitchYaw(facing);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    ScreenPiece piece = screenPieces[x][y];
                    Location loc = piece.getLocation();
                    int entityID = piece.getEntityId();
                    ClientboundAddEntityPacket packet1 = new ClientboundAddEntityPacket(
                            entityID, piece.getUUID(),
                            loc.getX(), loc.getY(), loc.getZ(),
                            pitchYaw.getKey(), pitchYaw.getValue(), EntityType.ITEM_FRAME,
                            facing.ordinal(), new Vec3(0, 0, 0), 0
                    );
                    spc.send(packet1);
                    ItemStack mapItem = new ItemStack(Items.FILLED_MAP);
                    mapItem.getOrCreateTag().putInt("map", entityID);
                    SynchedEntityData.DataItem dataItem = new SynchedEntityData.DataItem(new EntityDataAccessor<>(8, EntityDataSerializers.ITEM_STACK), mapItem);
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeVarInt(entityID);
                    List<SynchedEntityData.DataItem<?>> dataItemList = new ArrayList<>();
                    dataItemList.add(dataItem);
                    dataItemList.add(new SynchedEntityData.DataItem<>(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), (byte) 0x20));
                    SynchedEntityData.pack(dataItemList, byteBuf);
                    ClientboundSetEntityDataPacket packet2 = new ClientboundSetEntityDataPacket(byteBuf);
                    spc.send(packet2);
                }
            }
        } else {
            throw new RuntimeException("This Screen has not been placed.");
        }
    }

    public void putScreen() {
        if (!placed) {
            screenPieces = new ScreenPiece[width][height];
            switch (facing) {
                case UP:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x, 0, y));
                        }
                    }
                    break;
                case DOWN:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x, 0, -y));
                        }
                    }
                    break;
                case EAST:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(0, -y, -x));
                        }
                    }
                    break;
                case SOUTH:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x, -y, 0));
                        }
                    }
                    break;
                case WEST:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(0, -y, x));
                        }
                    }
                    break;
                case NORTH:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(-x, -y, 0));
                        }
                    }
                    break;
            }
            placed = true;
            allScreens.add(this);
//            byte[] testg = new byte[width*height*128*128];
//            for(int i=0;i<testg.length;i++)
//            {
//                try{
//                    testg[i]= (byte) i;
//                }catch (Exception e){
//                    break;
//                }
//            }

            for (Player player : location.getWorld().getPlayers()) {
                sendPutScreenPacket(player);
//                sendView(player,testg);
            }

        } else {
            throw new RuntimeException("This Screen has been placed.");
        }
    }

    public Core getCore() {
        return core;
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public void sendView(byte[] colors) {
        for (Player player : location.getWorld().getPlayers()) {
            sendView(player, colors);
        }
    }
    public void sendView(byte[] colors,int x,int y,int w,int h) {
        for (Player player : location.getWorld().getPlayers()) {
            sendView(player, colors,x,y,w,h);
        }
    }

    public void sendView(Player player, byte[] colors) {
        if (!location.getWorld().equals(player.getWorld())) {
            return;
        }
        if (location.distance(player.getLocation()) > displayDistance) {
            return;
        }
        ServerPlayer sp = ((CraftPlayer) player).getHandle();
        ServerPlayerConnection spc = sp.connection;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte[] result = new byte[16384];
                int p = (y * width * 128 + x);
                for (int i = 0; i < 128; i++) {
                    System.arraycopy(colors, (p + i * width) * 128, result, i * 128, 128);
                }
                MapItemSavedData.MapPatch mapPatch = new MapItemSavedData.MapPatch(0, 0, 128, 128, result);
                ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket(screenPieces[x][y].getEntityId(), (byte) 0, true, new ArrayList<>(), mapPatch);
                spc.send(packet);
            }
        }
    }
    public void sendView(Player player,byte[] colors,int x,int y,int w,int h){
        if (!location.getWorld().equals(player.getWorld())) {
            return;
        }
        if (location.distance(player.getLocation()) > displayDistance) {
            return;
        }
        ServerPlayer sp = ((CraftPlayer) player).getHandle();
        ServerPlayerConnection spc = sp.connection;
        if(w*h!=colors.length){
            return;
        }
        int a = x/128;
        int b = y/128;
        int c = x%128;
        int d = y%128;
        int e,f;
        if(w+c<128){
            e=w;
        }else{
            e=128-c;
        }
        if(h+d<128){
            f=h;
        }else{
            f=128-d;
        }
        byte[] r1 = new byte[e*f];
        for(int i=0;i<f;i++){
            System.arraycopy(colors, i * w, r1, i * e, e);
        }
        MapItemSavedData.MapPatch mapPatch = new MapItemSavedData.MapPatch(c, d, e, f, r1);
        ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket(screenPieces[a][b].getEntityId(), (byte) 0, true, new ArrayList<>(), mapPatch);
        spc.send(packet);
        int k = (c+w)/128+1;
        int l = (d+h)/128+1;
        for(int i=0;i<l;i++){
            for(int j=0;j<k;j++){
                if(i==0&&j==0){
                    continue;
                }
                int m,n,o,p,q,s;
                if(j==0){
                    m=c;
                }else{
                    m=0;
                }
                if(i==0){
                    n=d;
                }else{
                    n=0;
                }
                if(j==0){
                    if(k==1){
                        o=w;
                    }else{
                        o=128-m;
                    }
                } else if (j==k-1) {
                    o=w-e-(k-2)*128;
                } else {
                    o=128;
                }
                if(i==0){
                    if(l==1){
                        p=h;
                    }else{
                        p=128-n;
                    }
                } else if (i==l-1) {
                    p=h-f-(l-2)*128;
                } else {
                    p=128;
                }
                if(j==0){
                    q=0;
                }else{
                    q=e+(j-1)*128;
                }
                if(i==0){
                    s=0;
                }else{
                    s=f+(i-1)*128;
                }
                byte[] r2 = new byte[o*p];
                if(r2.length==0){
                    continue;
                }
//                for(int aaa=0;aaa<r2.length;aaa++){
//                    r2[aaa]=16;
//                }
                for(int r=0;r<p;r++){
                    System.arraycopy(colors, (r+s) * w+q, r2, r * o, o);
                }
                mapPatch = new MapItemSavedData.MapPatch(m, n, o, p, r2);
                packet = new ClientboundMapItemDataPacket(screenPieces[a+j][b+i].getEntityId(), (byte) 0, true, new ArrayList<>(), mapPatch);
                spc.send(packet);
            }
        }
    }

    public enum Facing {
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST
    }
}
