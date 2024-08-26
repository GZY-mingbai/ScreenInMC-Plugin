package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

public class OutActionBarPacket implements OutPacket{
    static Class ClientboundSetActionBarTextPacketClass;
    static Constructor ClientboundSetActionBarTextPacketConstructor;
    static Class PacketPlayOutChatClass;
    static Constructor PacketPlayOutChatConstructor;
    static Class ChatMessageTypeClass;
    static Class IChatBaseComponentClass;
    static Object ChatMessageTypeGameInfo;
    static Method ChatMessageTypeValues;
    static boolean needUUID = false;
    protected static void init() throws Exception {
        ClientboundSetActionBarTextPacketClass = CraftUtils.getMinecraftClass("ClientboundSetActionBarTextPacket");
        IChatBaseComponentClass = CraftUtils.getMinecraftClass("IChatBaseComponent");
        if(IChatBaseComponentClass==null){
            IChatBaseComponentClass=CraftUtils.getMinecraftClass("Component");
        }
        if(ClientboundSetActionBarTextPacketClass!=null){
            ClientboundSetActionBarTextPacketConstructor = ClientboundSetActionBarTextPacketClass.getDeclaredConstructor(IChatBaseComponentClass);
            return;
        }
        PacketPlayOutChatClass = CraftUtils.getMinecraftClass("PacketPlayOutChat");
        try {
            PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass,byte.class);
        }catch (Exception e){
            ChatMessageTypeClass = CraftUtils.getMinecraftClass("ChatMessageType");
            try{
                PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass,ChatMessageTypeClass);
            }catch (Exception er){
                needUUID = true;
                PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass,ChatMessageTypeClass,java.util.UUID.class);
            }
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
                if(needUUID)
                {
                    return PacketPlayOutChatConstructor.newInstance(text.toComponent(),ChatMessageTypeGameInfo,UUID.randomUUID());
                }else {
                    return PacketPlayOutChatConstructor.newInstance(text.toComponent(),ChatMessageTypeGameInfo);
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
