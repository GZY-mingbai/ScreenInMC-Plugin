package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import io.netty.channel.Channel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.entity.Player;
import org.bukkit.material.Diode;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

public class CraftUtils {
    static final String craftBukkit = "org.bukkit.craftbukkit";
    static final String minecraft = "net.minecraft";
    public static int minecraftVersion = 20;
    public static int subMinecraftVersion = 0;

    static Method CraftPlayerGetHandle;
    static Class ConnectionClass;
    static Field ServerPlayerConnection;
    static Class NetworkManagerClass;
    static Field ConnectionNetworkManager;
    static Field NetworkManagerChannel;
    static Method CraftItemStackAsNMSCopy;
    static Method CraftItemStackAsBukkitCopy;
    static Method CraftItemStackAsCraftMirror;
    static Method CraftWorldGetHandle;

    static Class[] minecraftClasses = new Class[0];
    static Constructor getConstructor(Class cls){
        try {
            return cls.getDeclaredConstructor();
        }catch (Exception e) {
            return cls.getDeclaredConstructors()[0];
        }
    }
    static Method getMethod(Class cls,String name){
        try {
            return cls.getDeclaredMethod(name);
        }catch (Exception e) {
            for(Method i:cls.getDeclaredMethods()){
                if(i.getName().equals(name)) return i;
            }
        }
        throw new RuntimeException("Not found "+cls.getName()+":"+name);
    }
    public static boolean isClassExists(String className){
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    public static void init() throws Exception{
            for(int i=8;i<21;i++) {
                if (Bukkit.getBukkitVersion().contains("1."+i)) {
                    minecraftVersion = i;
                }
            }
            for(int i=0;i<20;i++){
                if (Bukkit.getBukkitVersion().contains("1."+minecraftVersion+"."+i)) {
                    subMinecraftVersion = i;
                }
            }
            String[] craftBukkitSubClasses = getSubClasses(craftBukkit);
                for(String i: craftBukkitSubClasses){
                    if(i.endsWith("entity.CraftPlayer")){
                        Class craftPlayerClass = Class.forName(i);
                        CraftPlayerGetHandle = getMethod(craftPlayerClass,"getHandle");
                    }
                    if(i.endsWith("CraftWorld")){
                        Class craftWorldClass = Class.forName(i);
                        CraftWorldGetHandle = getMethod(craftWorldClass,"getHandle");
                    }
                    if(i.endsWith("inventory.CraftItemStack")){
                        Class craftItemStackClass = Class.forName(i);
                        CraftItemStackAsNMSCopy = getMethod(craftItemStackClass,"asNMSCopy");
                        CraftItemStackAsBukkitCopy = getMethod(craftItemStackClass,"asBukkitCopy");
                        CraftItemStackAsCraftMirror = getMethod(craftItemStackClass,"asCraftMirror");
                    }
                }
            for(Field field :CraftPlayerGetHandle.getReturnType().getDeclaredFields()) {
                if(field.getType().getSimpleName().equals("PlayerConnection")){
                    ConnectionClass=field.getType();
                    ServerPlayerConnection = field;
                    ServerPlayerConnection.setAccessible(true);
                }
            }
            for(Field field:ConnectionClass.getDeclaredFields()){
                if(field.getType().getSimpleName().equals("NetworkManager")) {
                    NetworkManagerClass=field.getType();
                    ConnectionNetworkManager = field;
                    ConnectionNetworkManager.setAccessible(true);
                }
            }
            if(ConnectionNetworkManager == null){
                for(Field field:ConnectionClass.getSuperclass().getDeclaredFields()){
                    if(field.getType().getSimpleName().equals("NetworkManager")) {
                        NetworkManagerClass=field.getType();
                        ConnectionNetworkManager = field;
                        ConnectionNetworkManager.setAccessible(true);
                    }
                }
            }
            for(Field field:NetworkManagerClass.getDeclaredFields()){
                if(field.getType().getSimpleName().equals("Channel")) {
                    NetworkManagerChannel = field;
                    NetworkManagerChannel.setAccessible(true);
                }
            }
            String[] classesNames = getSubClasses(minecraft);
            List<Class> classes = new ArrayList<>();
            for (String i:classesNames){
                try {
                    classes.add(Class.forName(i));
                } catch (NoClassDefFoundError error){}
                catch (IncompatibleClassChangeError error){}
                catch (Error error){}
                catch (Throwable error){}
            }
            minecraftClasses = classes.toArray(new Class[0]);
            JsonTextToNMSComponent.init();
            NMSItemStack.init();
            OutPacket.initAll();
            InPacket.initAll();
            PacketListener.init();
            if(minecraftVersion<=12){
                PacketPlayOutNamedSoundEffectClass = getMinecraftClass("PacketPlayOutNamedSoundEffect");
                PacketPlayOutWorldParticlesClass = getMinecraftClass("PacketPlayOutWorldParticles");
                EnumParticleClass = getMinecraftClass("EnumParticle");
                SoundEffectClass = getMinecraftClass("SoundEffect");
                SoundCategoryClass = getMinecraftClass("SoundCategory");
                PacketPlayOutWorldParticlesConstructor = PacketPlayOutWorldParticlesClass.getDeclaredConstructor(EnumParticleClass,boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class);
                for(Method i:EnumParticleClass.getDeclaredMethods()){
                    if(Modifier.isStatic(i.getModifiers())&&i.getParameterCount()==1&&i.getParameters()[0].getType().equals(int.class)){
                        fireWorkParticle = i.invoke(null,3);
                        redstoneParticle = i.invoke(null,30);
                    }
                }
                if(minecraftVersion>=9) {
                    PacketPlayOutNamedSoundEffectConstructor = PacketPlayOutNamedSoundEffectClass.getDeclaredConstructor(SoundEffectClass, SoundCategoryClass, double.class, double.class, double.class, float.class, float.class);
                }else {
                    PacketPlayOutNamedSoundEffectConstructor = PacketPlayOutNamedSoundEffectClass.getDeclaredConstructor(String.class, double.class, double.class, double.class, float.class, float.class);
                }
            }
            if(minecraftVersion<=12&&!(minecraftVersion<=8)){
                RegistryMaterialsClass = getMinecraftClass("RegistryMaterials");
                MinecraftKeyClass = getMinecraftClass("MinecraftKey");
                MinecraftKeyConstructor = MinecraftKeyClass.getDeclaredConstructor(String.class);

                for(Field i:RegistryMaterialsClass.getDeclaredFields()){
                    if(i.getType().equals(Map.class)){
                        RegistryMaterialsMap=i;
                        RegistryMaterialsMap.setAccessible(true);
                    }
                }
                for(Field i : SoundEffectClass.getDeclaredFields()){
                    if(i.getType().equals(RegistryMaterialsClass)){
                        i.setAccessible(true);
                        Object obj = i.get(null);
                        Map map = (Map) RegistryMaterialsMap.get(obj);
                        Object needKey = MinecraftKeyConstructor.newInstance("block.note.pling");
                        for(Object key:map.keySet()){
                            Object value = map.get(key);
                            if(value.equals(needKey)){
                                SoundPling = key;
                            }
                        }
                    }
                }
                SoundCategoryValues = SoundCategoryClass.getDeclaredMethod("values");
                for(Field i:SoundCategoryClass.getDeclaredFields()){
                    if(!Modifier.isStatic(i.getModifiers())&&i.getType().equals(String.class)){
                        SoundCategoryId = i;
                        SoundCategoryId.setAccessible(true);
                    }
                }
                for(Object i: (Object[]) SoundCategoryValues.invoke(null)){
                    if(((String)SoundCategoryId.get(i)).equals("block")) {
                        CategoryBlock = i;
                    }
                }

            }
        Class blockClass = Block.class;
        try {
            SetData = blockClass.getDeclaredMethod("setData",byte.class,boolean.class);
        }catch (Exception e){}

    }
    static Method SetData;
    static Constructor PacketPlayOutWorldParticlesConstructor;
    static Class PacketPlayOutWorldParticlesClass;
    static Class EnumParticleClass;
    static Class SoundCategoryClass;
    static Field SoundCategoryId;
    static Class RegistryMaterialsClass;
    static Field RegistryMaterialsMap;
    static Class MinecraftKeyClass;
    static Constructor MinecraftKeyConstructor;
    static Method SoundCategoryValues;
    static Class SoundEffectClass;
    static Object SoundPling;
    static Object CategoryBlock;
    static Object redstoneParticle;
    static Object fireWorkParticle;
    static Class PacketPlayOutNamedSoundEffectClass;
    static Constructor PacketPlayOutNamedSoundEffectConstructor;
    static Class getMinecraftClass(String name) {
        return getMinecraftClass(name,false);
    }
    static Class getMinecraftClass(String name,boolean allowSubClass){
        for(Class i:minecraftClasses){
            try {
                if(allowSubClass){
                    if(i.getSimpleName().equals(name)) {
                        return i;
                    }
                }else{
                    String[] typeName = i.getTypeName().split("\\.");
                    if(typeName[typeName.length-1].equals(name)){
                        return i;
                    }
                }

            }catch (IncompatibleClassChangeError e){}
            catch (Error e){
            }catch (Throwable t){
            }
        }
        return null;
    }
    private static List<File> listFiles(File file){
        List<File> files = new ArrayList<>();
        if(file.isDirectory()){
            for(File i:file.listFiles())files.addAll(listFiles(i));
        }
        if(file.isFile()){
            files.add(file);
        }
        return files;
    }
    private static String[] getSubClasses(String path){
        List<String> classes = new ArrayList<>();
        ClassLoader loader = Bukkit.class.getClassLoader();
        try {
            Enumeration<URL> dirs = loader.getResources(path.replace(".", "/"));
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                if (!url.getProtocol().equals("jar") && !url.getProtocol().equals("file")){
                    try {
                        String urlStr = url.getFile();
                        String[] jarAndClass = urlStr.split("!");
                        if(jarAndClass.length==2){
                            int jarIndex = jarAndClass[0].lastIndexOf("%");
                            String className = jarAndClass[1];
                            url = new URL("jar:file:"+jarAndClass[0].substring(0,jarIndex)+"!"+className);
                        }
                    }catch (Exception e){}
                }
                if (url.getProtocol().equals("jar")) {
                    JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
                    Enumeration<JarEntry> entries = urlConnection.getJarFile().entries();
                    String parentPath = path.replace(".", "/");
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".class")) {
                            if (name.startsWith(parentPath)) {
                                classes.add(
                                        path + name.substring(path.length(), name.length() - 6).replace("\\", ".").replace("/", ".")
                                );
                            }
                        }
                    }
                } else if (url.getProtocol().equals("file")) {
                    File file = new File(url.getFile());
                    Path parentPath = file.toPath();
                    List<File> subFiles = listFiles(file);
                    for (File i : subFiles) {
                        Path p = i.toPath();
                        String pathStr = parentPath.relativize(p).toString();
                        if (pathStr.endsWith(".class")) {
                            pathStr = pathStr.substring(0, pathStr.length() - 6);
                            classes.add(
                                    path + "." + pathStr.replace("\\", ".").replace("/", ".")
                            );
                        }

                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return classes.toArray(new String[0]);

    }
    protected static Channel getChannel(Player player){
        try {
            Object serverPlayer = CraftPlayerGetHandle.invoke(player);
            Object connection = ServerPlayerConnection.get(serverPlayer);
            Object networkManager = ConnectionNetworkManager.get(connection);
            Channel channel = (Channel) NetworkManagerChannel.get(networkManager);
            return channel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void sendPacket(Player player, Object packet) {
        Channel channel = getChannel(player);
        channel.pipeline().writeAndFlush(packet);
    }

    public static void spawnRedstoneParticle(World world,
                                             double x,
                                             double y,
                                             double z,
                                             Color color,
                                             int size
                                             ){
        if(minecraftVersion>=13){
            world.spawnParticle(Particle.REDSTONE,x,y,z,0,0,0,0,0,new Particle.DustOptions(color,size));
        }
        else {
            try {
                Object packet = PacketPlayOutWorldParticlesConstructor.newInstance(redstoneParticle,true,(float)x,(float)y,(float)z,(float)color.getRed(),(float)color.getGreen(),(float)color.getBlue(),0f,0,new int[0]);
                for(Player i:world.getPlayers()){
                    if(i.getLocation().getWorld().equals(world)&&i.getLocation().distance(new Location(world,x,y,z))<=particleMinDistance){
                        sendPacket(i,packet);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    static final int particleMinDistance=128;
    public static void spawnFireworkParticle(World world,double x,double y,double z){
        if(minecraftVersion>=13){
            world.spawnParticle(Particle.FIREWORKS_SPARK,x,y,z,0,0,0,0,0);
        }
        else {
            try {
                Object packet = PacketPlayOutWorldParticlesConstructor.newInstance(fireWorkParticle,true,(float)x,(float)y,(float)z,0f,0f,0f,0f,0,new int[0]);
                for(Player i:world.getPlayers()){
                    if(i.getLocation().getWorld().equals(world)&&i.getLocation().distance(new Location(world,x,y,z))<=particleMinDistance){
                        sendPacket(i,packet);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void playSoundPling(Player player){
        if(minecraftVersion>=13){
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS,10,2);
            return;
        }
        try {
            Location loc = player.getLocation();
            Object packet;
            if(minecraftVersion>=9){
                packet = PacketPlayOutNamedSoundEffectConstructor.newInstance(SoundPling,CategoryBlock,(double)loc.getX(),(double)loc.getY(),(double)loc.getZ(),10f,2f);
            }else{
                packet = PacketPlayOutNamedSoundEffectConstructor.newInstance("note.pling",(double)loc.getX(),(double)loc.getY(),(double)loc.getZ(),10f,2f);
            }
            sendPacket(player,packet);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static boolean isRepeater(Material material){
        String name = material.name().toLowerCase();
        if(name.contains("diode")||name.contains("repeater")) return true;
        return false;
    }
    public static boolean checkRedstoneRepeater(Location location){
        if(location==null){
            return false;
        }
        if(location.getBlock()==null){
            return false;
        }
        return isRepeater(location.getBlock().getType());
    }
    public static Material getRepeater(){
        if(CraftUtils.minecraftVersion<=12) {
            return Material.valueOf("DIODE_BLOCK_OFF");
        }else{
            return Material.valueOf("REPEATER");
        }
    }
    public static BlockFace getRepeaterFacing(Block block){
        if(CraftUtils.minecraftVersion<=12){
            MaterialData data = block.getState().getData();
            if(data instanceof Diode){
                return ((Diode)data).getFacing().getOppositeFace();
            }
        }else{
            BlockData data = block.getState().getBlockData();
            if(data instanceof Repeater){
                return ((Repeater)data).getFacing();
            }
        }
        return null;
    }

//    public static void setRepeaterFacing(Block block,BlockFace face){
//        if(CraftUtils.minecraftVersion<=12){
//            MaterialData data = block.getState().getData();
//            if(data instanceof Diode){
//                ((Diode)data).setFacingDirection(face.getOppositeFace());
//                ((Diode)data).setDelay(0);
//            }
//            try {
//                SetData.invoke(block,data.getData(),true);
//            } catch (Exception e) {
//            }
//        }else{
//            BlockData data = block.getState().getBlockData();
//            if(data instanceof Repeater){
//                ((Repeater)data).setFacing(face);
//                ((Repeater)data).setDelay(0);
//            }
//            block.setBlockData(data,true);
//        }
//    }
//    public static void activeRepeater(Block block){
//        if(CraftUtils.minecraftVersion<=12){
//            MaterialData data = block.getState().getData();
//            block.setType(Material.valueOf("DIODE_BLOCK_ON"));
//            try {
//                SetData.invoke(block,data.getData(),true);
//            } catch (Exception e) {
//            }
//        }else{
//            BlockData data = block.getState().getBlockData();
//            if(data instanceof Repeater){
//                ((Repeater)data).setPowered(true);
//            }
//            block.setBlockData(data,true);
//        }
//
//    }
//    public static void inactiveRepeater(Block block){
//        if(CraftUtils.minecraftVersion<=12){
//            MaterialData data = block.getState().getData();
//            block.setType(Material.valueOf("DIODE_BLOCK_OFF"));
//            try {
//                SetData.invoke(block,data.getData(),true);
//            } catch (Exception e) {
//            }
//        }else{
//            BlockData data = block.getState().getBlockData();
//            if(data instanceof Repeater){
//                ((Repeater)data).setPowered(false);
//            }
//            block.setBlockData(data,true);
//        }
//    }
}
