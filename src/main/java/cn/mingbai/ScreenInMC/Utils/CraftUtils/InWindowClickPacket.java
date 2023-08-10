package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
                if(i.getType().equals(int.class)){
                    if(ContainerId==null) ContainerId = i;
                    if(SlotId==null) SlotId = i;
                    if(ButtonId==null) ButtonId = i;
                    if(ActionId==null) ActionId = i;
                    if(ActionId!=null) break;
                }
            }
        }else{
            isActionNumber = false;
            InventoryClickTypeClass = CraftUtils.getMinecraftClass("InventoryClickType");
            InventoryClickTypeOrdinal = Enum.class.getDeclaredMethod("ordinal");
            if(CraftUtils.minecraftVersion<17){
                for(Field i:PacketPlayInWindowClickClass.getDeclaredFields()){
                    if(i.getType().equals(int.class)){
                        if(ContainerId==null) ContainerId = i;
                        if(SlotId==null) SlotId = i;
                        if(ButtonId==null) ButtonId = i;
                    }
                    if(i.getType().equals(InventoryClickTypeClass)){
                        ActionId = i;
                        break;
                    }
                }
            }else {
                boolean skippedStateId = false;
                for (Field i : PacketPlayInWindowClickClass.getDeclaredFields()) {
                    if (i.getType().equals(int.class)) {
                        if (ContainerId == null) ContainerId = i;
                        if (CraftUtils.minecraftVersion < 18) {
                            if (SlotId == null) SlotId = i;
                            if (ButtonId == null) {
                                if (skippedStateId) {
                                    ButtonId = i;
                                } else {
                                    skippedStateId = true;
                                }
                            }
                        } else {
                            if (SlotId == null) {
                                if (skippedStateId) {
                                    SlotId = i;
                                } else {
                                    skippedStateId = true;
                                }
                            }
                            if (ButtonId == null) ButtonId = i;
                        }
                    }
                    if (i.getType().equals(InventoryClickTypeClass)) {
                        ActionId = i;
                        break;
                    }
                }
            }
        }

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
            return (int) ContainerId.get(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public int getSlotNum(){
        try {
            return (int) SlotId.get(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ClickType getClickType(){
        try {
            if(isActionNumber){
                return ClickType.values()[(int)ActionId.get(packet)];
            }else{
                return ClickType.values()[(int) InventoryClickTypeOrdinal.invoke(ActionId.get(packet))];
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static enum ClickType{
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;
    }
}
