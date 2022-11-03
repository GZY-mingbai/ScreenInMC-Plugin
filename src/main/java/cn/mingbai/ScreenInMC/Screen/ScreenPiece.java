package cn.mingbai.ScreenInMC.Screen;

import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Location;

import java.util.UUID;

public class ScreenPiece {
    private static int nowId = 6000000;
    private Location location;
    private int id;
    private UUID uuid;
    public ScreenPiece(Location location){
        this.location=location;
        this.id = nowId;
        this.uuid = UUID.nameUUIDFromBytes(Utils.intToByteArray(this.id));
        nowId++;
        if(nowId<0){
            nowId = 6000000;
        }
    }
    public Location getLocation() {
        return location;
    }

    public int getEntityId() {
        return id;
    }

    public UUID getUUID() {
        return uuid;
    }
}
