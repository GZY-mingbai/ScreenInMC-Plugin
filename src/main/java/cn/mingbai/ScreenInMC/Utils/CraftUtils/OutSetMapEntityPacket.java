package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class OutSetMapEntityPacket implements OutPacket{
    static Class PacketPlayOutEntityMetadataClass;
    static Constructor PacketPlayOutEntityMetadataConstructor;
    static Class DataWatcherClass;
    static Class PacketDataSerializerClass;
    static Constructor PacketDataSerializerConstructor;
    static Class DataWatcherRegistryClass;
    static Object DataWatcherSerializerItemStack;
    static Object DataWatcherSerializerByte;
    static Class DataWatcherSerializerClass;
    static Method DataWatcherGetID;
    static int ItemStackID;
    static int ByteID;
    static Method PacketDataSerializerWriteItemStack;
    static boolean SetPacketDataSerializer = false;
    static Method SetPacketDataSerializerMethod;
    static Class EntityItemFrameClass;
    static int NewItemStackID;
    static int NewFacingID;
    static Class DataWatcherObjectClass;
    static Field DataWatcherObjectId;
    static Field testField;
    private static boolean testWriteMethod(Method i) throws Exception{
        ByteBuf buf = (ByteBuf) PacketDataSerializerConstructor.newInstance(Unpooled.buffer());
        writeVarInt(buf,0);
        if(CraftUtils.minecraftVersion<9) {
            buf.writeByte(OldStateID);
            buf.writeByte((byte)0x0);
            buf.writeByte(127);
        }else{
            buf.writeByte(0);
            writeVarInt(buf,ByteID);
            buf.writeByte((byte)0x0);
            buf.writeByte(255);
        }
        Object packet = PacketPlayOutEntityMetadataConstructor.newInstance();
        i.invoke(packet,buf);
        return testField.get(packet)!=null;
    }

    protected static void init() throws Exception {
        PacketPlayOutEntityMetadataClass = CraftUtils.getMinecraftClass("PacketPlayOutEntityMetadata");
        DataWatcherClass = CraftUtils.getMinecraftClass("DataWatcher");
        PacketDataSerializerClass=CraftUtils.getMinecraftClass("PacketDataSerializer");
        PacketDataSerializerConstructor = PacketDataSerializerClass.getDeclaredConstructor(ByteBuf.class);
        try {
            PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadataClass.getDeclaredConstructor(PacketDataSerializerClass);
        }catch (Exception e){
            for(Field i:PacketPlayOutEntityMetadataClass.getDeclaredFields()){
                if(!Modifier.isStatic(i.getModifiers())){
                    if(Object.class.isAssignableFrom(i.getClass())){
                        testField=i;
                        testField.setAccessible(true);
                    }
                }
            }
            SetPacketDataSerializer = true;
            PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadataClass.getDeclaredConstructor();
            PacketPlayOutEntityMetadataConstructor.setAccessible(true);
            for(Method i:PacketPlayOutEntityMetadataClass.getDeclaredMethods()){
                if(i.getParameterCount()==1&&i.getParameters()[0].getType().equals(PacketDataSerializerClass)){
                    if(testWriteMethod(i)){
                        SetPacketDataSerializerMethod = i;
                        i.setAccessible(true);
                        break;
                    }
                }
            }
        }
        for(Method i:PacketDataSerializerClass.getDeclaredMethods()){
            if(!Modifier.isStatic(i.getModifiers()) && i.getParameterCount()==1 && i.getParameters()[0].getType().equals(NMSItemStack.ItemStackClass)){
                PacketDataSerializerWriteItemStack = i;
            }
        }
        if(CraftUtils.minecraftVersion<9){
            return;
        }

        EntityItemFrameClass = CraftUtils.getMinecraftClass("EntityItemFrame");
        DataWatcherObjectClass = CraftUtils.getMinecraftClass("DataWatcherObject");
        for(Field i:DataWatcherObjectClass.getDeclaredFields()){
            if(i.getType().equals(int.class)){
                DataWatcherObjectId = i;
                DataWatcherObjectId.setAccessible(true);
                break;
            }
        }
        for(Field i:EntityItemFrameClass.getDeclaredFields()){
            if(Modifier.isStatic(i.getModifiers())){
                if(i.getGenericType().getTypeName().contains("DataWatcherObject")){
                    if(i.getGenericType().getTypeName().contains("ItemStack")) {
                        i.setAccessible(true);
                        NewItemStackID = Utils.getInt(DataWatcherObjectId.get(i.get(null)));
                    }
                    if(i.getGenericType().getTypeName().contains("Integer")) {
                        i.setAccessible(true);
                        NewFacingID = Utils.getInt(DataWatcherObjectId.get(i.get(null)));
                    }
                }
            }
        }

        DataWatcherSerializerClass = CraftUtils.getMinecraftClass("DataWatcherSerializer");
        DataWatcherRegistryClass = CraftUtils.getMinecraftClass("DataWatcherRegistry");

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
        for(Method i:DataWatcherRegistryClass.getDeclaredMethods()){
            if(Modifier.isStatic(i.getModifiers())&&i.getReturnType().equals(int.class)&&i.getParameterCount()==1&&i.getParameters()[0].getType().equals(DataWatcherSerializerClass)){
                DataWatcherGetID = i;
            }
        }
        ItemStackID = Utils.getInt(DataWatcherGetID.invoke(null,DataWatcherSerializerItemStack));
        ByteID = Utils.getInt(DataWatcherGetID.invoke(null,DataWatcherSerializerByte));


    }
    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & 0xFFFFFF80) != 0) {
            buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }
    static int OldItemStackID = ((5<<5)|(8&0x1F))&0xFF;
    static int OldRotateID = ((0<<5)|(9&0x1F))&0xFF;
    static int OldStateID = ((0<<5)|(0&0x1F))&0xFF;

    public static Object create(int entityId,int mapId){
        try {
            ByteBuf buf = (ByteBuf) PacketDataSerializerConstructor.newInstance(Unpooled.buffer());
            writeVarInt(buf,entityId);
            if(CraftUtils.minecraftVersion<9){
                buf.writeByte(OldStateID);
                buf.writeByte((byte)0x20);

                buf.writeByte(OldItemStackID);
                NMSMap item = new NMSMap();
                item.setMap(mapId);
                PacketDataSerializerWriteItemStack.invoke(buf,item.getItemStack());

//                buf.writeByte(OldRotateID);
//                buf.writeByte((int)0);

                buf.writeByte(127);
            }else{
                buf.writeByte(0);
                writeVarInt(buf,ByteID);
                buf.writeByte((byte)0x20);

                buf.writeByte(NewItemStackID);
                writeVarInt(buf,ItemStackID);
                NMSMap item = new NMSMap();
                item.setMap(mapId);
                PacketDataSerializerWriteItemStack.invoke(buf,item.getItemStack());

//                buf.writeByte(NewFacingID);
//                writeVarInt(buf,ByteID);
//                writeVarInt(buf,(int)0);



                buf.writeByte(255);
            }
            if(SetPacketDataSerializer){
                Object packet = PacketPlayOutEntityMetadataConstructor.newInstance();
                SetPacketDataSerializerMethod.invoke(packet,buf);
                return packet;
            }else{
                return PacketPlayOutEntityMetadataConstructor.newInstance(buf);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
