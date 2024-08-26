package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class OutSetMapEntityPacket implements OutPacket{
    static Class PacketPlayOutEntityMetadataClass;
    static Constructor PacketPlayOutEntityMetadataConstructor;
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
    static Object RegistryAccess;
    static Object ItemStackOptionalStreamCodec;
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
        if(PacketPlayOutEntityMetadataClass==null){
            PacketPlayOutEntityMetadataClass = CraftUtils.getMinecraftClass("ClientboundSetEntityDataPacket");
        }
        PacketDataSerializerClass=CraftUtils.getMinecraftClass("PacketDataSerializer");
        if(PacketDataSerializerClass==null){
            PacketDataSerializerClass = CraftUtils.getMinecraftClass("RegistryFriendlyByteBuf");
        }
        try {
            PacketDataSerializerConstructor = PacketDataSerializerClass.getDeclaredConstructor(ByteBuf.class);
        }catch (Exception e){
            Class RegistryAccessClass = CraftUtils.getMinecraftClass("RegistryAccess");
            Class MinecraftServerClass = CraftUtils.getMinecraftClass("MinecraftServer");
            Object server = null;
            Method MinecraftServerRegistryAccess = null;
            for(Method i : MinecraftServerClass.getDeclaredMethods()){
                if(Modifier.isStatic(i.getModifiers()) && i.getReturnType().equals(MinecraftServerClass) && i.getParameterCount()==0){
                    server=i.invoke(null);
                }
                if(!Modifier.isStatic(i.getModifiers()) && i.getParameterCount()==0){
                    for(Class j:i.getReturnType().getInterfaces()){
                        if(j.equals(RegistryAccessClass)){
                            MinecraftServerRegistryAccess = i;
                            MinecraftServerRegistryAccess.setAccessible(true);
                        }
                    }
                }
            }
            if(server!=null){
                RegistryAccess = MinecraftServerRegistryAccess.invoke(server);
            }

            PacketDataSerializerConstructor = PacketDataSerializerClass.getDeclaredConstructor(ByteBuf.class,RegistryAccessClass);
        }
        try {
            PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadataClass.getDeclaredConstructor(PacketDataSerializerClass);
            PacketPlayOutEntityMetadataConstructor.setAccessible(true);
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
            if(SetPacketDataSerializerMethod==null){
                for(Method i:PacketPlayOutEntityMetadataClass.getSuperclass().getDeclaredMethods()){
                    if(i.getParameterCount()==1&&i.getParameters()[0].getType().equals(PacketDataSerializerClass)){
                        if(testWriteMethod(i)){
                            SetPacketDataSerializerMethod = i;
                            i.setAccessible(true);
                            break;
                        }
                    }
                }
            }
        }
        for(Method i:PacketDataSerializerClass.getDeclaredMethods()){
            if(!Modifier.isStatic(i.getModifiers()) && i.getParameterCount()==1 && i.getParameters()[0].getType().equals(NMSItemStack.ItemStackClass)){
                PacketDataSerializerWriteItemStack = i;
            }
        }
        if(PacketDataSerializerWriteItemStack==null){
            for(Method i:PacketDataSerializerClass.getSuperclass().getDeclaredMethods()){
                if(!Modifier.isStatic(i.getModifiers()) && i.getParameterCount()==1 && i.getParameters()[0].getType().equals(NMSItemStack.ItemStackClass)){
                    PacketDataSerializerWriteItemStack = i;
                }
            }
        }
        if(PacketDataSerializerWriteItemStack==null){
            Class ItemStackClass = CraftUtils.getMinecraftClass("ItemStack");
            String correctGenericTypeName = "<"
                    +CraftUtils.getMinecraftClass("RegistryFriendlyByteBuf").getName()+", "+
                    CraftUtils.getMinecraftClass("ItemStack").getName();
            for(Field i : ItemStackClass.getDeclaredFields()){
                if(Modifier.isStatic(i.getModifiers())&&i.getType().getSimpleName().equals("StreamCodec")
                        &&i.getGenericType().getTypeName().contains(correctGenericTypeName)){
                    i.setAccessible(true);
                    ItemStackOptionalStreamCodec = i.get(null);
                }
            }
            for(Method i : ItemStackOptionalStreamCodec.getClass().getDeclaredMethods()){
                if(!Modifier.isStatic(i.getModifiers()) && i.getParameterCount()==2 && i.getParameters()[0].getType().getSimpleName().equals("RegistryFriendlyByteBuf") && i.getParameters()[1].getType().getSimpleName().equals("ItemStack")){
                    PacketDataSerializerWriteItemStack = i;
                    PacketDataSerializerWriteItemStack.setAccessible(true);
                }
            }
        }

        if(CraftUtils.minecraftVersion<9){
            return;
        }

        EntityItemFrameClass = CraftUtils.getMinecraftClass("EntityItemFrame");
        if(EntityItemFrameClass==null){
            EntityItemFrameClass = CraftUtils.getMinecraftClass("ItemFrame");
        }
        DataWatcherObjectClass = CraftUtils.getMinecraftClass("DataWatcherObject");
        if(DataWatcherObjectClass==null){
            for(Class i : CraftUtils.getMinecraftClasses("EntityDataAccessor",false)){
                if(i.getSuperclass().getSimpleName().equals("Record")){
                    DataWatcherObjectClass = i;
                }
            }
        }
        for(Field i:DataWatcherObjectClass.getDeclaredFields()){
            if(i.getType().equals(int.class)){
                DataWatcherObjectId = i;
                DataWatcherObjectId.setAccessible(true);
                break;
            }
        }
        for(Field i:EntityItemFrameClass.getDeclaredFields()){
            if(Modifier.isStatic(i.getModifiers())){
                if(i.getType().equals(DataWatcherObjectClass)){
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
        if(DataWatcherSerializerClass==null){
            DataWatcherSerializerClass = CraftUtils.getMinecraftClass("EntityDataSerializer");
        }
        DataWatcherRegistryClass = CraftUtils.getMinecraftClass("DataWatcherRegistry");
        if(DataWatcherRegistryClass==null){
            DataWatcherRegistryClass = CraftUtils.getMinecraftClass("EntityDataSerializers");
        }
        for(Field i:DataWatcherRegistryClass.getDeclaredFields()){
            if(i.getType().equals(DataWatcherSerializerClass)){
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
            ByteBuf buf;
            if(PacketDataSerializerConstructor.getParameterCount()==1){
                buf = (ByteBuf) PacketDataSerializerConstructor.newInstance(Unpooled.buffer());
            }else{
                buf = (ByteBuf) PacketDataSerializerConstructor.newInstance(Unpooled.buffer(),RegistryAccess);
            }
            writeVarInt(buf,entityId);
            if(CraftUtils.minecraftVersion<9){
                buf.writeByte(OldStateID);
                buf.writeByte((byte)0x20);

                buf.writeByte(OldItemStackID);
                NMSMap item = NMSMap.create();
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
                NMSMap item = NMSMap.create();
                item.setMap(mapId);
                if(PacketDataSerializerWriteItemStack.getParameterCount()==1){
                    PacketDataSerializerWriteItemStack.invoke(buf,item.getItemStack());
                }else{
                    PacketDataSerializerWriteItemStack.invoke(ItemStackOptionalStreamCodec,buf,item.getItemStack());
                }

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
