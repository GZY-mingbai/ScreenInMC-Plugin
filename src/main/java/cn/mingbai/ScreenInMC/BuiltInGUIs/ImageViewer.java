package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.Controls.MTextBlock;
import cn.mingbai.ScreenInMC.MGUI.MImageGenerator;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.GIFUtils;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Material;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ImageViewer extends Core {
    private BufferedImage savedImage;

    public ImageViewer() {
        super("ImageViewer");
    }

    private final static String defaultURI = "screen://builtin/example.jpg";
    public BufferedImage getErrorImage(){
        MImageGenerator generator = new MImageGenerator(getScreen().getWidth()*128,getScreen().getHeight()*128,Color.WHITE);
        MTextBlock textBlock = new MTextBlock(LangUtils.getText("image-viewer-load-failed"));
        textBlock.setVerticalAlignment(Alignment.VerticalAlignment.Stretch);
        textBlock.setHorizontalAlignment(Alignment.HorizontalAlignment.Stretch);
        textBlock.setTextHorizontalAlignment(Alignment.HorizontalAlignment.Center);
        textBlock.setTextVerticalAlignment(Alignment.VerticalAlignment.Center);
        generator.addChildControl(textBlock);
        BufferedImage image = generator.getImage();
        return image;
    }
    public static class ImageViewerStoredData implements StoredData {
        public String uri = defaultURI;
        public int scaleMode = 1;
        public int scaleAlgorithm = 0;

        @Override
        public StoredData clone() {
            ImageViewerStoredData storedData = new ImageViewerStoredData();
            storedData.uri = this.uri;
            storedData.scaleMode = this.scaleMode;
            storedData.scaleAlgorithm = this.scaleAlgorithm;
            return storedData;
        }



    }

    @Override
    public void onCreate() {
        loadImageLock = new Object();
        loadImage();
        updateImage(true);
    }

    private byte[] imageData = new byte[0];
    private boolean isAnimated = false;
    private List<BufferedImage> animatedFrames;
    private int animatedDelay = 50;
    private ImmediatelyCancellableBukkitRunnable animatedRunnable = null;
    private Object loadImageLock;
    private Object preProcess = null;

    public void loadImage() {
        synchronized (loadImageLock) {
            try {
                if (animatedFrames != null) {
                    animatedFrames.clear();
                }
                animatedFrames = new ArrayList<>();
                isAnimated = false;
                animatedDelay = 50;
                ImageViewerStoredData data = (ImageViewerStoredData) getStoredData();
                if (data.uri == null || data.uri.length() == 0) {
                    return;
                }
                URI uri = new URI(data.uri);
                imageData = Utils.getDataFromURI(uri,true);
                if (uri.getRawPath().endsWith("gif")) {
                    GIFUtils.GifDecoder decoder = new GIFUtils.GifDecoder();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
                    decoder.read(inputStream);
                    if (decoder.getFrameCount() == 0) {
                        savedImage = decoder.getFrame(0);
                        inputStream.close();
                        return;
                    }
                    for (int i = 0; i < decoder.getFrameCount(); i++) {
                        animatedFrames.add(decoder.getFrame(i));
                    }
                    animatedDelay = decoder.getDelay(0);
                    if (animatedDelay < 50) {
                        animatedDelay = 50;
                    }
                    inputStream.close();
                    isAnimated = true;
                    return;
                }
                isAnimated = false;
                savedImage = ImageUtils.byteArrayToImage(imageData);
            } catch (Exception e) {
                e.printStackTrace();
                isAnimated = false;
                savedImage = getErrorImage();
            }
        }
    }

    private byte[] lastImage = null;
    private int[] lastSize = null;
    private ImmediatelyCancellableBukkitRunnable sendingRunnable = null;

    private void sendImage(byte[] data, int left, int top, int width, int height) {
        synchronized (loadImageLock) {
            if (isUnloaded()) return;
            lastImage = data;
            lastSize = new int[]{left, top, width, height};
            if (sendingRunnable == null) {
                sendingRunnable = new ImmediatelyCancellableBukkitRunnable() {
                    @Override
                    public void run() {
                        byte[] newData = lastImage;
                        int[] newSize = lastSize;
                        while (true) {
                            getScreen().sendView(newData, newSize[0], newSize[1], newSize[2], newSize[3]);
                            if (lastImage != null && lastImage != newData) {
                                newData = lastImage;
                                newSize = lastSize;
                            } else {
                                sendingRunnable = null;
                                return;
                            }
                        }
                    }
                };
                sendingRunnable.runTaskAsynchronously(Main.thisPlugin());

            }
        }
    }


    private void setAnimatedRunnableToNull() {
        synchronized (loadImageLock) {
            animatedRunnable = null;
            loadImageLock.notifyAll();
        }
    }

    public void updateImage(boolean changedSettings) {
        try {
            int toWidth = getScreen().getWidth()*128;
            int toHeight = getScreen().getHeight()*128;
            if (changedSettings) {
                cancelAndWaitAnimatedRunnable();
            }
            final ImageViewerStoredData data = (ImageViewerStoredData) getStoredData();
            synchronized (loadImageLock) {
                if (isAnimated) {
                    if (changedSettings || animatedRunnable == null) {
                        Utils.Pair<Rectangle2D.Float, byte[]>[] preData = new Utils.Pair[animatedFrames.size()];
                        for (int i = 0; i < animatedFrames.size(); i++) {
                            Utils.Pair<BufferedImage,Rectangle2D.Float> image = ImageUtils.scaleImageAndGetPosition(animatedFrames.get(i), data.scaleMode, toWidth,toHeight, data.scaleAlgorithm);
                            preData[i] = new Utils.Pair<>(image.getValue(), ImageUtils.imageToMapColors(image.getKey()));
                        }
                        preProcess = preData;
                        animatedRunnable = new ImmediatelyCancellableBukkitRunnable() {
                            @Override
                            public void run() {
                                getScreen().clearScreen();
                                int nowFrame = 0;
                                final int delay = animatedDelay;
                                long timeStart = System.currentTimeMillis();
                                while (!this.isCancelled()) {
                                    timeStart = System.currentTimeMillis();
                                    synchronized (loadImageLock) {
                                        if (nowFrame > animatedFrames.size() - 1) {
                                            nowFrame = 0;
                                        }
                                        Utils.Pair<Rectangle2D.Float, byte[]>[] pre = (Utils.Pair<Rectangle2D.Float, byte[]>[]) (preProcess);
                                        Utils.Pair<Rectangle2D.Float, byte[]> preData = new Utils.Pair<>(pre[nowFrame].getKey(), pre[nowFrame].getValue());
                                        nowFrame++;
                                        if (!this.isCancelled()) {
                                            if (!getScreen().canSleep()) {
                                                sendImage(preData.getValue(), (int) preData.getKey().x, (int) preData.getKey().y, (int) preData.getKey().width, (int) preData.getKey().height);
                                            }
                                        }
                                    }
                                    long timeToSleep = delay - (System.currentTimeMillis() - timeStart);
                                    if (timeToSleep > 0) {
                                        try {
                                            Thread.sleep(timeToSleep);
                                        } catch (Exception e) {
                                            setAnimatedRunnableToNull();
                                            return;
                                        }
                                    }
                                }
                                setAnimatedRunnableToNull();
                            }
                        };
                        animatedRunnable.runTaskAsynchronously(Main.thisPlugin());
                    }
                } else {
                    if (changedSettings || preProcess == null) {
                        Utils.Pair<Rectangle2D.Float, byte[]> preData = new Utils.Pair<Rectangle2D.Float, byte[]>(null, null);
                        Utils.Pair<BufferedImage,Rectangle2D.Float> image = ImageUtils.scaleImageAndGetPosition(savedImage, data.scaleMode, toWidth,toHeight, data.scaleAlgorithm);
                        preData.setValue(ImageUtils.imageToMapColors(image.getKey()));
                        preData.setKey(image.getValue());
                        preProcess = preData;
                    }
                    try {
                        getScreen().clearScreen();
                        Utils.Pair<Rectangle2D.Float, byte[]> pre = (Utils.Pair<Rectangle2D.Float, byte[]>) preProcess;
                        if (!getScreen().canSleep()) {
                            sendImage(pre.getValue(), (int) pre.getKey().x, (int) pre.getKey().y, (int) pre.getKey().width, (int) pre.getKey().height);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void cancelAndWaitAnimatedRunnable() {
        synchronized (loadImageLock) {
            if (animatedRunnable != null&&!animatedRunnable.isCancelled()) {
                animatedRunnable.cancel();
                while (animatedRunnable != null) {
                    try {
                        loadImageLock.wait(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void reRender() {
        new ImmediatelyCancellableBukkitRunnable() {
            @Override
            public void run() {
                updateImage(false);
            }
        }.runTaskAsynchronously(Main.thisPlugin());
    }

    @Override
    public StoredData createStoredData() {
        return new ImageViewerStoredData();
    }

    @Override
    public void onUnload() {
        cancelAndWaitAnimatedRunnable();
    }

    public static class ScaleModeSettingsList implements EditGUI.EditGUICoreInfo.EditGUICoreSettingsList {
        //0=居中 1=填充
        @Override
        public String[] getList() {
            String[] modes = LangUtils.getText("controller-editor-cores-scale-modes").split("\\|", 3
            );
            return modes;
        }
    }

    public static class ScaleAlgorithmSettingsList implements EditGUI.EditGUICoreInfo.EditGUICoreSettingsList {
        //0=平滑 1=快速
        @Override
        public String[] getList() {
            String[] algorithms = LangUtils.getText("controller-editor-cores-scale-algorithms").split("\\|", 3
            );
            return algorithms;
        }
    }

    @Override
    public void addToEditGUI() {
        EditGUI.registerCoreInfo(new EditGUI.EditGUICoreInfo(
                "@controller-editor-cores-image-viewer-name",
                this,
                "@controller-editor-cores-image-viewer-details",
                "yellow",
                Material.PAINTING,
                new LinkedHashMap() {
                    {
                        put("@controller-editor-cores-image-viewer-uri", String.class);
                        put("@controller-editor-cores-scale-mode", ScaleModeSettingsList.class);
                        put("@controller-editor-cores-scale-algorithm", ScaleAlgorithmSettingsList.class);
                    }
                }
        ));
    }

    @Override
    public Object getEditGUISettingValue(String name) {
        ImageViewerStoredData data = (ImageViewerStoredData) getStoredData();

        switch (name) {
            case "@controller-editor-cores-image-viewer-uri":
                return data.uri;
            case "@controller-editor-cores-scale-mode":
                return data.scaleMode;
            case "@controller-editor-cores-scale-algorithm":
                return data.scaleAlgorithm;
        }
        return super.getEditGUISettingValue(name);
    }

    @Override
    public void setEditGUISettingValue(String name, Object value) {
        ImageViewerStoredData data = (ImageViewerStoredData) getStoredData();
        switch (name) {
            case "@controller-editor-cores-image-viewer-uri":
                data.uri = (String) value;
                loadImage();
                break;
            case "@controller-editor-cores-scale-mode":
                data.scaleMode = (int) value;
                break;
            case "@controller-editor-cores-scale-algorithm":
                data.scaleAlgorithm = (int) value;
                break;
        }
        updateImage(true);
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
//        if (image != null) {
//            send();
//        }
    }

    @Override
    public void onTextInput(String text) {
//        if (image != null) {
//            send();
//        }
    }

}
