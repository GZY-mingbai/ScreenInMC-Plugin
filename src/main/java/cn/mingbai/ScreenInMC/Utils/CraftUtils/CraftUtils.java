package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

public class CraftUtils {
    static final String craftBukkit = "org.bukkit.craftbukkit";
    static final String minecraft = "net.minecraft";
    static int minecraftVersion = 20;
    static Method CraftPlayerGetHandle;
    static Class ConnectionClass;
    static Field ServerPlayerConnection;
    static Class NetworkManagerClass;
    static Field ConnectionNetworkManager;
    static Field NetworkManagerChannel;
    static Method CraftItemStackAsNMSCopy;
    static Method CraftItemStackAsBukkitCopy;

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
    public static void init() throws Exception{
            for(int i=8;i<21;i++) {
                if (Bukkit.getBukkitVersion().contains("1."+i)) {
                    minecraftVersion = i;
                }
            }
            String[] craftBukkitSubClasses = getSubClasses(craftBukkit);
                for(String i: craftBukkitSubClasses){
                    if(i.endsWith("entity.CraftPlayer")){
                        Class craftPlayerClass = Class.forName(i);
                        CraftPlayerGetHandle = getMethod(craftPlayerClass,"getHandle");
                    }
                    if(i.endsWith("inventory.CraftItemStack")){
                        Class craftItemStackClass = Class.forName(i);
                        CraftItemStackAsNMSCopy = getMethod(craftItemStackClass,"asNMSCopy");
                        CraftItemStackAsBukkitCopy = getMethod(craftItemStackClass,"asBukkitCopy");
                    }
                }
            for(Field field :CraftPlayerGetHandle.getReturnType().getDeclaredFields()) {
                if(field.getType().getSimpleName().equals("PlayerConnection")){
                    ConnectionClass=field.getType();
                    ServerPlayerConnection = field;
                }
            }
            for(Field field:ConnectionClass.getDeclaredFields()){
                if(field.getType().getSimpleName().equals("NetworkManager")) {
                    NetworkManagerClass=field.getType();
                    ConnectionNetworkManager = field;
                }
            }
            for(Field field:NetworkManagerClass.getDeclaredFields()){
                if(field.getType().getSimpleName().equals("Channel")) {
                    NetworkManagerChannel = field;
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
    }

    static Class getMinecraftClass(String name){
        for(Class i:minecraftClasses){
            try {
                if(i.getSimpleName().equals(name)){
                    return i;
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
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> dirs = loader.getResources(path.replace(".", "/"));
            if(!dirs.hasMoreElements())return new String[0];
            URL url = dirs.nextElement();
            if(url.getProtocol().equals("jar")) {
                JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
                Enumeration<JarEntry> entries = urlConnection.getJarFile().entries();
                String parentPath = path.replace(".","/");
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if(name.endsWith(".class")) {
                        if(name.startsWith(parentPath)) {
                            classes.add(
                                    path + name.substring(path.length(),name.length()-6).replace("\\", ".").replace("/", ".")
                            );
                        }
                    }
                }
            }else if(url.getProtocol().equals("file")){
                File file = new File(url.getFile());
                Path parentPath = file.toPath();
                List<File> subFiles = listFiles(file);
                for(File i:subFiles){
                    Path p = i.toPath();
                    String pathStr = parentPath.relativize(p).toString();
                    if(pathStr.endsWith(".class")){
                        pathStr = pathStr.substring(0,pathStr.length()-6);
                        classes.add(
                                path+"."+pathStr.replace("\\",".").replace("/",".")
                        );
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



}
