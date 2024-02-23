package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.getMethod;

public class JsonTextToNMSComponent {
    static Class ChatBaseComponentClass;
    static Class ChatMessageClass;
    static Class ChatComponentKeybindClass;
    static Class ChatComponentTextClass;
    static Class ChatModifierClass;
    static Method ChatModifierSetBold;
    static Method ChatModifierSetItalic;
    static Method ChatModifierSetStrikethrough;
    static Method ChatModifierSetUnderline;
    static Method ChatModifierSetRandom;
    static Method ChatModifierSetColor;
    static Method ChatModifierSetChatClickable;
    static Method ChatModifierSetChatHoverable;
    static Class EnumChatFormatClass;
    static Class ChatClickableClass;
    static Class EnumClickActionClass;
    static Method ChatBaseComponentSetChatModifier;
    static Method ChatBaseComponentAddSibling;
    static Class ChatHexColorClass;
    static Method ChatHexColorFromEnumChatFormat;
    static Constructor ChatMessageClassConstructor;
    static Constructor ChatClickableClassConstructor;
    static Constructor LiteralContentsClassConstructor;
    static Constructor KeybindContentsClassConstructor;
    static Constructor TranslatableContentsClassConstructor;
    static Constructor ChatComponentKeybindClassConstructor;
    static Constructor ChatComponentTextClassConstructor;
    static Constructor ChatModifierClassConstructor;
    //1.19+
    static Class LiteralContentsClass;
    static Class KeybindContentsClass;
    static Class TranslatableContentsClass;
    static Class IChatMutableComponentClass;
    static Method IChatMutableComponentFromComponentContents;

    public static void init() throws Exception {
        ChatModifierClass = CraftUtils.getMinecraftClass("ChatModifier");
        ChatModifierClassConstructor = CraftUtils.getConstructor(ChatModifierClass);
        ChatModifierClassConstructor.setAccessible(true);
        EnumChatFormatClass = CraftUtils.getMinecraftClass("EnumChatFormat");
        ChatClickableClass = CraftUtils.getMinecraftClass("ChatClickable");
        ChatClickableClassConstructor = CraftUtils.getConstructor(ChatClickableClass);
        EnumClickActionClass = CraftUtils.getMinecraftClass("EnumClickAction");
        if(CraftUtils.minecraftVersion>=19){
            LiteralContentsClass = CraftUtils.getMinecraftClass("LiteralContents");
            KeybindContentsClass = CraftUtils.getMinecraftClass("KeybindContents");
            TranslatableContentsClass = CraftUtils.getMinecraftClass("TranslatableContents");
            IChatMutableComponentClass = CraftUtils.getMinecraftClass("IChatMutableComponent");
            try {
                LiteralContentsClassConstructor = CraftUtils.getConstructor(LiteralContentsClass);
            }catch (Exception e){
                LiteralContentsClassConstructor=null;
                literal:
                for(Class i:CraftUtils.getMinecraftClass("LiteralContents").getDeclaredClasses()){
                    for(Constructor j:i.getDeclaredConstructors())
                    {
                        if(j.getParameterCount()==1&&j.getParameters()[0].getType().equals(String.class)){
                            LiteralContentsClassConstructor = j;
                            LiteralContentsClass = i;
                            break literal;
                        }
                    }
                }
                if(LiteralContentsClassConstructor==null) {
                    throw new RuntimeException("class LiteralContents not found.");
                }
            }
            KeybindContentsClassConstructor = CraftUtils.getConstructor(KeybindContentsClass);
            TranslatableContentsClassConstructor = CraftUtils.getConstructor(TranslatableContentsClass);
            ChatHexColorClass = CraftUtils.getMinecraftClass("ChatHexColor");
            for(Method i:IChatMutableComponentClass.getDeclaredMethods()){
                if(i.getParameterCount()==1&&i.getParameters()[0].getType().getSimpleName().equals("ComponentContents")){
                    IChatMutableComponentFromComponentContents=i;
                }
            }
            if(IChatMutableComponentFromComponentContents==null) throw new RuntimeException("public static IChatMutableComponent ...(ComponentContents ...) not found.");
        }else {
            ChatBaseComponentClass = CraftUtils.getMinecraftClass("ChatBaseComponent");
            ChatComponentTextClass = CraftUtils.getMinecraftClass("ChatComponentText");
            ChatComponentTextClassConstructor = CraftUtils.getConstructor(ChatComponentTextClass);
            ChatMessageClass = CraftUtils.getMinecraftClass("ChatMessage");
            ChatMessageClassConstructor = CraftUtils.getConstructor(ChatMessageClass);

            if (CraftUtils.minecraftVersion >= 16) {
                ChatComponentKeybindClass = CraftUtils.getMinecraftClass("ChatComponentKeybind");
                ChatComponentKeybindClassConstructor = CraftUtils.getConstructor(ChatComponentKeybindClass);
                ChatHexColorClass = CraftUtils.getMinecraftClass("ChatHexColor");
            } else {
                if (CraftUtils.minecraftVersion >= 12) {
                    ChatComponentKeybindClass = CraftUtils.getMinecraftClass("ChatComponentKeybind");
                    ChatComponentKeybindClassConstructor = CraftUtils.getConstructor(ChatComponentKeybindClass);
                }
                ChatModifierSetBold = getMethod(ChatModifierClass,"setBold");
                ChatModifierSetItalic = getMethod(ChatModifierClass,"setItalic");
                ChatModifierSetStrikethrough = getMethod(ChatModifierClass,"setStrikethrough");
                ChatModifierSetUnderline = getMethod(ChatModifierClass,"setUnderline");
                ChatModifierSetRandom = getMethod(ChatModifierClass,"setRandom");
                ChatModifierSetColor = getMethod(ChatModifierClass,"setColor");
                ChatModifierSetChatClickable = getMethod(ChatModifierClass,"setChatClickable");
                ChatModifierSetChatHoverable = getMethod(ChatModifierClass,"setChatHoverable");
            }
        }
        if(ChatHexColorClass!=null) {
            for (Method i : ChatHexColorClass.getDeclaredMethods()) {
                if (i.getParameterCount() == 1 && i.getParameters()[0].getType().getSimpleName().equals("EnumChatFormat")) {
                    ChatHexColorFromEnumChatFormat = i;
                    ChatHexColorFromEnumChatFormat.setAccessible(true);
                }
            }
            if (ChatHexColorFromEnumChatFormat == null)
                throw new RuntimeException("public static ChatHexColor ...(EnumChatFormat ...) not found.");
        }
        for(Method i:IChatMutableComponentClass==null?
                ChatBaseComponentClass.getDeclaredMethods():
                IChatMutableComponentClass.getDeclaredMethods()){
            if(i.getParameterCount()==1){
                String paramName = i.getParameters()[0].getType().getSimpleName();
                if(paramName.equals("ChatModifier")) {
                    ChatBaseComponentSetChatModifier = i;
                }
                if(paramName.equals("IChatBaseComponent")){
                    ChatBaseComponentAddSibling = i;
                }
            }
        }
        if(ChatBaseComponentAddSibling==null) throw new RuntimeException("public IChatBaseComponent ...(IChatBaseComponent ...) not found.");
        if(ChatBaseComponentSetChatModifier==null) throw new RuntimeException("public IChatBaseComponent ...(ChatModifier ...) not found.");
    }
    public static String getKeybind(String keybind){
        switch (keybind){
            case "key.sneak":
                return "Shift(Default)";
            case "key.attack":
                return "Left-Click(Default)";
            case "key.use":
                return "Right-Click(Default)";
        }
        return "Unknown";
    }
    private static Object getColor(String color) throws Exception{
        if(color==null) return null;
        Object o = Enum.valueOf(EnumChatFormatClass, color.toUpperCase());
        if (ChatHexColorClass != null) {
            o = ChatHexColorFromEnumChatFormat.invoke(null, o);
        }
        return o;
    }
    private static Object getClickEvent(LangUtils.JsonText.ClickEvent event) throws Exception {
        if(event==null) return null;
        Object action = Enum.valueOf(EnumClickActionClass, event.action.toUpperCase());
        Object chatClickable = ChatClickableClassConstructor.newInstance(action, event.value);
        return chatClickable;
    }
    public static Object jsonTextToComponent(LangUtils.JsonText text){
        try {
            Object obj=null;
            if(text.translate!=null){
                if(CraftUtils.minecraftVersion>=19) {
                    for(Constructor i : TranslatableContentsClass.getDeclaredConstructors()){
                        if(i.getParameterCount()==1){
                            obj = i.newInstance(text.translate);
                            break;
                        }
                        if(i.getParameterCount()==3){
                            obj = i.newInstance(text.translate,null,new Object[0]);
                        }
                    }
                    throw new RuntimeException("public TranslatableContents(...) not found.");
                }else
                if(ChatMessageClass!=null){
                    obj = ChatMessageClassConstructor.newInstance(text.translate);
                }else{
                    throw new RuntimeException("ChatMessageClass not found.");
                }
            }else if(text.keybind!=null){
                if(CraftUtils.minecraftVersion>=19) {
                    obj = KeybindContentsClassConstructor.newInstance(text.keybind);
                }else
                if(ChatComponentKeybindClass!=null){
                    obj = ChatComponentKeybindClassConstructor.newInstance(text.keybind);
                }else{
                    obj = ChatComponentTextClassConstructor.newInstance(getKeybind(text.keybind));
                }
            }else {
                if(CraftUtils.minecraftVersion>=19) {
                    obj = LiteralContentsClassConstructor.newInstance(text.text == null ? "" : text.text);
                }else {
                    obj = ChatComponentTextClassConstructor.newInstance(text.text == null ? "" : text.text);
                }
            }
            if(CraftUtils.minecraftVersion>=19) {
                obj=IChatMutableComponentFromComponentContents.invoke(null,obj);
            }
            Object chatModifier = null;
            if(CraftUtils.minecraftVersion>=16){
                if(ChatModifierClassConstructor.getParameterCount()==10) {
                    chatModifier = ChatModifierClassConstructor.newInstance(
                            getColor(text.color),
                            text.bold,
                            text.italic,
                            text.underlined,
                            text.strikethrough,
                            text.obfuscated,
                            getClickEvent(text.clickEvent),
                            null,
                            null,
                            null
                    );
                }else{
                    chatModifier = ChatModifierClassConstructor.newInstance(
                            getColor(text.color),
                            text.bold,
                            text.italic,
                            text.underlined,
                            text.strikethrough,
                            text.obfuscated,
                            getClickEvent(text.clickEvent),
                            null,
                            null,
                            null,
                            null
                    );
                }

            }else {
                chatModifier = ChatModifierClassConstructor.newInstance();
                if (text.bold != null) {
                    ChatModifierSetBold.invoke(chatModifier, text.bold);
                }
                if (text.italic != null) {
                    ChatModifierSetItalic.invoke(chatModifier, text.italic);
                }
                if (text.strikethrough != null) {
                    ChatModifierSetStrikethrough.invoke(chatModifier, text.strikethrough);
                }
                if (text.underlined != null) {
                    ChatModifierSetUnderline.invoke(chatModifier, text.underlined);
                }
                if (text.obfuscated != null) {
                    ChatModifierSetRandom.invoke(chatModifier, text.obfuscated);
                }
                if (text.bold != null) {
                    ChatModifierSetBold.invoke(chatModifier, text.bold);
                }
                if (text.color != null) {
                    ChatModifierSetColor.invoke(chatModifier, getColor(text.color));
                }
                if (text.clickEvent != null) {
                    ChatModifierSetChatClickable.invoke(chatModifier, getClickEvent(text.clickEvent));
                }
            }
            ChatBaseComponentSetChatModifier.invoke(obj,chatModifier);
            if(text.extra!=null){
                ChatBaseComponentAddSibling.invoke(obj,jsonTextToComponent(text.extra));
            }
            return obj;

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
