package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Cores.AWTCore;
import cn.mingbai.ScreenInMC.Cores.BaseCore;
import cn.mingbai.ScreenInMC.Screen.Screen;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.nio.charset.Charset;
import java.util.Random;

public class test extends BaseCore {

    private byte color = 0;
    static String getRandomString(int i)
    {

        // bind the length
        byte[] bytearray = new byte[256];
        String mystring;
        StringBuffer thebuffer;
        String theAlphaNumericS;

        new Random().nextBytes(bytearray);

        mystring
                = new String(bytearray, Charset.forName("UTF-8"));

        thebuffer = new StringBuffer();

        //remove all spacial char
        theAlphaNumericS
                = mystring
                .replaceAll("[^A-Z0-9]", "");

        //random selection
        for (int m = 0; m < theAlphaNumericS.length(); m++) {

            if (Character.isLetter(theAlphaNumericS.charAt(m))
                    && (i > 0)
                    || Character.isDigit(theAlphaNumericS.charAt(m))
                    && (i > 0)) {

                thebuffer.append(theAlphaNumericS.charAt(m));
                i--;
            }
        }

        // the resulting string
        return thebuffer.toString();
    }
    int d = 0;
    public test() {
        super();
        setOnRender((Screen screen) -> {
            d++;
            if(d%10!=0){
                return null;
            }
            d=0;
            byte[] colors = new byte[screen.getHeight()*screen.getWidth()*128*128];
            for(int i=0;i<colors.length;i++){
                colors[i]=color;
            }
            BufferedImage image = new BufferedImage(screen.getWidth()*128,screen.getHeight()*128,BufferedImage.TYPE_USHORT_GRAY);
            Graphics graphics = image.getGraphics();
            String str = String.valueOf(color);
            char[] text = (str).toCharArray();
            Font font = new Font("微软雅黑",Font.PLAIN,128);
            graphics.setFont(font);
            graphics.drawChars(text,0,text.length,0,128);
//            int h = graphics.getFontMetrics().getHeight();
//            for(int i=0;i<20;i++) {
//                text = getRandomString(50).toCharArray();
//                graphics.drawChars(text, 0, text.length, 0, 128+i*h);
//            }
            graphics.dispose();
            Raster raster = image.getData();
            int[] its = new int[screen.getHeight()*screen.getWidth()*128*128];
            raster.getPixels(0,0,screen.getWidth()*128,screen.getHeight()*128,its);
            for(int i=0;i<its.length;i++){
                if(its[i]>0.5){
                    colors[i]=1;
                }
            }
            screen.sendView(colors);
            color++;
            return null;
        });
    }
}
