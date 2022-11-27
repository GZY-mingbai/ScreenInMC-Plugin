package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.MControl;
import cn.mingbai.ScreenInMC.MGUI.MRenderer;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.GIFUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;


public class MGIFImage extends MControl {
    private final GIFUtils.GifDecoder gifDecoder = new GIFUtils.GifDecoder();
    private Image nowImage;
    private int nowFrame = 0;
    public synchronized void setImage(BufferedInputStream stream) {
        nowFrame = 0;
        gifDecoder.read(stream);
        registerRenderTask();
    }
    private int scaleMode = Image.SCALE_DEFAULT;
    public synchronized void setScaleMode(int scaleMode) {
        this.scaleMode = scaleMode;
    }

    public int getScaleMode() {
        return scaleMode;
    }
    public MGIFImage(){}
    public MGIFImage(BufferedInputStream stream){
        gifDecoder.read(stream);
        registerRenderTask();
    }
    private void registerRenderTask(){
        if(renderTask!=null){
            removeRenderTask(renderTask);
        }
        renderTask=new Runnable() {
            @Override
            public synchronized void run() {
                if(renderTask!=this){
                    return;
                }
                if(gifDecoder.getFrameCount()>0){
                    BufferedImage image = gifDecoder.getFrame(nowFrame);
                    if(getWidth()<=0||getHeight()<=0||(image.getWidth()==(int)getWidth()&&image.getHeight()==(int)getHeight())){
                        nowImage = image;
                    }else{
                        nowImage = image.getScaledInstance((int)getWidth(),(int)getHeight(),scaleMode);
                    }
                    reRender();
                    nowFrame++;
                    if(nowFrame>=gifDecoder.getFrameCount()){
                        nowFrame=0;
                    }
                }else{
                    removeRenderTask(renderTask);
                }
            }
        };
        addRenderTask(renderTask);
    }

    @Override
    public void onRender(MRenderer mRenderer) {
        renderBackground(mRenderer);
        mRenderer.drawImage(nowImage,0,0);
        renderChildren(mRenderer);
    }


    private Runnable renderTask;

}
