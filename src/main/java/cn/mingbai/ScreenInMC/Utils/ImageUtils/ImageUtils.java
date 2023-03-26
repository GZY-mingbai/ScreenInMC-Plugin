package cn.mingbai.ScreenInMC.Utils.ImageUtils;

import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.IOUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ImageUtils {
    private static int pieceSize = 4;
    private static Color[] paletteColors;
    private static int[] paletteRGBs;
    private static DitheringProcessor processor = new DitheringProcessor.JavaDitheringProcessor();
    private static String[] platforms;

    public static int[] getPalette() {
        return paletteRGBs;
    }

    public static String[] getOpenCLPlatforms() {
        return platforms.clone();
    }

    public static int getBestOpenCLDevice(){
        int device = -1;
        String[] plats = getOpenCLPlatforms();
        String[] suggestions = new String[]{"openclon", "nvidia", "intel(r)cpu", "intel(r)opencl"};
        boolean found = false;
        for (int i = 0; i < suggestions.length; i++) {
            for (int j = 0; j < plats.length; j++) {
                String plat = plats[j].replace(" ", "").toLowerCase();
                if (plat.startsWith(suggestions[i])) {
                    device = j;
                    found = true;
                }
            }
            if (found) {
                break;
            }
        }
        if (!found && plats.length > 0) {
            device = 0;
        }
        if (plats.length == 0) {
            device = -1;
        }
        return device;
    }

    public static int getPieceSize() {
        return pieceSize;
    }

    public static void setPieceSize(int pieceSize) {
        ImageUtils.pieceSize = pieceSize;
    }
    private static String customOpenCLCode = "code_fast.cl"; // code_fast.cl

    public static void setCustomOpenCLCode(String customOpenCLCode) {
        ImageUtils.customOpenCLCode = customOpenCLCode;
    }

    public static String getOpenCLCode(){
        String loadCode = "code.cl";
        if(pieceSize==1){
            loadCode = "code_1x1.cl";
        }
        if(customOpenCLCode.length()!=0){
            loadCode=customOpenCLCode;
        }
        InputStream stream = ImageUtils.class.getResourceAsStream("/lib/"+loadCode);
        try {
            String codeStr = new String(IOUtils.readInputStream(stream), StandardCharsets.UTF_8);
            return codeStr;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void initImageUtils(PaletteLoader paletteLoader,DitheringProcessor processor) {
        try {
            ImageUtils.processor = processor;
            platforms = GPUDither.getPlatforms();
            paletteColors = paletteLoader.getPaletteColors();
            paletteRGBs = paletteLoader.getPaletteColorRGBs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] imageToMapColors(Image image){
        return imageToMapColors(processor,image,pieceSize);
    }
    public static byte[] imageToMapColors(DitheringProcessor processor,Image image, int pieceSize){
        return processor.processImage(image,paletteColors,pieceSize);
    }
    public static byte[] imageToMapColors(int[] data, int width, int height){
        return imageToMapColors(processor,data,width,height,pieceSize);
    }
    public static byte[] imageToMapColors(DitheringProcessor processor,int[] data, int width, int height,int pieceSize){
        return processor.processData(data,width,height,paletteColors,pieceSize);
    }

    public static DitheringProcessor getDitheringProcessor() {
        return ImageUtils.processor;
    }

    public static void setDitheringProcessor(DitheringProcessor processor) {
        ImageUtils.processor = processor;
    }

    public static BufferedImage imageToBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();
        return bufferedImage;
    }
}
