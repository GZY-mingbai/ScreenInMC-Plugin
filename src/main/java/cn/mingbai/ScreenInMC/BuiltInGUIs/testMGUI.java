package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.Controls.*;
import cn.mingbai.ScreenInMC.MGUI.MContainer;

import java.awt.*;
import java.io.BufferedInputStream;
import java.net.URL;

import static java.awt.Font.PLAIN;

public class testMGUI extends MGUICore {
    //    int i = 0;
    Thread thread;
    long time;
    @Override
    public void onCreate(MContainer container) {
        container.setBackground(new Color(255,255,255));
        MTextBlock mTextBlock = new MTextBlock1("114514 FPS");
        mTextBlock.setTextHorizontalAlignment(Alignment.HorizontalAlignment.Left);
        mTextBlock.setTextVerticalAlignment(Alignment.VerticalAlignment.Top);
        mTextBlock.setLeft(0);
        mTextBlock.setTop(0);
        mTextBlock.setHeight(128);
        mTextBlock.setWidth(256);
        mTextBlock.setClipToBounds(false);
        mTextBlock.setFont(new Font("微软雅黑", PLAIN, 64));
        time = System.currentTimeMillis();
        mTextBlock.addRenderTask(new Runnable() {
            @Override
            public void run() {
                thread = new Thread(()->{
                    try {
                        synchronized (container.getRenderLock()) {
                            container.getRenderLock().wait();
                        }
                        long newTime = System.currentTimeMillis();
                        mTextBlock.setText((1f/(((float)(newTime-time))/1000f))+" FPS");
                        time = System.currentTimeMillis();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        });
        try{
            MGIFImage gif = new MGIFImage(new BufferedInputStream(new URL("http://tva3.sinaimg.cn/large/ceeb653ejw1fb0pt1djukg208106444n.gif").openStream()));
            gif.setHeight(256);
            gif.setWidth(256);
            gif.setScaleMode(Image.SCALE_FAST);
            gif.setHorizontalAlignment(Alignment.HorizontalAlignment.Right);
            gif.setVerticalAlignment(Alignment.VerticalAlignment.Top);
            getContainer().addChildControl(gif);

        }catch (Exception e){
            e.printStackTrace();
        }
        getContainer().addChildControl(mTextBlock);

    }

    @Override
    public void onTextInput(String text) {

    }

    public class MTextBlock1 extends MTextBlock{
        public MTextBlock1(String text){
            super(text);
        }
        @Override
        public void onUnload() {
            super.onUnload();
            thread.interrupt();
        }
    }
}
