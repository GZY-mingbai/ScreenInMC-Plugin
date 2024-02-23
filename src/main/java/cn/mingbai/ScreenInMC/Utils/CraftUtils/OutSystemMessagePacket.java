package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

public class OutSystemMessagePacket implements OutPacket {
    static Class ClientboundSystemChatPacketClass;
    static Constructor ClientboundSetActionBarTextPacketConstructor;
    static Class PacketPlayOutChatClass;
    static Constructor PacketPlayOutChatConstructor;
    static Class ChatMessageTypeClass;
    static Class IChatBaseComponentClass;
    static Object ChatMessageTypeSystem;
    static Method ChatMessageTypeValues;
    static boolean needUUID = false;
    static boolean isClientboundSetActionBarTextPacketConstructorBoolean = false;

    protected static void init() throws Exception {
        ClientboundSystemChatPacketClass = CraftUtils.getMinecraftClass("ClientboundSystemChatPacket");
        IChatBaseComponentClass = CraftUtils.getMinecraftClass("IChatBaseComponent");
        if (ClientboundSystemChatPacketClass != null) {
            try {
                ClientboundSetActionBarTextPacketConstructor = ClientboundSystemChatPacketClass.getDeclaredConstructor(IChatBaseComponentClass, boolean.class);
                isClientboundSetActionBarTextPacketConstructorBoolean = true;
            } catch (Exception e) {
                ClientboundSetActionBarTextPacketConstructor = ClientboundSystemChatPacketClass.getDeclaredConstructor(IChatBaseComponentClass, int.class);
                isClientboundSetActionBarTextPacketConstructorBoolean = false;
            }
            return;
        }
        PacketPlayOutChatClass = CraftUtils.getMinecraftClass("PacketPlayOutChat");
        try {
            PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass, byte.class);
        } catch (Exception e) {
            ChatMessageTypeClass = CraftUtils.getMinecraftClass("ChatMessageType");
            try {
                PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass, ChatMessageTypeClass);
            } catch (Exception er) {
                needUUID = true;
                PacketPlayOutChatConstructor = PacketPlayOutChatClass.getDeclaredConstructor(IChatBaseComponentClass, ChatMessageTypeClass, java.util.UUID.class);
            }
            ChatMessageTypeValues = ChatMessageTypeClass.getDeclaredMethod("values");
            ChatMessageTypeSystem = ((Object[]) ChatMessageTypeValues.invoke(null))[1];
        }
    }

    public static Object create(LangUtils.JsonText text) {
        try {
            if (ClientboundSystemChatPacketClass != null) {
                if (isClientboundSetActionBarTextPacketConstructorBoolean) {
                    return ClientboundSetActionBarTextPacketConstructor.newInstance(text.toComponent(), false);
                } else {
                    return ClientboundSetActionBarTextPacketConstructor.newInstance(text.toComponent(), 0);
                }
            }
            if (ChatMessageTypeClass == null) {
                return PacketPlayOutChatConstructor.newInstance(text.toComponent(), (byte) 1);
            } else {
                if (needUUID) {
                    return PacketPlayOutChatConstructor.newInstance(text.toComponent(), ChatMessageTypeSystem, UUID.randomUUID());
                } else {
                    return PacketPlayOutChatConstructor.newInstance(text.toComponent(), ChatMessageTypeSystem);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
