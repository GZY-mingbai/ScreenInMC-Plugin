package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.lang.reflect.*;
import java.util.List;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.*;

public class NMSItemStack {
    static Class ItemStackClass;
    static Class ItemClass;

    static Class ItemsClass;
    static Class IMaterialClass;

    static Constructor ItemStackClassConstructor;
    static Class NBTTagCompoundClass;
    static Field ItemStackNBTTagCompound;
    static Constructor NBTTagCompoundConstructor;
    static Method NBTTagCompoundPut;
    static Method NBTTagCompoundGet;
    static Class NBTTagStringClass;
    static Constructor NBTTagStringClassConstructor;
    static Class NBTTagListClass;
    static Constructor NBTTagListClassConstructor;
    static Field NBTTagListNBTBaseList;
    static Class NBTTagIntClass;
    static Constructor NBTTagIntClassConstructor;
    static Class NBTTagByteClass;
    static Constructor NBTTagByteClassConstructor;
    static Field NBTTagStringString;
    static Field NBTTagIntInt;
    static Method GetItemInHand;
    static Method SetItemInHand;




    static void init() throws Exception{
        ItemStackClass=CraftUtils.getMinecraftClass("ItemStack");
        IMaterialClass=CraftUtils.getMinecraftClass("IMaterial");
        ItemClass=CraftUtils.getMinecraftClass("Item");
        ItemsClass=CraftUtils.getMinecraftClass("Items");
        NBTTagCompoundClass=CraftUtils.getMinecraftClass("NBTTagCompound");
        try {
            ItemStackClassConstructor=ItemStackClass.getDeclaredConstructor(ItemClass,int.class);
        }catch (Exception e){
            ItemStackClassConstructor=ItemStackClass.getDeclaredConstructor(IMaterialClass,int.class);
        }
        for(Field i:ItemStackClass.getDeclaredFields()){
            if(i.getType().equals(NBTTagCompoundClass)&& !Modifier.isStatic(i.getModifiers())){
                ItemStackNBTTagCompound=i;
            }
        }
        if(ItemStackNBTTagCompound==null){
            throw new RuntimeException("private NBTTagCompound ... not found");
        }
        ItemStackNBTTagCompound.setAccessible(true);
        NBTTagCompoundConstructor = getConstructor(NBTTagCompoundClass);
        for(Method i:NBTTagCompoundClass.getDeclaredMethods()){
            if(i.getParameterCount()==2&&i.getParameters()[0].getType().equals(String.class)&&i.getParameters()[1].getType().getSimpleName().equals("NBTBase")){
                NBTTagCompoundPut=i;
            }
            if(i.getParameterCount()==1&&i.getParameters()[0].getType().equals(String.class)&&i.getReturnType().getSimpleName().equals("NBTBase")){
                NBTTagCompoundGet=i;
            }
        }
        NBTTagStringClass=CraftUtils.getMinecraftClass("NBTTagString");
        NBTTagStringClassConstructor = NBTTagStringClass.getDeclaredConstructor(String.class);
        NBTTagStringClassConstructor.setAccessible(true);

        NBTTagIntClass=CraftUtils.getMinecraftClass("NBTTagInt");
        NBTTagIntClassConstructor = NBTTagIntClass.getDeclaredConstructor(int.class);
        NBTTagIntClassConstructor.setAccessible(true);

        NBTTagByteClass=CraftUtils.getMinecraftClass("NBTTagByte");
        NBTTagByteClassConstructor = NBTTagByteClass.getDeclaredConstructor(byte.class);
        NBTTagByteClassConstructor.setAccessible(true);

        NBTTagListClass=CraftUtils.getMinecraftClass("NBTTagList");
        NBTTagListClassConstructor = getConstructor(NBTTagListClass);
        NBTTagListClassConstructor.setAccessible(true);
        for(Field i:NBTTagListClass.getDeclaredFields()){
            if(i.getType().equals(List.class)){
                NBTTagListNBTBaseList = i;
                NBTTagListNBTBaseList.setAccessible(true);
            }
        }
        for(Field i:NBTTagStringClass.getDeclaredFields()){
            if(!Modifier.isStatic(i.getModifiers())&&i.getType().equals(String.class)){
                NBTTagStringString=i;
                i.setAccessible(true);
            }
        }
        for(Field i:NBTTagIntClass.getDeclaredFields()){
            if(!Modifier.isStatic(i.getModifiers())&&i.getType().equals(int.class)){
                NBTTagIntInt=i;
                i.setAccessible(true);
            }
        }
        if(NBTTagListNBTBaseList==null){
            throw new RuntimeException("private List<NBTBase> ... = Lists.newArrayList(); not found");
        }
        Class PlayerInventoryClass = Class.forName("org.bukkit.inventory.PlayerInventory");
        try {
            GetItemInHand = PlayerInventoryClass.getDeclaredMethod("getItemInMainHand");
        }catch (Exception e){
            GetItemInHand = PlayerInventoryClass.getDeclaredMethod("getItemInHand");
        }
        try {
            SetItemInHand = PlayerInventoryClass.getDeclaredMethod("setItemInMainHand",ItemStack.class);
        }catch (Exception e){
            SetItemInHand = PlayerInventoryClass.getDeclaredMethod("setItemInHand",ItemStack.class);
        }
    }
    private String[] itemIds;
    private int count;
    private short oldId;
    private boolean unbreakable = false;

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }
    private Integer customModelData=null;
    private String screenInMCData = null;

    public void setScreenInMCData(String screenInMCData) {
        this.screenInMCData = screenInMCData;
    }

    public void setCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
    }
    public static int getCustomModelData(ItemStack itemStack){
        try {
            Object item = bukkitToNmsItemStack(itemStack);
            Object nbt = ItemStackNBTTagCompound.get(item);
            if(nbt==null)return -1;
            Object obj = NBTTagCompoundGet.invoke(nbt,"CustomModelData");
            if(obj==null)return -1;
            try {
                return Utils.getInt(NBTTagIntInt.get(obj));
            }catch (Exception e){
                return -1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LangUtils.JsonText[] lore = null;
    private LangUtils.JsonText name = null;
    public static NMSItemStack EMPTY = new NMSItemStack(new String[0],1);

    public void setOldId(short oldId) {
        this.oldId = oldId;
    }

    // When generating items,
    // each item ID will be tried one by one from the itemIds list.
    // If the item with that ID does not exist in the current version of Minecraft, the next ID will be tried.
    public NMSItemStack(String[] itemIds,int count) {
        this(itemIds,count,(short) 0);
    }

    public final static short COLOR_WHITE = 0;
    public final static short COLOR_ORANGE = 1;
    public final static short COLOR_MAGENTA = 2;
    public final static short COLOR_LIGHT_BLUE = 3;
    public final static short COLOR_YELLOW = 4;
    public final static short COLOR_LIME = 5;
    public final static short COLOR_PINK = 6;
    public final static short COLOR_GRAY = 7;
    public final static short COLOR_SILVER = 8;
    public final static short COLOR_CYAN = 9;
    public final static short COLOR_PURPLE = 10;
    public final static short COLOR_BLUE = 11;
    public final static short COLOR_BROWN = 12;
    public final static short COLOR_GREEN = 13;
    public final static short COLOR_RED = 14;
    public final static short COLOR_BLACK = 15;



    public NMSItemStack(String[] itemIds,int count,short oldId){
        this.itemIds = itemIds;
        this.count=count;
        this.oldId=oldId;
    }
    public void setLore(LangUtils.JsonText[] lore){
        this.lore=lore;
    }
    public void setName(LangUtils.JsonText name){
        this.name=name;
    }

    public boolean isEmpty(){
        if(this.itemIds.length==0||this.itemIds[0].equals("air")){
            return true;
        }
        return false;
    }

    protected void setNbt(Object nbt) throws Exception {
        Object displayNbt = NBTTagCompoundConstructor.newInstance();
        if(name!=null) {
            Object nameNbt = NBTTagStringClassConstructor.newInstance(CraftUtils.minecraftVersion<=12?name.toRichString():name.toJSON());
            NBTTagCompoundPut.invoke(displayNbt, "Name", nameNbt);
        }
        if(lore!=null) {
            Object loreNbt = NBTTagListClassConstructor.newInstance();
            List list = (List) NBTTagListNBTBaseList.get(loreNbt);
            for (LangUtils.JsonText i : lore) {
                Object singleLoreNbt = NBTTagStringClassConstructor.newInstance(CraftUtils.minecraftVersion<=13?i.toRichString():i.toJSONWithoutExtra());
                list.add(singleLoreNbt);
            }
            NBTTagCompoundPut.invoke(displayNbt,"Lore",loreNbt);
        }
        if(unbreakable){
            NBTTagCompoundPut.invoke(nbt,"Unbreakable",NBTTagByteClassConstructor.newInstance((byte)1));
        }
        if(customModelData!=null){
            NBTTagCompoundPut.invoke(nbt,"CustomModelData",NBTTagIntClassConstructor.newInstance((int)customModelData.intValue()));
        }
        if(screenInMCData!=null){
            NBTTagCompoundPut.invoke(nbt,"ScreenInMCData",NBTTagStringClassConstructor.newInstance(screenInMCData));
        }
        NBTTagCompoundPut.invoke(nbt,"display",displayNbt);
    }
    public Object getItemStack(){
        if(itemIds.length==0) return bukkitToNmsItemStack(new ItemStack(Material.AIR,0));
        Material itemType=null;
        try {
            for(String id:itemIds) {
                try {
                    itemType = Material.getMaterial(id.toUpperCase());
                    if(itemType!=null) {
                        break;
                    }
                }catch (Exception e){
                }
            }
            if(itemType==null){
                throw new RuntimeException("Item: "+String.join(", ",itemIds)+" not found.");
            }
            ItemStack bukkitItem = new ItemStack(itemType,count);
            if(minecraftVersion<=12){
                bukkitItem.setDurability((short) oldId);
            }
            Object stack = bukkitToNmsItemStack(bukkitItem);
            Object nbt = NBTTagCompoundConstructor.newInstance();
            setNbt(nbt);
            ItemStackNBTTagCompound.set(stack,nbt);
            return stack;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String readScreenInMCData(ItemStack itemStack){
        try {
            Object item = bukkitToNmsItemStack(itemStack);
            Object nbt = ItemStackNBTTagCompound.get(item);
            if(nbt==null)return "";
            Object obj = NBTTagCompoundGet.invoke(nbt,"ScreenInMCData");
            if(obj==null)return "";
            try {
                return (String) NBTTagStringString.get(obj);
            }catch (Exception e){
                return "";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeScreenInMCData(ItemStack itemStack,String data){
        try {
            Object item = bukkitToNmsItemStack(itemStack);
            Object nbt = ItemStackNBTTagCompound.get(item);
            if(nbt==null) {
                nbt = NBTTagCompoundConstructor.newInstance();
                ItemStackNBTTagCompound.set(item,nbt);
            }
            Object string = NBTTagStringClassConstructor.newInstance(data);
            NBTTagCompoundPut.invoke(nbt,"ScreenInMCData",string);
            ItemStack result = (ItemStack) CraftItemStackAsBukkitCopy.invoke(null,item);
            itemStack.setItemMeta(result.getItemMeta());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ItemStack getBukkitItemStack(){
        return nmsToBukkitItemStack(getItemStack());
    }
    public ItemStack getCraftItemStack(){
        try {
            return (ItemStack) CraftItemStackAsCraftMirror.invoke(null,getItemStack());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static ItemStack nmsToBukkitItemStack(Object obj){
        try {
            return (ItemStack) CraftUtils.CraftItemStackAsBukkitCopy.invoke(null,obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Object bukkitToNmsItemStack(Object obj){
        try {
            return CraftItemStackAsNMSCopy.invoke(null,obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static ItemStack getItemInHand(PlayerInventory inventory){
        try {
            return (ItemStack) GetItemInHand.invoke(inventory);
        } catch (Exception e) {
            return null;
        }
    }
    public static void setItemInHand(PlayerInventory inventory,ItemStack itemStack){
        try {
            SetItemInHand.invoke(inventory,itemStack);
        } catch (Exception e) {
        }
    }
}
