package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Screen.Screen;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;


public class OutAddMapEntityPacket implements OutPacket{
    static Class PacketPlayOutSpawnEntityClass;
    static Class EntityClass;
    static Constructor PacketPlayOutSpawnEntityConstructor;
    static Class EntityTypesClass;
    static Class Vec3DClass;
    //由于Mojang在1.13.2及以下版本中使用if else判断EntityTypeID
    //因此无法动态获取ItemFrame的ID
    final static int ItemFrameID = 71;
    static Object EntityTypesItemFrame;
    static Constructor Vec3DConstructor;
    static Object Vec3D000;
    static Field FieldEntityId;
    static Field FieldX;
    static Field FieldY;
    static Field FieldZ;
    static Field FieldPitch;
    static Field FieldYaw;

    static Field FieldType;
    static Field FieldUUID;
    static Field FieldSpeedX;
    static Field FieldSpeedY;
    static Field FieldSpeedZ;
    static Field FieldData;



    protected static void init() throws Exception {
        PacketPlayOutSpawnEntityClass = CraftUtils.getMinecraftClass("PacketPlayOutSpawnEntity");
        EntityClass = CraftUtils.getMinecraftClass("Entity");
        if(CraftUtils.minecraftVersion<=13) {
            PacketPlayOutSpawnEntityConstructor = CraftUtils.getConstructor(PacketPlayOutSpawnEntityClass);
            for(Field i:PacketPlayOutSpawnEntityClass.getDeclaredFields()){
                if(i.getType().equals(int.class) || i.getType().equals(byte.class) || i.getType().equals(short.class) ||i.getType().equals(double.class) || i.getType().equals(float.class)){
                    i.setAccessible(true);
                    if(FieldEntityId==null) {FieldEntityId=i;continue;}
                    if(FieldX==null) {FieldX=i;continue;}
                    if(FieldY==null) {FieldY=i;continue;}
                    if(FieldZ==null) {FieldZ=i;continue;}
                    if(FieldSpeedX==null) {FieldSpeedX=i;continue;}
                    if(FieldSpeedY==null) {FieldSpeedY=i;continue;}
                    if(FieldSpeedZ==null) {FieldSpeedZ=i;continue;}
                    if(FieldPitch==null) {FieldPitch=i;continue;}
                    if(FieldYaw==null) {FieldYaw=i;continue;}
                    if(FieldType==null) {FieldType=i;continue;}
                    if(FieldData==null) {FieldData=i;continue;}
                }
                if(i.getType().equals(UUID.class)){
                    i.setAccessible(true);
                    if(FieldUUID==null){FieldUUID = i;continue;}
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
    private static void autoSetNumber(Field field,Object obj,Number value) throws Exception {
        if(field.getType().equals(byte.class)){
            field.set(obj,value.byteValue());
        }
        if(field.getType().equals(int.class)){
            field.set(obj,value.intValue());
        }
        if(field.getType().equals(double.class)){
            field.set(obj,value.doubleValue());
        }
        if(field.getType().equals(float.class)){
            field.set(obj,value.floatValue());
        }
    }
    public static void writeUUID(ByteBuf buf,UUID uuid){
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }
    public static int floorDouble(double value)
    {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }
    public static Object create(int entityId, UUID uuid, int x, int y, int z, int yaw, int pitch, Screen.Facing facing) {
        try {
            Object packet;
            if (CraftUtils.minecraftVersion <= 13) {
                packet = PacketPlayOutSpawnEntityConstructor.newInstance();
                autoSetNumber(FieldEntityId,packet, entityId);
                if(!(CraftUtils.minecraftVersion<=8)) FieldUUID.set(packet,uuid);
                autoSetNumber(FieldX, packet, CraftUtils.minecraftVersion<=8?floorDouble(x*32d):x);
                autoSetNumber(FieldY,packet, CraftUtils.minecraftVersion<=8?floorDouble(y*32d):y);
                autoSetNumber(FieldZ,packet, CraftUtils.minecraftVersion<=8?floorDouble(z*32d):z);
                autoSetNumber(FieldSpeedX,packet, 0);
                autoSetNumber(FieldSpeedY,packet, 0);
                autoSetNumber(FieldSpeedZ,packet, 0);
                autoSetNumber(FieldPitch,packet, (byte)(((float)pitch)*256f/360f));
                autoSetNumber(FieldYaw,packet, (byte)(((float)yaw)*256f/360f));
                autoSetNumber(FieldType,packet, ItemFrameID);
                autoSetNumber(FieldData,packet, CraftUtils.minecraftVersion<=12? facing.getHorizontalIndex():facing.ordinal());
                return packet;
            }
            if(CraftUtils.minecraftVersion<=16){
                return PacketPlayOutSpawnEntityConstructor.newInstance(entityId,uuid,(double)x,(double)y,(double)z,(float)pitch,(float)yaw,EntityTypesItemFrame,facing.ordinal(),Vec3D000);
            }
            if (CraftUtils.minecraftVersion <= 18) {
                return PacketPlayOutSpawnEntityConstructor.newInstance(entityId,uuid,(double)x,(double)y,(double)z,(float)pitch,(float)yaw,EntityTypesItemFrame,facing.ordinal(),Vec3D000);
            }
            return PacketPlayOutSpawnEntityConstructor.newInstance(entityId,uuid,(double)x,(double)y,(double)z,(float)pitch,(float)yaw,EntityTypesItemFrame,facing.ordinal(),Vec3D000,0d);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
