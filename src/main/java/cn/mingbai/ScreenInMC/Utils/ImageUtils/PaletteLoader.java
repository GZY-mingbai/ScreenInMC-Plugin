package cn.mingbai.ScreenInMC.Utils.ImageUtils;

import java.awt.*;

public abstract class PaletteLoader {
    public abstract Color[] getPaletteColors();
    public abstract int[] getPaletteColorRGBs();
    public abstract PaletteLoader get();
    public static class GlobalPaletteLoader extends PaletteLoader{

        private int loadFrom = 0;
        public static final int FROM_GAME_CODE = 0;
        public static final int FROM_CONFIG = 1;
        public GlobalPaletteLoader(int from){
            loadFrom = from;
            initParentPaletteLoader();
        }
        public GlobalPaletteLoader(){
            this(FROM_GAME_CODE);
        }
        public PaletteLoader parentPaletteLoader=null;

        public void setLoadFrom(int loadFrom) {
            this.loadFrom = loadFrom;
            initParentPaletteLoader();
        }

        public int getLoadFrom() {
            return loadFrom;
        }

        private void initParentPaletteLoader(){
            switch (loadFrom){
                case FROM_GAME_CODE:
                    this.parentPaletteLoader = new GameCodePaletteLoader();
                    break;
                case FROM_CONFIG:
                    this.parentPaletteLoader = new ConfigPaletteLoader();
                    break;
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public Color[] getPaletteColors() {
            return parentPaletteLoader.getPaletteColors();
        }

        @Override
        public int[] getPaletteColorRGBs() {
            return parentPaletteLoader.getPaletteColorRGBs();
        }

        @Override
        public PaletteLoader get() {
            return this;
        }
    }
}

