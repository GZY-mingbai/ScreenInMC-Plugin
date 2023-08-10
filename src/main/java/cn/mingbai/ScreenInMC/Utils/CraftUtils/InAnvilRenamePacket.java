package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class InAnvilRenamePacket implements InPacket{
    static Class PacketPlayInItemNameClass;
    static Class PacketPlayInCustomPayloadClass;
    static Object packet;
    static Field Key;
    static Field PacketData;
    static Field Name;
    protected static void init() throws Exception {
        try {
            PacketPlayInItemNameClass = CraftUtils.getMinecraftClass("PacketPlayInItemName");
            for(Field i:PacketPlayInItemNameClass.getDeclaredFields()) {
                if(!Modifier.isStatic(i.getModifiers())){
                    if(i.getType().equals(String.class)) Name = i;
                }
            }
            Name.setAccessible(true);
        }catch (Exception e){
            PacketPlayInCustomPayloadClass = CraftUtils.getMinecraftClass("PacketPlayInCustomPayload");
            for(Field i:PacketPlayInCustomPayloadClass.getDeclaredFields()){
                if(!Modifier.isStatic(i.getModifiers())){
                    if(i.getType().equals(String.class)) Key = i;
                    if(i.getType().equals(OutSetMapEntityPacket.PacketDataSerializerClass)) PacketData = i;
                }
            }
            Key.setAccessible(true);
            PacketData.setAccessible(true);
        }

    }

    @Override
    public Class getNMSClass() {
        if(PacketPlayInItemNameClass==null){
            return PacketPlayInCustomPayloadClass;
        }else {
            return PacketPlayInItemNameClass;
        }
    }

    @Override
    public boolean load(Object obj) {
        this.packet=obj;
        if(PacketPlayInItemNameClass!=null) return true;
        try {
            if(Key.get(obj).equals("MC|ItemName"))return true;
        }catch (Exception e){
            return false;
        }
        return false;
    }

    public String getName() {
        try {
            if(PacketPlayInItemNameClass==null){
                ByteBuf buf = (ByteBuf) PacketData.get(packet);
                byte[] data = new byte[1024];
                buf.readBytes(data);
                return new String(data, StandardCharsets.UTF_8);
            }else {
                return (String) Name.get(packet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
