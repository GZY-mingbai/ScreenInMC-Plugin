package cn.mingbai.ScreenInMC.Screen;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Utils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.mingbai.ScreenInMC.Core.getCoreFromData;

public class Screen {
    private static final List<Screen> allScreens = Collections.synchronizedList(new ArrayList<>());
    private static Utils.Pair<Integer, Integer> maxScreenSize = new Utils.Pair<>(50, 50);
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

    public Screen(ScreenData data) {
        this.location = new Location(Bukkit.getWorld(data.world), data.x, data.y, data.z);
        this.facing = data.facing;
        this.height = data.height;
        this.width = data.width;
        if (data.core != null) {
            this.core = getCoreFromData(data.core);
        }
    }

    public static Utils.Pair<Integer, Integer> getMaxScreenSize() {
        return maxScreenSize;
    }

    public static void setMaxScreenSize(int width, int height) {
        Screen.maxScreenSize = new Utils.Pair<>(width, height);
    }

    public static Screen[] getAllScreens() {
        Screen[] result = new Screen[allScreens.size()];
        for (int i = 0; i < allScreens.size(); i++) {
            result[i] = allScreens.get(i);
        }
        return result;
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

    public static void removeScreen(Screen screen) {
        screen.disableScreen();
        allScreens.remove(screen);
    }

    public ScreenData getScreenData() {
        ScreenData screenData = new ScreenData();
        screenData.world = location.getWorld().getName();
        screenData.x = location.getBlockX();
        screenData.y = location.getBlockY();
        screenData.z = location.getBlockZ();
        screenData.facing = this.facing;
        screenData.width = this.width;
        screenData.height = this.height;
        if (core != null) {
            screenData.core = core.getCoreData();
        }
        return screenData;
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
                    CraftUtils.sendPacket(player, packet1);
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
                    CraftUtils.sendPacket(player, packet2);
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
            for (Player player : location.getWorld().getPlayers()) {
                sendPutScreenPacket(player);
            }
            core.create(this);

        } else {
            throw new RuntimeException("This Screen has been placed.");
        }
    }

    public void disableScreen() {
        placed = false;
        core.unload();
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < screenPieces.length; i++) {
            for (int j = 0; j < screenPieces[i].length; j++) {
                ids.add(screenPieces[i][j].getEntityId());
            }
        }
        int[] array = new int[ids.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = ids.get(i);
        }
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(array);
        for (Player player : location.getWorld().getPlayers()) {
            CraftUtils.sendPacket(player, packet);
        }
    }

    public Core getCore() {
        return core;
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public void sendView(byte[] colors) {
        List<Packet> packets = getPackets(colors);
        for (Player player : location.getWorld().getPlayers()) {
            for (Packet i : packets) {
                CraftUtils.sendPacket(player, i);
            }
        }
    }

    public void sendView(byte[] colors, int x, int y, int w, int h) {
        List<Packet> packets = getPackets(colors, x, y, w, h);
        for (Player player : location.getWorld().getPlayers()) {
            for (Packet i : packets) {
                CraftUtils.sendPacket(player, i);
            }
        }
    }

    public void sendView(Player player, byte[] colors) {
        if (!location.getWorld().equals(player.getWorld())) {
            return;
        }
        if (location.distance(player.getLocation()) > displayDistance) {
            return;
        }
        for (Packet i : getPackets(colors)) {
            CraftUtils.sendPacket(player, i);
        }
    }

    public List<Packet> getPackets(byte[] colors) {
        List<Packet> packets = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte[] result = new byte[16384];
                int p = (y * width * 128 + x);
                for (int i = 0; i < 128; i++) {
                    System.arraycopy(colors, (p + i * width) * 128, result, i * 128, 128);
                }
                MapItemSavedData.MapPatch mapPatch = new MapItemSavedData.MapPatch(0, 0, 128, 128, result);
                packets.add(new ClientboundMapItemDataPacket(screenPieces[x][y].getEntityId(), (byte) 0, true, new ArrayList<>(), mapPatch));
            }
        }
        return packets;
    }

    public List<Packet> getPackets(byte[] colors, int x, int y, int w, int h) {
        List<Packet> packets = new ArrayList<>();
        int a = x / 128;
        int b = y / 128;
        int c = x % 128;
        int d = y % 128;
        int e, f;
        if (w + c < 128) {
            e = w;
        } else {
            e = 128 - c;
        }
        if (h + d < 128) {
            f = h;
        } else {
            f = 128 - d;
        }
        byte[] r1 = new byte[e * f];
        for (int i = 0; i < f; i++) {
            System.arraycopy(colors, i * w, r1, i * e, e);
        }
        MapItemSavedData.MapPatch mapPatch = new MapItemSavedData.MapPatch(c, d, e, f, r1);
        packets.add(new ClientboundMapItemDataPacket(screenPieces[a][b].getEntityId(), (byte) 0, true, new ArrayList<>(), mapPatch));
        int k = (c + w) / 128 + 1;
        int l = (d + h) / 128 + 1;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < k; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                int m, n, o, p, q, s;
                if (j == 0) {
                    m = c;
                } else {
                    m = 0;
                }
                if (i == 0) {
                    n = d;
                } else {
                    n = 0;
                }
                if (j == 0) {
                    if (k == 1) {
                        o = w;
                    } else {
                        o = 128 - m;
                    }
                } else if (j == k - 1) {
                    o = w - e - (k - 2) * 128;
                } else {
                    o = 128;
                }
                if (i == 0) {
                    if (l == 1) {
                        p = h;
                    } else {
                        p = 128 - n;
                    }
                } else if (i == l - 1) {
                    p = h - f - (l - 2) * 128;
                } else {
                    p = 128;
                }
                if (j == 0) {
                    q = 0;
                } else {
                    q = e + (j - 1) * 128;
                }
                if (i == 0) {
                    s = 0;
                } else {
                    s = f + (i - 1) * 128;
                }
                byte[] r2 = new byte[o * p];
                if (r2.length == 0) {
                    continue;
                }
                for (int r = 0; r < p; r++) {
                    System.arraycopy(colors, (r + s) * w + q, r2, r * o, o);
                }
                mapPatch = new MapItemSavedData.MapPatch(m, n, o, p, r2);
                packets.add(new ClientboundMapItemDataPacket(screenPieces[a + j][b + i].getEntityId(), (byte) 0, true, new ArrayList<>(), mapPatch));
            }
        }
        return packets;
    }

    public void sendView(Player player, byte[] colors, int x, int y, int w, int h) {
        if (!location.getWorld().equals(player.getWorld())) {
            return;
        }
        if (location.distance(player.getLocation()) > displayDistance) {
            return;
        }
        if (w * h != colors.length) {
            return;
        }
        for (Packet i : getPackets(colors, x, y, w, h)) {
            CraftUtils.sendPacket(player, i);
        }
    }

    public enum Facing {
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST;

        public Facing getOpposition() {
            switch (this) {
                case UP:
                    return DOWN;
                case DOWN:
                    return UP;
                case EAST:
                    return WEST;
                case WEST:
                    return EAST;
                case NORTH:
                    return SOUTH;
                case SOUTH:
                    return NORTH;
            }
            return null;
        }

        public String getFacingName() {
            switch (this) {
                case UP:
                    return LangUtils.getText("facing-up");
                case DOWN:
                    return LangUtils.getText("facing-down");
                case EAST:
                    return LangUtils.getText("facing-east");
                case WEST:
                    return LangUtils.getText("facing-west");
                case NORTH:
                    return LangUtils.getText("facing-north");
                case SOUTH:
                    return LangUtils.getText("facing-south");
            }
            return "";
        }

    }

    public static class ScreenData {
        public String world;
        public int x = 0;
        public int y = 0;
        public int z = 0;
        public Facing facing = Facing.UP;
        public int width = 1;
        public int height = 1;
        public Core.CoreData core = null;
    }
}
