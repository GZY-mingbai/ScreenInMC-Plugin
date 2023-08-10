package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class OutSystemMessagePacket implements OutPacket{
    static Class ClientboundSystemChatPacketClass;
    static Constructor ClientboundSetActionBarTextPacketConstructor;
    static Class PacketPlayOutChatClass;
    static Constructor PacketPlayOutChatConstructor;
    static Class ChatMessageTypeClass;
    static Class IChatBaseComponentClass;
    static Object ChatMessageTypeSystem;
    static Method ChatMessageTypeValues;
    protected static void init() throws Exception {
        ClientboundSystemChatPacketClass = CraftUtils.getMinecraftClass("ClientboundSystemChatPacket");
        if(ClientboundSystemChatPacketClass!=null){
            return;
        }
        PacketPlayOutChatClass = CraftUtils.getMinecraftClass("PacketPlayOutChat");
        IChatBaseComponentClass = CraftUtils.getMinecraftClass("IChatBaseComponent");
        if(ClientboundSystemChatPacketClass !=null){
            ClientboundSetActionBarTextPacketConstructor = ClientboundSystemChatPacketClass.getDeclaredConstructor(IChatBaseComponentClass,boolean.class);
        }
        try {
            PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass,byte.class);
        }catch (Exception e){
            ChatMessageTypeClass = CraftUtils.getMinecraftClass("ChatMessageTypeClass");
            PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass,ChatMessageTypeClass);
            ChatMessageTypeValues = ChatMessageTypeClass.getDeclaredMethod("values");
            ChatMessageTypeSystem = ((Object[])ChatMessageTypeValues.invoke(null) )[1];
        }
    }
    public static Object create(LangUtils.JsonText text){
        try {
            if (ClientboundSystemChatPacketClass != null) {
                return ClientboundSetActionBarTextPacketConstructor.newInstance(text.toComponent(),false);
            }
            if(ChatMessageTypeClass==null){
                return PacketPlayOutChatConstructor.newInstance(text.toComponent(),(byte)1);
            }else{
                return PacketPlayOutChatConstructor.newInstance(text.toComponent(), ChatMessageTypeSystem);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
