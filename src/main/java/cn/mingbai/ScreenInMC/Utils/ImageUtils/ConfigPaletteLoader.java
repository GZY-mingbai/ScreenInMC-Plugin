package cn.mingbai.ScreenInMC.Utils.ImageUtils;

import cn.mingbai.ScreenInMC.Utils.IOUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Bukkit;

import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigPaletteLoader extends PaletteLoader{
    Color[] colors;
    int[] colorRGBs;
    @Override
    public Color[] getPaletteColors() {
        return colors;
    }

    @Override
    public int[] getPaletteColorRGBs() {
        return colorRGBs;
    }

    private String configName = "";
    private void setConfigName(String configName){
        this.configName = configName;
        InputStream stream = ConfigPaletteLoader.class.getResourceAsStream("/palettes/palette_"+configName+".txt");
        try {
            String paletteString = new String(IOUtils.readInputStream(stream), StandardCharsets.UTF_8);
            String[] cololrsString = paletteString.replace("\r","").split("\n");
            List<Color> colorsList = new ArrayList<>();
            List<Integer> colorRGBsList = new ArrayList<>();
            for(String i : cololrsString){
                String[] RGBString = i.split(",");
                Color color = new Color(Integer.parseInt(RGBString[0]),Integer.parseInt(RGBString[1]),Integer.parseInt(RGBString[2]));
                colorsList.add(color);
                colorRGBsList.add(color.getRGB());
            }
            this.colors = colorsList.toArray(new Color[0]);
            this.colorRGBs = Utils.toPrimitive(colorRGBsList.toArray(new Integer[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ConfigPaletteLoader(String configName){
        setConfigName(configName);
    }
    public ConfigPaletteLoader(){
        this("1.17");
    }

    @Override
    public PaletteLoader get() {
        if(configName.length()==0){
            String[] versions = Bukkit.getVersion().split("\\.");
            this.configName = versions[0]+"."+versions[1];
        }
        return this;
    }
}
