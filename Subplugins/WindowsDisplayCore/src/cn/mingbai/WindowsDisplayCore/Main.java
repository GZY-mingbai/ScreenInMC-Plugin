package cn.mingbai.WindowsDisplayCore;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import io.netty.channel.Channel;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import jdk.incubator.foreign.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import sun.misc.Unsafe;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//DLL From https://github.com/GG22G2/csgo-aimbot/blob/master/dll/screenshot/x64/Release/screenShot.dll
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            File file = File.createTempFile("screen-in-mc-windows-display-core", ".dll");
            InputStream stream = Main.class.getResourceAsStream("/screenshot.dll");
            ReadableByteChannel inputChannel = Channels.newChannel(stream);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
            fileChannel.close();
            fileOutputStream.close();
            inputChannel.close();
            stream.close();
            System.load(file.getAbsolutePath());
            Core.addCore(new windowsDisplayCore());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public static class windowsDisplayCore extends Core{
        private static CLinker cLinker;
        MethodHandle capture;
        MethodHandle release;
        MethodHandle init;
        ResourceScope scope;
        ImageUtils.DelayConverter converter = new ImageUtils.DelayConverter(new ImageUtils.DelayConverter.DelayOnReady() {
            @Override
            public void apply(ImageUtils.DelayConverter.DelayImage delayImage) {
                getScreen().sendView(delayImage.getData());
            }

            @Override
            public void apply(ImageUtils.DelayConverter.DelayImage delayImage, int i, int i1, int i2, int i3) {
                getScreen().sendView(delayImage.getData(),i,i1,i2,i3);
            }
        });
        public windowsDisplayCore() {
            super("WindowsDisplayCore");
        }
        private final Map<String, MethodHandle> methodHandles = new HashMap<>();
        private MethodHandle getMethodHandle(String methodName, FunctionDescriptor descriptor) throws Throwable {
            MethodHandle cachedHandle = methodHandles.get(methodName);
            if (cachedHandle != null) {
                return cachedHandle;
            }

            // trying to find method handle by loaderLookup
            Optional<NativeSymbol> directMethodAddress = SymbolLookup.loaderLookup().lookup(methodName);
            if (directMethodAddress.isPresent()) {
                MethodHandle methodHandle = cLinker.downcallHandle(directMethodAddress.get(), descriptor);
                methodHandles.put(methodName, methodHandle);
                return methodHandle;
            }

            // Some opengl functions are not in lookup table (extensions) so we need to call wglGetProcAddress to get pointer to needed function
            MethodHandle extensionMethodHandle = getMethodHandle("wglGetProcAddress", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            MemorySegment extensionMethodNameMemorySegment = SegmentAllocator.implicitAllocator().allocateUtf8String(methodName);
            MemoryAddress address = (MemoryAddress) extensionMethodHandle.invoke(extensionMethodNameMemorySegment);

            if (address.equals(MemoryAddress.NULL)) {
                throw new IllegalArgumentException("Could not find method: " + methodName);
            }


            // making call to retrieved function address
            NativeSymbol methodAddress = NativeSymbol.ofAddress(methodName, address, ResourceScope.globalScope());
            MethodHandle methodHandle = cLinker.downcallHandle(methodAddress, descriptor);
            methodHandles.put(methodName, methodHandle);
            return methodHandle;
        }
        Thread thread;
        ByteBuffer imageData;
        long imageDataAddr;
        int dataSize;
        int w,h;
        @Override
        public void onCreate() {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cLinker = CLinker.systemCLinker();
                        init = getMethodHandle(
                                "capture_init",
                                FunctionDescriptor.of(ValueLayout.JAVA_INT)
                        );

                        release = getMethodHandle(
                                "capture_release",
                                FunctionDescriptor.of(ValueLayout.JAVA_INT)
                        );

                        capture = getMethodHandle(
                                "capture",
                                FunctionDescriptor.of(ValueLayout.JAVA_LONG)
                        );
                        int success = (int) init.invokeExact();
                        if(success!=1){
                            throw new RuntimeException();
                        }
                        scope = ResourceScope.newConfinedScope();
                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        Dimension screenSize = toolkit.getScreenSize();
                        dataSize = screenSize.width * screenSize.height * 4;
                        w = screenSize.width;
                        h = screenSize.height;
                        imageData = ByteBuffer.allocateDirect(dataSize);
                        Class<?> aClass = java.nio.Buffer.class;
                        Field field = aClass.getDeclaredField("address");
                        field.setAccessible(true);
                        imageDataAddr = (long)field.get(imageData);
                        while (isLoad) {
                            converter.addImage(new ImageUtils.DelayConverter.DelayImage(screenShot(),w,h));
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }

        boolean isLoad = true;
        @Override
        public void onUnload() {
            isLoad=false;
            try{
                release.invokeExact();
            }catch (Throwable e){
                e.printStackTrace();
            }
            unsafe.freeMemory(imageDataAddr);
        }

        @Override
        public void onMouseClick(int i, int i1, Utils.MouseClickType mouseClickType) {

        }

        @Override
        public void onTextInput(String s) {

        }
        public static Unsafe createUnsafe() {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field field = unsafeClass.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                Unsafe unsafe = (Unsafe) field.get(null);
                return unsafe;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        Unsafe unsafe = createUnsafe();
        public int[] screenShot(){
            try {
                long result = (long) capture.invokeExact();
                unsafe.copyMemory(result,imageDataAddr,dataSize);
                int d = dataSize/4;
                int[] data = new int[d];
                for(int i=0;i<d;i++){
                    int dd = i*4;
                    data[i] = imageData.get(dd)<<16 | imageData.get(dd+1)<<8 | imageData.get(dd+2);
                }
//                unsafe.freeMemory(result);
                return data;
            }catch (Throwable e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
