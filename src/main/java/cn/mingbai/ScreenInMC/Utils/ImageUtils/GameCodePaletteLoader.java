package cn.mingbai.ScreenInMC.Utils.ImageUtils;

import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameCodePaletteLoader extends PaletteLoader {
    public GameCodePaletteLoader(){
    }
    private Color[] colors;
    private int[] colorRGBs;
    protected void setColors(Color[] colors,int[] colorRGBs){
        this.colors = colors;
        this.colorRGBs = colorRGBs;
    }
    @Override
    public Color[] getPaletteColors() {
        return colors;
    }

    @Override
    public int[] getPaletteColorRGBs() {
        return colorRGBs;
    }

    @Override
    public PaletteLoader get() {
        String version = Bukkit.getBukkitVersion();
        if(version.startsWith("1.19")){
            return new GameCodePaletteLoader_1_19();
        } else if (version.startsWith("1.18")) {

        }
        return this;
    }

    public static class GameCodePaletteLoader_1_19 extends GameCodePaletteLoader{
        public GameCodePaletteLoader_1_19(){
            List<Color> colors = new ArrayList<>();
            List<Integer> colorRGBs = new ArrayList<>();
            for (int i = 1; i < net.minecraft.world.level.material.MaterialColor.MATERIAL_COLORS.length - 1; i++) {
                net.minecraft.world.level.material.MaterialColor materialColor = net.minecraft.world.level.material.MaterialColor.byId(i);
                if (materialColor == null || materialColor.equals(net.minecraft.world.level.material.MaterialColor.NONE)) {
                    break;
                }
                for (int b = 0; b < 4; b++) {
                    Color color = new Color(materialColor.calculateRGBColor(net.minecraft.world.level.material.MaterialColor.Brightness.byId(b)));
                    int cr = color.getRed();
                    int cg = color.getGreen();
                    int cb = color.getBlue();
                    int ca = color.getAlpha();
                    color = new Color(cb, cg, cr, ca);
                    colors.add(color);
                    colorRGBs.add(color.getRGB());
                }
            }
            setColors(colors.toArray(new Color[0]),Utils.toPrimitive(colorRGBs.toArray(new Integer[0])));
        }
    }
}
