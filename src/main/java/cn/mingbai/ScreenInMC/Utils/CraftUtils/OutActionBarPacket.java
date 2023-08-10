package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class OutActionBarPacket implements OutPacket{
    static Class ClientboundSetActionBarTextPacketClass;
    static Constructor ClientboundSetActionBarTextPacketConstructor;
    static Class PacketPlayOutChatClass;
    static Constructor PacketPlayOutChatConstructor;
    static Class ChatMessageTypeClass;
    static Class IChatBaseComponentClass;
    static Object ChatMessageTypeGameInfo;
    static Method ChatMessageTypeValues;
    protected static void init() throws Exception {
        ClientboundSetActionBarTextPacketClass = CraftUtils.getMinecraftClass("ClientboundSetActionBarTextPacket");
        if(ClientboundSetActionBarTextPacketClass!=null){
            return;
        }
        PacketPlayOutChatClass = CraftUtils.getMinecraftClass("PacketPlayOutChat");
        IChatBaseComponentClass = CraftUtils.getMinecraftClass("IChatBaseComponent");
        if(ClientboundSetActionBarTextPacketClass!=null){
            ClientboundSetActionBarTextPacketConstructor = ClientboundSetActionBarTextPacketClass.getDeclaredConstructor(IChatBaseComponentClass);
        }
        try {
            PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass,byte.class);
        }catch (Exception e){
            ChatMessageTypeClass = CraftUtils.getMinecraftClass("ChatMessageTypeClass");
            PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass,ChatMessageTypeClass);
            ChatMessageTypeValues = ChatMessageTypeClass.getDeclaredMethod("values");
            ChatMessageTypeGameInfo = ((Object[])ChatMessageTypeValues.invoke(null) )[2];
        }
    }
    public static Object create(LangUtils.JsonText text){
        try {
            if (ClientboundSetActionBarTextPacketClass != null) {
                return ClientboundSetActionBarTextPacketConstructor.newInstance(text.toComponent());
            }
            if(ChatMessageTypeClass==null){
                return PacketPlayOutChatConstructor.newInstance(text.toComponent(),(byte)2);
            }else{
                return PacketPlayOutChatConstructor.newInstance(text.toComponent(),ChatMessageTypeGameInfo);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
