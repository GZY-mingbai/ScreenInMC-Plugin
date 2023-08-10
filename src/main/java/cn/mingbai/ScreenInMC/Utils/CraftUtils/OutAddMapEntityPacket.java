package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

public class OutAddMapEntityPacket implements OutPacket{
    static Class PacketPlayOutSpawnEntityClass;
    static Class EntityClass;
    static Constructor PacketPlayOutSpawnEntityConstructor;
    static Field FieldEntityId;
    static Field FieldX;
    static Field FieldY;
    static Field FieldZ;
    static Field FieldSpeedX;
    static Field FieldSpeedY;
    static Field FieldSpeedZ;
    static Field FieldPitch;
    static Field FieldYaw;
    static Field FieldType;
    static Field FieldUUID;
    static Class EntityTypesClass;
    static Class Vec3DClass;
    final static int ItemFrameID = 18;
    static Object EntityTypesItemFrame;
    static Constructor Vec3DConstructor;
    static Object Vec3D000;

    protected static void init() throws Exception {
        PacketPlayOutSpawnEntityClass = CraftUtils.getMinecraftClass("PacketPlayOutSpawnEntity");
        EntityClass = CraftUtils.getMinecraftClass("Entity");
        if(CraftUtils.minecraftVersion<=8) {
            PacketPlayOutSpawnEntityConstructor = CraftUtils.getConstructor(PacketPlayOutSpawnEntityClass);
            for(Field i:PacketPlayOutSpawnEntityClass.getDeclaredFields()){
                if(i.getType().equals(int.class)){
                    i.setAccessible(true);
                    if(FieldEntityId!=null) {FieldEntityId=i;continue;}
                    if(FieldX!=null) {FieldX=i;continue;}
                    if(FieldY!=null) {FieldY=i;continue;}
                    if(FieldZ!=null) {FieldZ=i;continue;}
                    if(FieldSpeedX!=null) {FieldSpeedX=i;continue;}
                    if(FieldSpeedY!=null) {FieldSpeedY=i;continue;}
                    if(FieldSpeedZ!=null) {FieldSpeedZ=i;continue;}
                    if(FieldPitch!=null) {FieldPitch=i;continue;}
                    if(FieldYaw!=null) {FieldYaw=i;continue;}
                    if(FieldType!=null) {FieldType=i;continue;}
                }
            }
            return;
        }
        if(CraftUtils.minecraftVersion<=13) {
            PacketPlayOutSpawnEntityConstructor = CraftUtils.getConstructor(PacketPlayOutSpawnEntityClass);
            for(Field i:PacketPlayOutSpawnEntityClass.getDeclaredFields()){
                if(i.getType().equals(int.class)){
                    i.setAccessible(true);
                    if(FieldEntityId!=null) {FieldEntityId=i;continue;}
                    if(FieldSpeedX!=null) {FieldSpeedX=i;continue;}
                    if(FieldSpeedY!=null) {FieldSpeedY=i;continue;}
                    if(FieldSpeedZ!=null) {FieldSpeedZ=i;continue;}
                    if(FieldPitch!=null) {FieldPitch=i;continue;}
                    if(FieldYaw!=null) {FieldYaw=i;continue;}
                    if(FieldType!=null) {FieldType=i;continue;}
                }
                if(i.getType().equals(double.class)){
                    i.setAccessible(true);
                    if(FieldX!=null) {FieldX=i;continue;}
                    if(FieldY!=null) {FieldY=i;continue;}
                    if(FieldZ!=null) {FieldZ=i;continue;}
                }
                if(i.getType().equals(UUID.class)){
                    i.setAccessible(true);
                    if(FieldUUID!=null) {FieldUUID=i;continue;}
                }
            }
            return;
        }
        EntityTypesClass=CraftUtils.getMinecraftClass("EntityTypes");
        Vec3DClass=CraftUtils.getMinecraftClass("Vec3D");
        Vec3DConstructor=Vec3DClass.getDeclaredConstructor(double.class,double.class,double.class);
        Vec3D000 = Vec3DConstructor.newInstance(0d,0d,0d);
        for(Field i:EntityTypesClass.getDeclaredFields()){
            if(i.getGenericType().getTypeName().contains("EntityItemFrame")){
                EntityTypesItemFrame=i.get(null);
            }
        }
        if(CraftUtils.minecraftVersion<=18){
            PacketPlayOutSpawnEntityConstructor = PacketPlayOutSpawnEntityClass.getDeclaredConstructor(
                    int.class,
                    UUID.class,
                    double.class,
                    double.class,
                    double.class,
                    float.class,
                    float.class,
                    EntityTypesClass,
                    int.class,
                    Vec3DClass
                    );
            return;
        }
        PacketPlayOutSpawnEntityConstructor = PacketPlayOutSpawnEntityClass.getDeclaredConstructor(
                int.class,
                UUID.class,
                double.class,
                double.class,
                double.class,
                float.class,
                float.class,
                EntityTypesClass,
                int.class,
                Vec3DClass,
                double.class
        );
    }
    public static Object create(int entityId, UUID uuid, int x, int y, int z, int pitch, int yaw, int facing) {
        try {
            Object packet;
            if (CraftUtils.minecraftVersion <= 8) {
                packet = PacketPlayOutSpawnEntityConstructor.newInstance();
                FieldEntityId.set(packet, entityId);
                FieldX.set(packet, x);
                FieldY.set(packet, y);
                FieldZ.set(packet, z);
                FieldSpeedX.set(packet, 0);
                FieldSpeedY.set(packet, 0);
                FieldSpeedZ.set(packet, 0);
                FieldPitch.set(packet, pitch);
                FieldYaw.set(packet, yaw);
                FieldType.set(packet, ItemFrameID);
                return packet;
            }
            if (CraftUtils.minecraftVersion <= 13) {
                packet = PacketPlayOutSpawnEntityConstructor.newInstance();
                FieldEntityId.set(packet, entityId);
                FieldUUID.set(packet,uuid);
                FieldX.set(packet, (double)x);
                FieldY.set(packet, (double)y);
                FieldZ.set(packet, (double)z);
                FieldSpeedX.set(packet, 0);
                FieldSpeedY.set(packet, 0);
                FieldSpeedZ.set(packet, 0);
                FieldPitch.set(packet, pitch);
                FieldYaw.set(packet, yaw);
                FieldType.set(packet, ItemFrameID);
                return packet;
            }
            if (CraftUtils.minecraftVersion <= 18) {
                return PacketPlayOutSpawnEntityConstructor.newInstance(entityId,uuid,(double)x,(double)y,(double)z,(float)pitch,(float)yaw,EntityTypesItemFrame,facing,Vec3D000);
            }
            return PacketPlayOutSpawnEntityConstructor.newInstance(entityId,uuid,(double)x,(double)y,(double)z,(float)pitch,(float)yaw,EntityTypesItemFrame,facing,Vec3D000,0d);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
