package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class OutSetMapEntityPacket implements OutPacket{
    static Class PacketPlayOutEntityMetadataClass;
    static Constructor PacketPlayOutEntityMetadataConstructor;
    static Class DataWatcherClass;
    static Class DataWatcherItemClass;
    static Class PacketDataSerializerClass;
    static Constructor PacketDataSerializerConstructor;
    static Constructor DataWatcherItemConstructor;
    static Class DataWatcherObjectClass;
    static Constructor DataWatcherObjectConstructor;
    static Class DataWatcherRegistryClass;
    static Object DataWatcherSerializerItemStack;
    static Object DataWatcherSerializerByte;
    static Method DataWatcherAdd;

    protected static void init() throws Exception {
        PacketPlayOutEntityMetadataClass = CraftUtils.getMinecraftClass("PacketPlayOutEntityMetadata");
        DataWatcherClass = CraftUtils.getMinecraftClass("DataWatcher");
        PacketDataSerializerClass=CraftUtils.getMinecraftClass("PacketDataSerializer");
        PacketDataSerializerConstructor = PacketDataSerializerClass.getDeclaredConstructor(ByteBuf.class);
        for(Class cls:DataWatcherClass.getDeclaredClasses()){
            if(cls.getSimpleName().equals("Item")){
                DataWatcherItemClass = cls;
            }
        }
        DataWatcherObjectClass = CraftUtils.getMinecraftClass("DataWatcherObject");
        DataWatcherRegistryClass = CraftUtils.getMinecraftClass("DataWatcherRegistry");
        DataWatcherItemConstructor = CraftUtils.getConstructor(DataWatcherItemClass);
        DataWatcherObjectConstructor = CraftUtils.getConstructor(DataWatcherObjectClass);
        PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadataClass.getDeclaredConstructor(PacketDataSerializerClass);
        for(Field i:DataWatcherRegistryClass.getDeclaredFields()){
            if(i.getType().getSimpleName().equals("DataWatcherSerializer")){
                if(i.getGenericType().getTypeName().contains("ItemStack")) {
                    DataWatcherSerializerItemStack = i.get(null);
                }
                if(i.getGenericType().getTypeName().contains("java.lang.Byte")) {
                    DataWatcherSerializerByte = i.get(null);
                }
            }
        }
        for(Method i:DataWatcherClass.getDeclaredMethods()){
            if(Modifier.isStatic(i.getModifiers())){
                if(i.getParameterCount()==2){
                    if(i.getParameters()[0].getType().equals(List.class)&&i.getParameters()[1].getType().getSimpleName().equals("PacketDataSerializer")){
                        DataWatcherAdd=i;
                    }
                }
            }
        }

    }
    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & 0xFFFFFF80) != 0) {
            buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }
    public static Object create(int entityId,int mapId){
        try {
            ByteBuf buf = (ByteBuf) PacketDataSerializerConstructor.newInstance(Unpooled.buffer());
            writeVarInt(buf,entityId);
            Object dataWatcherObject = DataWatcherObjectConstructor.newInstance(8,DataWatcherSerializerItemStack);
            NMSMap item = new NMSMap();
            item.setMap(mapId);
            Object dataWatcherItem = DataWatcherItemConstructor.newInstance(dataWatcherObject,item.getItemStack());
            List<Object> dataWatcherItemList = new ArrayList<>();
            dataWatcherItemList.add(dataWatcherItem);
            dataWatcherObject = DataWatcherObjectConstructor.newInstance(0,DataWatcherSerializerByte);
            dataWatcherItem = DataWatcherItemConstructor.newInstance(dataWatcherObject,(byte)0x20);
            dataWatcherItemList.add(dataWatcherItem);
            DataWatcherAdd.invoke(null,dataWatcherItemList,buf);
            return PacketPlayOutEntityMetadataConstructor.newInstance(buf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
