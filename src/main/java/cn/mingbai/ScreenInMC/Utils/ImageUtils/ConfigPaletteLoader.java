package cn.mingbai.ScreenInMC.Utils.ImageUtils;

import cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils;
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
    private String readConfig(String configName) throws Exception {
        InputStream stream = ConfigPaletteLoader.class.getResourceAsStream("/palettes/palette_"+configName+".txt");
        return new String(IOUtils.readInputStream(stream), StandardCharsets.UTF_8);
    }
    private List<String> loadConfig(String configName){
        String paletteString;
        try {
            paletteString = readConfig(configName);
        }catch (Exception e){
            return new ArrayList<>();
        }
        String[] colorsString = paletteString.replace("\r","").split("\n");
        List<String> newPalette = new ArrayList<>();
        for(String i:colorsString){
            if(i.startsWith("#")){
                String configId = i.substring(1);
                newPalette.addAll(loadConfig(configId));
                continue;
            }
            newPalette.add(i);
        }
        return newPalette;
    }
    private void setConfigName(String configName){
        this.configName = configName;
        try {
            List<String> colorsString = loadConfig(configName);
            List<Color> colorsList = new ArrayList<>();
            List<Integer> colorRGBsList = new ArrayList<>();
            for(String i : colorsString){
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
        if(CraftUtils.minecraftVersion>=17){
            setConfigName("1.17");
            return;
        }
        if(CraftUtils.minecraftVersion>=16){
            setConfigName("1.16");
            return;
        }
        if(CraftUtils.minecraftVersion>=12){
            setConfigName("1.12");
            return;
        }
        if(CraftUtils.minecraftVersion>=8){
            if(CraftUtils.minecraftVersion==8){
                if(CraftUtils.subMinecraftVersion>=1){
                    setConfigName("1.8.1");
                }else{
                    setConfigName("1.8");
                }
            }else{
                setConfigName("1.8.1");
            }
        }
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
