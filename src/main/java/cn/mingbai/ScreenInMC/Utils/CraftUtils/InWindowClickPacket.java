package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InWindowClickPacket implements InPacket {
    static Class PacketPlayInWindowClickClass;
    static Field ContainerId;
    static Field SlotId;
    static Field ButtonId;
    static Field ActionId;
    static boolean isActionNumber = true;
    static Class InventoryClickTypeClass;
    static Method InventoryClickTypeOrdinal;

    protected static void init() throws Exception{
        PacketPlayInWindowClickClass = CraftUtils.getMinecraftClass("PacketPlayInWindowClick");
        if(CraftUtils.minecraftVersion<9){
            isActionNumber = true;
            for(Field i:PacketPlayInWindowClickClass.getDeclaredFields()){
                if(Modifier.isStatic(i.getModifiers())) continue;
                if(i.getType().equals(int.class)){
                    if(ContainerId==null) {ContainerId = i;continue;}
                    if(SlotId==null) {SlotId = i;continue;}
                    if(ButtonId==null) {ButtonId = i;continue;}
                    if(ActionId==null) {ActionId = i;continue;}
                    if(ActionId!=null) break;
                }
            }
        }else{
            isActionNumber = false;
            InventoryClickTypeClass = CraftUtils.getMinecraftClass("InventoryClickType");
            InventoryClickTypeOrdinal = Enum.class.getDeclaredMethod("ordinal");
            if(CraftUtils.minecraftVersion<17){
                for(Field i:PacketPlayInWindowClickClass.getDeclaredFields()){
                    if(Modifier.isStatic(i.getModifiers())) continue;
                    if(i.getType().equals(int.class)){
                        if(ContainerId==null) {ContainerId = i;continue;}
                        if(SlotId==null) {SlotId = i;continue;}
                        if(ButtonId==null) {ButtonId = i;continue;}
                    }
                    if(i.getType().equals(InventoryClickTypeClass)){
                        ActionId = i;
                        break;
                    }
                }
            }else {
                boolean skippedStateId = false;
                for (Field i : PacketPlayInWindowClickClass.getDeclaredFields()) {
                    if(Modifier.isStatic(i.getModifiers())) continue;
                    if (i.getType().equals(int.class)) {
                        if (ContainerId == null) {ContainerId = i;continue;}
                        if (SlotId == null) {
                            if (skippedStateId) {
                                SlotId = i;
                            } else {
                                skippedStateId = true;
                                continue;
                            }
                        }
                        if (ButtonId == null) {ButtonId = i;continue;}
                    }
                    if (i.getType().equals(InventoryClickTypeClass)) {
                        ActionId = i;
                        break;
                    }
                }
            }
        }
        ContainerId.setAccessible(true);
        SlotId.setAccessible(true);
        ActionId.setAccessible(true);
        ButtonId.setAccessible(true);


    }
    @Override
    public Class getNMSClass() {
        return PacketPlayInWindowClickClass;
    }
    Object packet;
    @Override
    public boolean load(Object obj) {
        packet = obj;
        return true;
    }
    public int getContainerId(){
        try {
            return Utils.getInt(ContainerId.get(packet));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public int getSlotNum(){
        try {
            return Utils.getInt(SlotId.get(packet));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ClickType getClickType(){
        try {
            if(isActionNumber){
                return ClickType.values()[Utils.getInt(ActionId.get(packet))];
            }else{
                return ClickType.values()[Utils.getInt(InventoryClickTypeOrdinal.invoke(ActionId.get(packet)))];
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static enum ClickType{
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;
    }
}
