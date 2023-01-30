package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.Utils;
import cn.mingbai.ScreenInMC.VideoProcessor;
import com.google.gson.internal.LinkedTreeMap;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class VideoPlayer extends Core {

    public VideoPlayer() {
        super("VideoPlayer");
    }
    private String path;
    private boolean loop;

    public void setPath(String path) {
        this.path = path;
        saveData();
    }

    public String getPath() {
        return path;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
        saveData();
    }

    public boolean isLoop() {
        return loop;
    }
    private void saveData(){
        LinkedTreeMap data = new LinkedTreeMap();
        data.put("path",path);
        data.put("loop",loop);
        setStoredData(data);
    }

    @Override
    public void onCreate() {
        try {
            LinkedTreeMap data = (LinkedTreeMap)getStoredData();
            if(data!=null){
                path = (String) data.get("path");
                loop = (boolean) data.get("loop");
                if (path!=null){
                    play();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private boolean isPlaying;
    private VideoProcessor.DitheredVideo video;
    private BukkitRunnable playRunnable = null;
    public void stop(){
        if(video==null){
            return;
        }
        if(isPlaying&&playRunnable!=null){
            isPlaying=false;
            playRunnable.cancel();
            while (playRunnable!=null){
                try {
                    Thread.sleep(50);
                }catch (Exception e){}
            }
        }
    }
    public void play(){
        if(video==null){
            return;
        }
        stop();
        if(!isPlaying){
            video = VideoProcessor.readDitheredVideo(path,loop);
            isPlaying = true;
            isPause=false;
            playRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    while (isPlaying && !playRunnable.isCancelled()){
                        if(isPause){
                            try {
                                Thread.sleep(50);
                                continue;
                            }catch (Exception e){
                            }
                        }
                        long start = System.currentTimeMillis();
                        byte[] data = video.readAFrame();
                        getScreen().sendView(data);
                        long wait = 50-(System.currentTimeMillis()-start);
                        if(wait>0){
                            try {
                                Thread.sleep(wait);
                            }catch (Exception e){
                            }
                        }
                    }
                    playRunnable=null;
                    isPlaying=false;
                    isPause=false;
                }
            };
            playRunnable.runTask(Main.thisPlugin());
        }
    }
    private boolean isPause = false;
    public void setPause(boolean isPause){
        this.isPause = isPause;
    }

    @Override
    public void onUnload() {
        if(playRunnable!=null){
            isPlaying = false;
            playRunnable.cancel();
        }
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {

    }

    @Override
    public void onTextInput(String text) {

    }
}
