package cn.mingbai.ScreenInMC.Utils.ImageUtils;

import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.IOUtils;
import cn.mingbai.ScreenInMC.Utils.LanczosFilter;
import cn.mingbai.ScreenInMC.Utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.DitheringProcessor.JavaFastDitheringProcessor.processPaletteColors;

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
    public static BufferedImage byteArrayToImage(byte[] array) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        BufferedImage bufferedImage = ImageIO.read(bis);
        bis.close();
        return bufferedImage;
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
    private static short openCLFastColorsCount = 40;

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
            if(customOpenCLCode.equals("code_fast.cl")){
                byte[][][] colors = processPaletteColors(paletteColors, openCLFastColorsCount);
                String result1 = "";
                String result2 = "";
                int halfLength = openCLFastColorsCount*openCLFastColorsCount*openCLFastColorsCount/2;
                if(halfLength>32000){
                    throw new RuntimeException("Too many colors.(>40)");
                }
                for(int b=0;b<openCLFastColorsCount;b++){
                    for(int g=0;g<openCLFastColorsCount;g++) {
                        for (int r = 0; r < openCLFastColorsCount; r++) {
                            if(b*openCLFastColorsCount*openCLFastColorsCount+g*openCLFastColorsCount+r>=
                                    halfLength){
                                result2+=colors[r][g][b]+4+",";
                            }else{
                                result1+=colors[r][g][b]+4+",";
                            }
                        }
                    }
                }
                result1 = result1.substring(0,result1.length()-1);
                result2 = result2.substring(0,result2.length()-1);
                codeStr = codeStr.replace("##LIST1##",result1);
                codeStr = codeStr.replace("##LIST2##",result2);
                codeStr = codeStr.replace("##ColorsCount##",String.valueOf(openCLFastColorsCount));
                codeStr = codeStr.replace("##listLength##",String.valueOf(halfLength));

            }
            return codeStr;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void initImageUtils(PaletteLoader paletteLoader,DitheringProcessor processor) {
        try {
            ImageUtils.processor = processor;
            paletteColors = paletteLoader.getPaletteColors();
            paletteRGBs = paletteLoader.getPaletteColorRGBs();
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e){
            e.printStackTrace();
        }catch (Throwable e){
            e.printStackTrace();
        }

        try {
            platforms = GPUDither.getPlatforms();
        }catch (Exception e) {
            throw new RuntimeException();
        }catch (Error e){
            throw new RuntimeException();
        }catch (Throwable e){
            throw new RuntimeException();
        }
    }
    public static final String OpenCLLoadErrorMessage = "初始化OpenCL加速失败，将禁用OpenCL加速功能";
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
    public static BufferedImage scaleImage(BufferedImage image,int toWidth,int toHeight,int scaleAlgorithm){
        switch (scaleAlgorithm){
            case 0:
                return imageToBufferedImage(image.getScaledInstance(toWidth,toHeight,Image.SCALE_SMOOTH));
            case 1:
                return imageToBufferedImage(image.getScaledInstance(toWidth,toHeight,Image.SCALE_FAST));
            case 2:
                return LanczosFilter.scale(image,((float) toWidth)/((float) image.getWidth(null)),((float) toHeight)/((float) image.getHeight(null)));
            default:
                throw new RuntimeException("Unknown algorithm: "+scaleAlgorithm);
        }
    }
    public static byte[] getSubimageForMapColors(byte[] image,int imageWidth,int imageHeight,int startX,int startY, int toWidth, int toHeight){
        byte[] newImage = new byte[toWidth*toHeight];
        for(int y=0;y<toHeight;y++){
            for(int x=0;x<toWidth;x++){
                newImage[y*toWidth+x] = image[(startY+y)*imageWidth+startX+x];
            }
        }
        return newImage;
    }
    public static byte[] scaleImageForMapColors(byte[] image,int imageWidth,int imageHeight,int toWidth,int toHeight){
        byte[] output = new byte[toWidth * toHeight];

        double widthRatio = ((double) imageWidth) / ((double) toWidth);
        double heightRatio = ((double) imageHeight) / ((double) toHeight);
        for (int y = 0; y < toHeight; y++) {
            for (int x = 0; x < toWidth; x++) {
                int px = (int) (((double)x) * widthRatio);
                int py = (int) (((double)y) * heightRatio);
                output[y * toWidth + x] = image[py * imageWidth + px];
            }
        }
        return output;
    }
    public static Utils.Pair<byte[],Rectangle2D.Float> scaleMapColorsAndGetPosition(byte[] image, int scaleMode,int imageWidth,int imageHeight, int toWidth, int toHeight){
        if(scaleMode==0) {
            boolean subImage = false;
            int newImageWidth = imageWidth;
            int newImageHeight = imageHeight;
            if (imageWidth > toWidth) {
                newImageWidth = toWidth;
                subImage = true;
            }
            if (imageHeight > toHeight) {
                newImageHeight = toHeight;
                subImage = true;
            }
            if (subImage) {
                image = getSubimageForMapColors(image,imageWidth,imageHeight,0, 0, newImageWidth, newImageHeight);
            }
            return new Utils.Pair<>(image,new Rectangle2D.Float(0,0,newImageWidth,newImageHeight));
        }
        if(scaleMode==1){
            double imageScaling = ((double) imageWidth)/((double) imageHeight);
            double scaling = ((double) toWidth)/((double) toHeight);
            if(imageScaling>scaling){
                int newHeight = (int)Math.floor(toWidth/imageScaling);
                image = scaleImageForMapColors(image,imageWidth,imageHeight,toWidth,newHeight);
                int top = (toHeight-newHeight)/2;
                return new Utils.Pair<>(image,new Rectangle2D.Float(0, top, toWidth, newHeight));
            }else {
                int newWidth = (int)Math.floor(toHeight*imageScaling);
                image = scaleImageForMapColors(image,imageWidth,imageHeight,newWidth,toHeight);
                int left = (toWidth-newWidth)/2;
                return new Utils.Pair<>(image,new Rectangle2D.Float(left, 0,newWidth, toHeight));
            }
        }
        if(scaleMode==2){
            image = scaleImageForMapColors(image,imageWidth,imageHeight,toWidth,toHeight);
            return new Utils.Pair<>(image,new Rectangle2D.Float(0,0,toWidth,toHeight));
        }
        throw new RuntimeException("Unknown scale mode: "+scaleMode);
    }

    public static Utils.Pair<BufferedImage,Rectangle2D.Float> scaleImageAndGetPosition(BufferedImage image, int scaleMode, int toWidth, int toHeight, int scaleAlgorithm){
        int w = image.getWidth();
        int h = image.getHeight();
        if(scaleMode==0) {
            boolean subImage = false;
            if (w > toWidth) {
                w = toWidth;
                subImage = true;
            }

            if (h > toHeight) {
                h = toHeight;
                subImage = true;
            }
            if (subImage) {
                image = image.getSubimage(0, 0, w, h);
            }
            return new Utils.Pair<>(image,new Rectangle2D.Float(0,0,w,h));
        }
        if(scaleMode==1){
            double imageScaling = ((double) w)/((double) h);
            double scaling = ((double) toWidth)/((double) toHeight);
            if(imageScaling>scaling){
                image = ImageUtils.imageToBufferedImage(scaleImage(image,toWidth,(int)Math.floor(toWidth/imageScaling),scaleAlgorithm));
                int top = (toHeight-image.getHeight())/2;
                return new Utils.Pair<>(image,new Rectangle2D.Float(0, top, image.getWidth(), image.getHeight()));
            }else {
                image = ImageUtils.imageToBufferedImage(scaleImage(image,(int)Math.floor(toHeight*imageScaling),toHeight,scaleAlgorithm));
                int left = (toWidth-image.getWidth())/2;
                return new Utils.Pair<>(image,new Rectangle2D.Float(left, 0, image.getWidth(), image.getHeight()));
            }
        }
        if(scaleMode==2){
            image = ImageUtils.imageToBufferedImage(scaleImage(image,toWidth,toHeight,scaleAlgorithm));
            return new Utils.Pair<>(image,new Rectangle2D.Float(0,0,image.getWidth(),image.getHeight()));
        }
        throw new RuntimeException("Unknown scale mode: "+scaleMode);
    }
}
