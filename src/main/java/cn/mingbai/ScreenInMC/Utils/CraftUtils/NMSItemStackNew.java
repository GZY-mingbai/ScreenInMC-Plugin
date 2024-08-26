package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.CraftItemStackAsBukkitCopy;

public class NMSItemStackNew extends NMSItemStack{
    static Class DataComponentHolderClass;
    static Method DataComponentHolderGetComponents;
    static Class PatchedDataComponentMapClass;
    static Method PatchedDataComponentMapSet;
    static Method PatchedDataComponentMapGet;
    static Constructor ResourceLocationConstructor;
    static Method RegistryGet;
    static Object DataComponentTypeRegistry;

    protected NMSItemStackNew(String[] itemIds, int count) {
        super(itemIds, count);
    }

    protected NMSItemStackNew(String[] itemIds, int count, short oldId) {
        super(itemIds, count, oldId);
    }

    static void init() throws Exception {
        DataComponentHolderClass = CraftUtils.getMinecraftClass("DataComponentHolder");
        for(Method i : DataComponentHolderClass.getDeclaredMethods()){
            if(i.getReturnType().getSimpleName().equals("DataComponentMap")){
                DataComponentHolderGetComponents = i;
                DataComponentHolderGetComponents.setAccessible(true);
            }
        }
        if(DataComponentHolderGetComponents==null) throw new Exception("DataComponentMap getComponents() not found");
        PatchedDataComponentMapClass = CraftUtils.getMinecraftClass("PatchedDataComponentMap");
        for(Method i : PatchedDataComponentMapClass.getDeclaredMethods()){
            if(i.getParameterCount()==2&&i.getParameters()[0].getType().getSimpleName().equals("DataComponentType")&&i.getParameters()[1].getType().equals(Object.class)){
                PatchedDataComponentMapSet = i;
                PatchedDataComponentMapSet.setAccessible(true);
            }
            if(i.getParameterCount()==1&&i.getParameters()[0].getType().getSimpleName().equals("DataComponentType")){
                PatchedDataComponentMapGet = i;
                PatchedDataComponentMapGet.setAccessible(true);
            }
        }
        if(DataComponentHolderGetComponents==null) throw new Exception("public Object set(DataComponentType dataComponentType, Object t) not found");
        Class ResourceLocationClass = CraftUtils.getMinecraftClass("ResourceLocation");
        try {
            ResourceLocationConstructor = ResourceLocationClass.getDeclaredConstructor(String.class);
        }catch (Exception e){
            ResourceLocationConstructor = ResourceLocationClass.getDeclaredConstructor(String.class,String.class);
        }
        ResourceLocationConstructor.setAccessible(true);
        Class BuiltInRegistriesClass = CraftUtils.getMinecraftClass("BuiltInRegistries");
        for(Field i : BuiltInRegistriesClass.getDeclaredFields()){
            if(Modifier.isStatic(i.getModifiers())&&i.getType().getSimpleName().equals("Registry")&&i.getGenericType().getTypeName().contains("DataComponentType")){
                i.setAccessible(true);
                Object reg = i.get(null);
                if(reg.toString().contains("minecraft:data_component_type")){
                    DataComponentTypeRegistry = reg;
                }
            }
        }
        for(Method i : DataComponentTypeRegistry.getClass().getDeclaredMethods()){
            if(!Modifier.isStatic(i.getModifiers())&&i.getParameterCount()==1&&i.getParameters()[0].getType().equals(ResourceLocationClass)&&i.getReturnType().equals(Object.class)){
                RegistryGet = i;
                RegistryGet.setAccessible(true);
            }
        }
        if(RegistryGet==null) throw new Exception("Object get(ResourceLocation location) not found");
        LoreDataComponentType = getDataComponentType("lore");
        UnbreakableDataComponentType = getDataComponentType("unbreakable");
        CustomModelDataDataComponentType = getDataComponentType("custom_model_data");
        NameDataComponentType = getDataComponentType("item_name");
        CustomDataDataDataComponentType = getDataComponentType("custom_data");
        ItemLoreClass = CraftUtils.getMinecraftClass("ItemLore");
        ItemLoreConstructor = ItemLoreClass.getDeclaredConstructor(List.class,List.class);
        UnbreakableClass = CraftUtils.getMinecraftClass("Unbreakable");
        UnbreakableConstructor = UnbreakableClass.getDeclaredConstructor(boolean.class);
        CustomModelDataClass = CraftUtils.getMinecraftClass("CustomModelData");
        CustomModelDataConstructor = CustomModelDataClass.getDeclaredConstructor(int.class);
        CustomDataClass = CraftUtils.getMinecraftClass("CustomData");
        CompoundTagClass = CraftUtils.getMinecraftClass("CompoundTag");
        for(Method i : CustomDataClass.getDeclaredMethods()){
            if(Modifier.isStatic(i.getModifiers())&&i.getParameterCount()==1&&i.getParameters()[0].getType().equals(CompoundTagClass)){
                CustomDataOf = i;
            }
        }
        if(CustomDataOf==null) throw new Exception("public static CustomData of(CompoundTag nbt) not found");
        CompoundTagConstructor = CompoundTagClass.getDeclaredConstructor();
        for(Method i : CompoundTagClass.getDeclaredMethods()){
            if(!Modifier.isStatic(i.getModifiers()) && i.getParameterCount()==2 &&
                    i.getParameters()[0].getType().equals(String.class)  && i.getParameters()[1].getType().equals(String.class)){
                CompoundTagPutString = i;
            }
            if(!Modifier.isStatic(i.getModifiers()) && i.getParameterCount()==1 &&
                    i.getParameters()[0].getType().equals(String.class)  && i.getReturnType().equals(String.class)
            &&Modifier.isPublic(i.getModifiers())){
                CompoundTagGetString = i;
            }
        }
        if(CompoundTagPutString==null){
            throw new Exception("public void putString(String key, String value) not found");
        }
        for(Field i : CustomDataClass.getDeclaredFields()){
            if(i.getType().equals(CompoundTagClass)){
                CustomDataTag = i;
                CustomDataTag.setAccessible(true);
            }
        }
        for(Field i : CustomModelDataClass.getDeclaredFields()){
            if(i.getType().equals(int.class)){
                CustomModelDataValue = i;
                CustomModelDataValue.setAccessible(true);
            }
        }
        NMSMapNew.init();
        NMSBookNew.init();
    }
    static Field CustomDataTag;
    static Field CustomModelDataValue;
    static Method CompoundTagPutString;
    static Method CompoundTagGetString;
    static Constructor CompoundTagConstructor;
    static Class CompoundTagClass;
    static Method CustomDataOf;
    static Class CustomDataClass;
    static Constructor CustomModelDataConstructor;
    static Class CustomModelDataClass;
    static Constructor UnbreakableConstructor;
    static Class UnbreakableClass;
    static Constructor ItemLoreConstructor;
    static Class ItemLoreClass;
    static Object LoreDataComponentType;
    static Object UnbreakableDataComponentType;
    static Object CustomModelDataDataComponentType;
    static Object NameDataComponentType;
    static Object CustomDataDataDataComponentType;
    public static Object getDataComponentType(String name) throws Exception{
        Object location;
        if(ResourceLocationConstructor.getParameterCount()==1){
            location = ResourceLocationConstructor.newInstance(name);
        }else{
            location = ResourceLocationConstructor.newInstance("minecraft",name);
        }
        Object obj = RegistryGet.invoke(DataComponentTypeRegistry,location);
        if(obj.getClass().equals(Optional.class)){
            Optional optional = (Optional) obj;
            Object reference = optional.get();
            for(Field i : reference.getClass().getDeclaredFields()){
                if(i.getType().equals(Object.class)){
                    i.setAccessible(true);
                    return i.get(reference);
                }
            }
        }
        return obj;
    }

    @Override
    protected void setNbtNew(Object itemStack) {
        try {
            Object patchedDataComponentMap = DataComponentHolderGetComponents.invoke(itemStack);
            if(lore!=null){
                List<Object> lores = new ArrayList<>();
                for (LangUtils.JsonText i : lore) {
                    lores.add(i.toComponent());
                }
                PatchedDataComponentMapSet.invoke(patchedDataComponentMap,LoreDataComponentType,ItemLoreConstructor.newInstance(lores,new ArrayList<>()));
            }
            PatchedDataComponentMapSet.invoke(patchedDataComponentMap,UnbreakableDataComponentType,UnbreakableConstructor.newInstance(unbreakable));
            if(customModelData!=null){
                PatchedDataComponentMapSet.invoke(patchedDataComponentMap,CustomModelDataDataComponentType,CustomModelDataConstructor.newInstance((int)customModelData));
            }
            if(name!=null){
                PatchedDataComponentMapSet.invoke(patchedDataComponentMap,NameDataComponentType,name.toComponent());
            }
            if(screenInMCData!=null){
                Object tag = CompoundTagConstructor.newInstance();
                CompoundTagPutString.invoke(tag,"ScreenInMCData",screenInMCData);
                Object customData = CustomDataOf.invoke(null,tag);
                PatchedDataComponentMapSet.invoke(patchedDataComponentMap,CustomDataDataDataComponentType,customData);
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String readScreenInMCData(ItemStack itemStack) {
        try {
            Object item = bukkitToNmsItemStack(itemStack);
            Object patchedDataComponentMap = DataComponentHolderGetComponents.invoke(item);
            Object customData = PatchedDataComponentMapGet.invoke(patchedDataComponentMap,CustomDataDataDataComponentType);
            Object tag = CustomDataTag.get(customData);
            return (String) CompoundTagGetString.invoke(tag,"ScreenInMCData");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static void writeScreenInMCData(ItemStack itemStack,String data) {
        try {
            Object item = bukkitToNmsItemStack(itemStack);
            Object patchedDataComponentMap = DataComponentHolderGetComponents.invoke(item);
            Object tag = CompoundTagConstructor.newInstance();
            CompoundTagPutString.invoke(tag,"ScreenInMCData",data);
            Object customData = CustomDataOf.invoke(null,tag);
            PatchedDataComponentMapSet.invoke(patchedDataComponentMap,CustomDataDataDataComponentType,customData);
            ItemStack result = (ItemStack) CraftItemStackAsBukkitCopy.invoke(null,item);
            itemStack.setItemMeta(result.getItemMeta());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static int getCustomModelData(ItemStack itemStack) {
        try {
            Object item = bukkitToNmsItemStack(itemStack);
            Object patchedDataComponentMap = DataComponentHolderGetComponents.invoke(item);
            Object customModelData = PatchedDataComponentMapGet.invoke(patchedDataComponentMap,CustomModelDataDataComponentType);
            if(customModelData==null){
                return 0;
            }
            return (int) CustomModelDataValue.get(customModelData);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
