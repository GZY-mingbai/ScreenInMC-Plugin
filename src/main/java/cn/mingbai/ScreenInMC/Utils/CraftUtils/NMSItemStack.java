package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
    static Constructor NBTTagCompoundClassConstructor;
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
        NBTTagCompoundClassConstructor = getConstructor(NBTTagCompoundClass);
        for(Method i:ItemStackClass.getDeclaredMethods()){
            if(i.getParameterCount()==2&&i.getParameters()[0].getType().equals(String.class)&&i.getParameters()[1].getType().getSimpleName().equals("NBTBase")){
                NBTTagCompoundPut=i;
            }
            if(i.getParameterCount()==1&&i.getParameters()[0].getType().equals(String.class)&&i.getReturnType().getSimpleName().equals("NBTTagCompound")){
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
            }
        }
        for(Field i:NBTTagStringClass.getDeclaredFields()){
            if(!Modifier.isStatic(i.getModifiers())&&i.getType().equals(String.class)){
                NBTTagStringString=i;
                i.setAccessible(true);
            }
        }
        if(NBTTagListNBTBaseList==null){
            throw new RuntimeException("private List<NBTBase> ... = Lists.newArrayList(); not found");
        }
    }
    private String[] itemIds;
    private int count;
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
            Object item = CraftItemStackAsNMSCopy.invoke(itemStack);
            Object nbt = ItemStackNBTTagCompound.get(item);
            if(nbt==null)return -1;
            Object obj = NBTTagCompoundGet.invoke(nbt,"CustomModelData");
            if(obj==null)return -1;
            try {
                return (int) NBTTagStringString.get(obj);
            }catch (Exception e){
                return -1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LangUtils.JsonText[] lore = new LangUtils.JsonText[0];
    private LangUtils.JsonText name = null;
    public static NMSItemStack EMPTY = new NMSItemStack(new String[0],1);

    // When generating items,
    // each item ID will be tried one by one from the itemIds list.
    // If the item with that ID does not exist in the current version of Minecraft, the next ID will be tried.
    public NMSItemStack(String[] itemIds,int count){
        this.itemIds = itemIds;
        this.count=count;
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
        //Caused by: java.lang.IllegalArgumentException: object is not an instance of declaring class
        //报错
        Object displayNbt = NBTTagCompoundClassConstructor.newInstance();
        NBTTagCompoundPut.invoke(nbt,"display",displayNbt);
        Object nameNbt = NBTTagStringClassConstructor.newInstance(name.toJSON());
        NBTTagCompoundPut.invoke(displayNbt,"Name",nameNbt);
        Object loreNbt = NBTTagListClassConstructor.newInstance();
        List list = (List) NBTTagListNBTBaseList.get(loreNbt);
        for(LangUtils.JsonText i:lore){
            Object singleLoreNbt = NBTTagStringClassConstructor.newInstance(i.toJSONWithoutExtra());
            list.add(singleLoreNbt);
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
        NBTTagCompoundPut.invoke(displayNbt,"Lore",loreNbt);
    }
    public Object getItemStack(){
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
            Object stack = bukkitToNmsItemStack(new ItemStack(itemType,count));
            Object nbt = NBTTagCompoundClassConstructor.newInstance();
            setNbt(nbt);
            ItemStackNBTTagCompound.set(stack,nbt);
            return stack;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String readScreenInMCData(ItemStack itemStack){
        try {
            Object item = CraftItemStackAsNMSCopy.invoke(itemStack);
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
            Object item = CraftItemStackAsNMSCopy.invoke(itemStack);
            Object nbt = ItemStackNBTTagCompound.get(item);
            if(nbt==null) {
                nbt = NBTTagCompoundClassConstructor.newInstance();
                ItemStackNBTTagCompound.set(item,nbt);
            }
            Object string = NBTTagStringClassConstructor.newInstance(data);
            NBTTagCompoundPut.invoke(nbt,"ScreenInMCData",string);
            ItemStack result = (ItemStack) CraftItemStackAsBukkitCopy.invoke(item);
            itemStack.setItemMeta(result.getItemMeta());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ItemStack getBukkitItemStack(){
        return nmsToBukkitItemStack(getItemStack());
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
}
