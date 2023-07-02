package cn.mingbai.ScreenInMC.Utils.ImageUtils;

import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;



public abstract class DitheringProcessor {
    public abstract byte[] processData(int[] data, int width, int height,Color[] paletteColors,int pieceSize);
    public byte[] processImage(Image image,Color[] paletteColors,int pieceSize){
        BufferedImage img = ImageUtils.imageToBufferedImage(image);
        int height = img.getHeight();
        int width = img.getWidth();
        int[] data = img.getRGB(0, 0, width, height, null, 0, width);
        return processData(data, width, height,paletteColors,pieceSize);
    }
    public static class JavaDitheringProcessor extends DitheringProcessor{
        public static int colorDistance(Color c1, Color c2) {
            int rmean = (c1.getBlue() + c2.getBlue()) / 2;
            int r = c1.getRed() - c2.getRed();
            int g = c1.getGreen() - c2.getGreen();
            int b = c1.getBlue() - c2.getBlue();
            return (int) Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
        }
        public int colorClip(int i) {
            if (i > 255) {
                return 255;
            }
            if (i < 0) {
                return 0;
            }
            return i;
        }

        public int rgbToInt(int r, int g, int b) {
            return 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);
        }
        public Utils.Pair<Integer, Color> getClosestMatch(Color color,Color[] paletteColors) {
            int minimum_index = 0;
            int minimum_difference = colorDistance(color, paletteColors[0]);
            for (int i = 1; i < paletteColors.length; i++) {
                int current_difference = colorDistance(color, paletteColors[i]);
                if (current_difference < minimum_difference) {
                    minimum_difference = current_difference;
                    minimum_index = i;
                }
            }

            return new Utils.Pair<>(minimum_index, paletteColors[minimum_index]);
        }
        @Override
        public byte[] processData(int[] data, int width, int height,Color[] paletteColors,int pieceSize) {
            byte[] result = new byte[height * width];
            int i = 0;
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    Color current_color = new Color(data[i], true);
                    if (current_color.getAlpha() != 255) {
                        data[i] = 0;
                        i++;
                        continue;
                    }
                    Utils.Pair<Integer, Color> closest_match = getClosestMatch(current_color,paletteColors);
                    Color co = closest_match.getValue();
                    int errorR = current_color.getRed() - co.getRed();
                    int errorG = current_color.getGreen() - co.getGreen();
                    int errorB = current_color.getBlue() - co.getBlue();
                    data[i] = co.getRGB();
                    int key = closest_match.getKey() + 4;
                    result[i] = (byte) ((key / 4) << 2 | (key % 4) & 3);
                    if (!(x == width - 1)) {
                        int t = i + 1;
                        Color c = new Color(data[t], true);
                        if (c.getAlpha() == 255) {
                            data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.4375f)),
                                    colorClip((int) (c.getGreen() + errorG * 0.4375f)),
                                    colorClip((int) (c.getBlue() + errorB * 0.4375f)));
                        }
                        if (!(y == height - 1)) {
                            t = i + 1 + width;
                            c = new Color(data[t], true);
                            if (c.getAlpha() == 255) {
                                data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.0625f)),
                                        colorClip((int) (c.getGreen() + errorG * 0.0625f)),
                                        colorClip((int) (c.getBlue() + errorB * 0.0625f)));
                            }
                        }
                    }
                    if (!(y == height - 1)) {
                        int t = i + width;
                        Color c = new Color(data[t], true);
                        if (c.getAlpha() == 255) {
                            data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.1875f)),
                                    colorClip((int) (c.getGreen() + errorG * 0.1875f)),
                                    colorClip((int) (c.getBlue() + errorB * 0.1875f)));
                        }
                        if (!(x == 0)) {
                            t = i - 1 + width;
                            c = new Color(data[t], true);
                            if (c.getAlpha() == 255) {
                                data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.3125f)),
                                        colorClip((int) (c.getGreen() + errorG * 0.3125f)),
                                        colorClip((int) (c.getBlue() + errorB * 0.3125f)));
                            }
                        }
                    }
                    i++;
                }
            }
            return result;
        }

    }
    public static class JavaFastDitheringProcessor extends JavaDitheringProcessor{
        private Color[] nowPaletteColors=null;
        private byte[][][] processedPaletteColors=null;
        private short colorsCount;
        public JavaFastDitheringProcessor() {
            this((short) 40);
        }
        public JavaFastDitheringProcessor(short colorsCount){
            if(colorsCount>256||colorsCount<4){
                throw new RuntimeException("ColorsCount must be from 4 to 256.");
            }
            this.colorsCount = colorsCount;
        }
        public static byte[][][] processPaletteColors(Color[] paletteColors,short colorsCount){
            byte[][][] processedPaletteColors = new byte[colorsCount][colorsCount][colorsCount];
            for(byte r=0;r<colorsCount;r++){
                for(byte g=0;g<colorsCount;g++){
                    for(byte b=0;b<colorsCount;b++){
                        Color nowColor = new Color(r*256/colorsCount,g*256/colorsCount,b*256/colorsCount);
                        int closestColor = 0;
                        int closestDistance = -1;
                        for(int i=0;i<paletteColors.length;i++){
                            if(paletteColors[i].getAlpha()==255){
                                int distance = colorDistance(paletteColors[i],nowColor);
                                if(closestDistance==-1||distance<=closestDistance){
                                    closestColor = i;
                                    closestDistance = distance;
                                }
                            }
                        }
                        processedPaletteColors[r][g][b]= (byte) closestColor;
                    }
                }
            }
            return processedPaletteColors;
        }
        @Override
        public Utils.Pair<Integer, Color> getClosestMatch(Color color, Color[] paletteColors) {
            if(paletteColors!=nowPaletteColors){
                nowPaletteColors = paletteColors;
                processedPaletteColors = processPaletteColors(paletteColors,colorsCount);
            }
            if(processedPaletteColors==null){
                return new Utils.Pair<>(0,new Color(0,0,0,0));
            }
            int index = processedPaletteColors
                    [color.getRed()*colorsCount/256]
                    [color.getGreen()*colorsCount/256]
                    [color.getBlue()*colorsCount/256];
            if(index<0){
                index+=256;
            }

            return new Utils.Pair<>(index,paletteColors[index]);
        }
    }
    public static class OpenCLDitheringProcessor extends DitheringProcessor{
        @Override
        public byte[] processData(int[] data, int width, int height, Color[] paletteColors,int pieceSize) {
            return GPUDither.dither(data,width,height,pieceSize);
        }
    }
}
