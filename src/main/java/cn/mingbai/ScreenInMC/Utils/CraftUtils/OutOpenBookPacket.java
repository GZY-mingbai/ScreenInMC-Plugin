package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class OutOpenBookPacket implements OutPacket{
    static Class PacketPlayOutOpenBookClass;

    static Class PacketPlayOutCustomPayloadClass;
    static Constructor PacketPlayOutCustomPayloadConstructor;
    static Constructor PacketPlayOutOpenBookConstructor;
    static Object MainHand;
    static Class EnumHandClass;
    static Method EnumHandValues;
    static Method EnumHandOrdinal;
    static Class MinecraftKeyClass;
    static Object OpenBookKey;

    static byte method = 0;
    protected static void init() throws Exception {
        try {
            PacketPlayOutOpenBookClass = CraftUtils.getMinecraftClass("PacketPlayOutOpenBook");
            if(PacketPlayOutOpenBookClass==null){
                PacketPlayOutOpenBookClass = CraftUtils.getMinecraftClass("ClientboundOpenBookPacket");
            }
            EnumHandClass = CraftUtils.getMinecraftClass("EnumHand");
            if(EnumHandClass==null){
                EnumHandClass = CraftUtils.getMinecraftClass("InteractionHand");
            }
            PacketPlayOutOpenBookConstructor = PacketPlayOutOpenBookClass.getDeclaredConstructor(EnumHandClass);
            method = 2;
        }catch (Exception e){
            PacketPlayOutCustomPayloadClass = CraftUtils.getMinecraftClass("PacketPlayOutCustomPayload");
            if(CraftUtils.minecraftVersion<=13){
                try {
                    MinecraftKeyClass = CraftUtils.getMinecraftClass("MinecraftKey");
                    PacketPlayOutCustomPayloadConstructor = PacketPlayOutCustomPayloadClass.getDeclaredConstructor(MinecraftKeyClass, OutSetMapEntityPacket.PacketDataSerializerClass);
                    method = 1;
                }catch (Exception er){
                    PacketPlayOutCustomPayloadConstructor = PacketPlayOutCustomPayloadClass.getDeclaredConstructor(String.class, OutSetMapEntityPacket.PacketDataSerializerClass);
                    method = 0;
                }
            }else{
                throw new RuntimeException(e);
            }
        }
        if(method==1){
            for(Method i:MinecraftKeyClass.getDeclaredMethods()){
                if(i.getParameterCount()==1&&i.getParameters()[0].getType().equals(String.class)&& Modifier.isStatic(i.getModifiers())){
                    OpenBookKey = i.invoke(null,"minecraft:book_open");
                }
            }
        }
        if(method==1||method==2){
            EnumHandClass = CraftUtils.getMinecraftClass("EnumHand");
            if(EnumHandClass==null){
                EnumHandClass = CraftUtils.getMinecraftClass("InteractionHand");
            }
            EnumHandValues = EnumHandClass.getDeclaredMethod("values");
            EnumHandOrdinal = Enum.class.getDeclaredMethod("ordinal");
            MainHand = ((Object[])EnumHandValues.invoke(null))[0];
        }
    }
    public static Object create(){
        try {
            if(method==2){
                return PacketPlayOutOpenBookConstructor.newInstance(MainHand);
            }
            if(method==1){
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(0);
                return PacketPlayOutCustomPayloadConstructor.newInstance(OpenBookKey, OutSetMapEntityPacket.PacketDataSerializerConstructor.newInstance(buf));
            }
            if(method==0){
                ByteBuf buf = Unpooled.buffer(256);
                buf.setByte(0, (byte)0);
                buf.writerIndex(1);
                return PacketPlayOutCustomPayloadConstructor.newInstance("MC|BOpen", OutSetMapEntityPacket.PacketDataSerializerConstructor.newInstance(buf));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException();
    }
}
