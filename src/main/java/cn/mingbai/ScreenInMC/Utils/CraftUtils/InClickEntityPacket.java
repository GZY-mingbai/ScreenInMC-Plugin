package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InClickEntityPacket implements InPacket {
    static Class PacketPlayInUseEntityClass;
    static Class EnumEntityUseActionClass;
    static Method EnumEntityUseActionOrdinal;
    static Method ActionGetEnum;
    static Field Action;
    static Field Id;
    static int method=0;
    public static final int INTERACT=0, ATTACK=1, INTERACT_AT=2;
    protected static void init() throws Exception {
        PacketPlayInUseEntityClass = CraftUtils.getMinecraftClass("PacketPlayInUseEntity");
        if(PacketPlayInUseEntityClass==null){
            PacketPlayInUseEntityClass = CraftUtils.getMinecraftClass("ServerboundInteractPacket");
        }
        EnumEntityUseActionClass = CraftUtils.getMinecraftClass("EnumEntityUseAction",true);
        if(EnumEntityUseActionClass==null){
            EnumEntityUseActionClass=CraftUtils.getMinecraftClass("ActionType",true);
        }
        EnumEntityUseActionOrdinal = Enum.class.getDeclaredMethod("ordinal");
        if(EnumEntityUseActionClass.isEnum()){
            method=0;
        }else {
            method=1;
            for (Method i:EnumEntityUseActionClass.getDeclaredMethods()){
                if(i.getReturnType().isEnum()&&i.getParameterCount()==0){
                    ActionGetEnum=i;
                    ActionGetEnum.setAccessible(true);
                }
            }
        }
        for(Field i:PacketPlayInUseEntityClass.getDeclaredFields()){
            if(i.getType().equals(int.class)){
                if(Id==null) {
                    Id = i;
                    Id.setAccessible(true);
                }
                continue;
            }
            if(i.getType().getSimpleName().equals("Action")){
                if(Action==null){
                    Action = i;
                    Action.setAccessible(true);
                }
                continue;
            }
        }
    }
    @Override
    public Class getNMSClass() {
        return PacketPlayInUseEntityClass;
    }
    Object packet;
    @Override
    public boolean load(Object obj) {
        packet=obj;
        return true;
    }
    public int getEntityId(){
        try {
            return Utils.getInt(Id.get(packet));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public int getAction(){
        try {
            Object i = Action.get(packet);
            if(i.getClass().getSimpleName().equals("InteractionAtLocationAction")){
                return INTERACT_AT;
            }
            if(i.getClass().getSimpleName().equals("InteractionAction")){
                return INTERACT;
            }
            for(Method j : i.getClass().getDeclaredMethods()) {
                if(j.getReturnType().equals(EnumEntityUseActionClass)){
                    j.setAccessible(true);
                    i = j.invoke(null);
                    break;
                }
            }
            if(method==0){
                return Utils.getInt(EnumEntityUseActionOrdinal.invoke(i));
            }
            if(method==1){
                Object j = ActionGetEnum.invoke(i);
                return Utils.getInt(EnumEntityUseActionOrdinal.invoke(j));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("");
    }
}
