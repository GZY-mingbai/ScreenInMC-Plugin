package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.Utils;
import cn.mingbai.ScreenInMC.VideoProcessor;
import org.bukkit.scheduler.BukkitRunnable;

public class VideoPlayer extends Core {

    private boolean isPlaying;
    private VideoProcessor.DitheredVideo video;
    private ImmediatelyCancellableBukkitRunnable playRunnable = null;
    private boolean isPause = false;

    public VideoPlayer() {
        super("VideoPlayer");
    }

    public String getPath() {
        VideoPlayerStoredData data = ((VideoPlayerStoredData)getStoredData());
        return data.path;
    }

    public void setPath(String path) {
        VideoPlayerStoredData data = ((VideoPlayerStoredData)getStoredData());
        data.path = path;
    }

    public boolean isLoop() {
        VideoPlayerStoredData data = ((VideoPlayerStoredData)getStoredData());
        return data.loop;
    }

    public void setLoop(boolean loop) {
        VideoPlayerStoredData data = ((VideoPlayerStoredData)getStoredData());
        data.loop = loop;
    }
    public static class VideoPlayerStoredData implements StoredData{
        public String path=null;
        public boolean loop=false;

        @Override
        public StoredData clone() {
            return null;
        }


    }


    @Override
    public StoredData createStoredData() {
        return null;
    }

    @Override
    public void onCreate() {
        try {
            VideoPlayerStoredData data = ((VideoPlayerStoredData)getStoredData());
            if (data != null) {
                if (data.path != null) {
                    play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (video == null) {
            return;
        }
        if (isPlaying && playRunnable != null) {
            isPlaying = false;
            playRunnable.cancel();
            while (playRunnable != null) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }
        }
    }

    public void play() {
        if (video == null) {
            return;
        }
        stop();
        if (!isPlaying) {
            VideoPlayerStoredData data = ((VideoPlayerStoredData)getStoredData());
            video = VideoProcessor.readDitheredVideo(data.path, data.loop);
            isPlaying = true;
            isPause = false;
            playRunnable = new ImmediatelyCancellableBukkitRunnable() {
                @Override
                public void run() {
                    while (isPlaying && !playRunnable.isCancelled()) {
                        if (isPause) {
                            try {
                                Thread.sleep(50);
                                continue;
                            } catch (Exception e) {
                            }
                        }
                        long start = System.currentTimeMillis();
                        byte[] data = video.readAFrame();
                        getScreen().sendView(data);
                        long wait = 50 - (System.currentTimeMillis() - start);
                        if (wait > 0) {
                            try {
                                Thread.sleep(wait);
                            } catch (Exception e) {
                            }
                        }
                    }
                    playRunnable = null;
                    isPlaying = false;
                    isPause = false;
                }
            };
            playRunnable.runTask(Main.thisPlugin());
        }
    }

    public void setPause(boolean isPause) {
        this.isPause = isPause;
    }

    @Override
    public void onUnload() {
        if (playRunnable != null) {
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
