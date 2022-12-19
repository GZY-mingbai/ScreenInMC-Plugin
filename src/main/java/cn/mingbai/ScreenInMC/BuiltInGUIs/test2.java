package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class test2 extends Core {
    BufferedImage image;
    @Override
    public void onCreate() {
        byte[] c = new byte[screen.getWidth()* screen.getHeight()*128*128];
        try {
            image = ImageIO.read(new URL("https://i1.hdslb.com/bfs/face/b272b58f61c7d848f1fea48468e74625cf0a0af7.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i=0;i<c.length;i++){
            c[i]=(byte)8;
        }
        screen.sendView(c);
    }
    private void draw(int x,int y,int w,int h){
        Image newImage = image.getScaledInstance(w,h,Image.SCALE_FAST);
        byte[] c= ImageUtils.imageToMapColors(newImage);
        for(Screen i:Screen.getAllScreens()){
            if(i.getCore() instanceof test2){
                i.sendView(c,x,y,w,h);
            }
        }
    }
    private int fx;
    private int fy;
    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if(type.equals(Utils.MouseClickType.LEFT)){
            fx = x;
            fy = y;
        }else{
            if(x>fx&&y>fy){
                draw(fx,fy,x-fx,y-fy);
            }
        }

    }

    @Override
    public void onTextInput(String text) {
    }
}
