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

    public int readVarIntFromBuffer(ByteBuf buf)
    {
        int i = 0;
        int j = 0;

        while (true)
        {
            byte b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;

            if (j > 5)
            {
                throw new RuntimeException("VarInt too big");
            }

            if ((b0 & 128) != 128)
            {
                break;
            }
        }

        return i;
    }
    public String readStringFromBuffer(ByteBuf buf,int maxLength)
    {
        int i;
        try {
            i=readVarIntFromBuffer(buf);
        }catch (RuntimeException e){
            return "";
        }
        if (i > maxLength * 4)
        {
            return "";
        }
        else if (i < 0)
        {
            return "";
        }
        else
        {
            byte[] data = new byte[i];
            buf.readBytes(data);
            String s = new String(data, StandardCharsets.UTF_8);

            if (s.length() > maxLength)
            {
                return "";
            }
            else
            {
                return s;
            }
        }
    }
    public static boolean isAllowedCharacter(char character)
    {
        return character != 167 && character >= 32 && character != 127;
    }
    public static String filterAllowedCharacters(String input)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (char c0 : input.toCharArray())
        {
            if (isAllowedCharacter(c0))
            {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }
    public String getName() {
        try {
            if(PacketPlayInItemNameClass==null){
                ByteBuf buf = (ByteBuf) PacketData.get(packet);
                return filterAllowedCharacters(readStringFromBuffer(buf,32767));
            }else {
                return (String) Name.get(packet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
