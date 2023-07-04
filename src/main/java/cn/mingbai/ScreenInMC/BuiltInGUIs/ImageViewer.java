package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

import static cn.mingbai.ScreenInMC.Utils.Utils.getDataFromURI;

public class ImageViewer extends Core {
    BufferedImage image;

    public ImageViewer() {
        super("ImageViewer");
    }
    private final static String defaultURI = "https://i1.hdslb.com/bfs/archive/360d6633673f1b403cbbeb9d33d02161eda3486a.jpg";

    public static class ImageViewerStoredData{
        public String uri = defaultURI;
        public int scaleMode=0;
        public int scaleAlgorithm=0;
    }
    private ImageViewerStoredData data = new ImageViewerStoredData();

    public ImageViewerStoredData getData() {
        return data;
    }

    @Override
    public void onCreate() {
        try {
            LinkedTreeMap map = (LinkedTreeMap) getStoredData();
            if(map!=null){
                String uri = (String) map.get("uri");
                Object scaleMode = (Object) map.get("scaleMode");
                Object scaleAlgorithm = (Object) map.get("scaleAlgorithm");
                if(uri!=null){
                    this.data.uri = uri;
                }else{
                    this.data.uri = defaultURI;
                }
                if(scaleMode!=null){
                    this.data.scaleMode = ((Double)scaleMode).intValue();
                }else{
                    this.data.scaleMode=0;
                }
                if(scaleAlgorithm!=null){
                    this.data.scaleAlgorithm = ((Double)scaleAlgorithm).intValue();
                }else{
                    this.data.scaleAlgorithm=0;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        reRender();
    }
    @Override
    public void reRender(){
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URI uri = new URI(data.uri);
                    image = ImageUtils.byteArrayToImage(Utils.getDataFromURI(uri));
                    int algorithm = 0;
                    if(data.scaleAlgorithm==0){
                        algorithm=Image.SCALE_SMOOTH;
                    }
                    if(data.scaleAlgorithm==1){
                        algorithm=Image.SCALE_FAST;
                    }
                    if(data.scaleMode==1){
                        image = ImageUtils.imageToBufferedImage(image.getScaledInstance(getScreen().getWidth() * 128, getScreen().getHeight() * 128, algorithm));
                    }if(data.scaleMode==0){
                        double scaleFactor = Math.min((double)(getScreen().getWidth() * 128 )/ (double)image.getWidth(), (double)(getScreen().getHeight() * 128) / (double)image.getHeight());
                        int newWidth = (int)(image.getWidth() * scaleFactor);
                        int newHeight = (int)(image.getHeight() * scaleFactor);
                        image = ImageUtils.imageToBufferedImage(image.getScaledInstance(newWidth, newHeight, algorithm));
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            double screenWidth = getScreen().getWidth() * 128;
                            double screenHeight = getScreen().getHeight() * 128;
                            double imageWidth = image.getWidth();
                            double imageHeight = image.getHeight();
                            int left = (int) ((screenWidth-imageWidth)/2);
                            int top = (int) ((screenHeight-imageHeight)/2);
                            left = left<0?0:left;
                            top = top<0?0:top;
                            getScreen().clearScreen();
                            getScreen().sendView(ImageUtils.imageToMapColors(image), left, top, image.getWidth(), image.getHeight());
                        }
                    }.runTask(Main.thisPlugin());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.thisPlugin());
    }

    @Override
    public void onUnload() {
    }
    public static class ScaleModeSettingsList implements EditGUI.EditGUICoreInfo.EditGUICoreSettingsList {
        //0=居中 1=填充
        @Override
        public String[] getList() {
            String[] modes = LangUtils.getText("controller-editor-cores-image-viewer-scale-modes").split("\\|",2
            );
            return modes;
        }
    }
    public static class ScaleAlgorithmSettingsList implements EditGUI.EditGUICoreInfo.EditGUICoreSettingsList {
        //0=平滑 1=快速
        @Override
        public String[] getList() {
            String[] algorithms = LangUtils.getText("controller-editor-cores-image-viewer-scale-algorithms").split("\\|",2
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
                new LinkedHashMap<>(){
                    {
                        put("@controller-editor-cores-image-viewer-uri",String.class);
                        put("@controller-editor-cores-image-viewer-scale-mode",ScaleModeSettingsList.class);
                        put("@controller-editor-cores-image-viewer-scale-algorithm",ScaleAlgorithmSettingsList.class);
                    }
                }
        ));
    }

    @Override
    public Object getEditGUISettingValue(String name) {
        switch (name){
            case "@controller-editor-cores-image-viewer-uri":
                return data.uri;
            case "@controller-editor-cores-image-viewer-scale-mode":
                return data.scaleMode;
            case "@controller-editor-cores-image-viewer-scale-algorithm":
                return data.scaleAlgorithm;
        }
        return super.getEditGUISettingValue(name);
    }

    @Override
    public void setEditGUISettingValue(String name, Object value) {
        switch (name){
            case "@controller-editor-cores-image-viewer-uri":
                this.data.uri = (String) value;
                break;
            case "@controller-editor-cores-image-viewer-scale-mode":
                this.data.scaleMode = (int)value;
                break;
            case "@controller-editor-cores-image-viewer-scale-algorithm":
                this.data.scaleAlgorithm = (int)value;
                break;
        }
        setStoredData(data);
        reRender();
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
