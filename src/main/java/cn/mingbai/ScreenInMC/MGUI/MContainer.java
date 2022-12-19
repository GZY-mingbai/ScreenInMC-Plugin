package cn.mingbai.ScreenInMC.MGUI;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;

public class MContainer extends MControl {
    BufferedImage image;
    private final Screen screen;
    private Graphics2D graphics;
    private FontRenderContext frc;
    private boolean canClick = true;
    private long clickTime = 0;
    public static final int minClickInterval = 100;

    public MContainer(Screen screen) {
        this.screen = screen;
        this.setWidth(screen.getWidth() * 128);
        this.setHeight(screen.getHeight() * 128);
        createImage();
    }
    public void load(){
        this.loaded=true;
        onLoad();
    }
    public void unload(){
        onUnload();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(renderThread!=null){
            renderThread.cancel();
        }
        renderThread = new BukkitRunnable() {
            private boolean cancelRender = false;
            @Override
            public synchronized void cancel() throws IllegalStateException {
                cancelRender=true;
                super.cancel();
            }
            @Override
            public void run() {
                while (!cancelRender){
                    long startTime = System.currentTimeMillis();
                    render();
                    for(Runnable i:getAllRenderTasks()){
                        i.run();
                    }
                    long sleepTime = 50 - System.currentTimeMillis() + startTime;
                    if(sleepTime>0){
                        try {
                            Thread.sleep(sleepTime);
                        }catch (Exception e){
                            return;
                        }
                    }
                }
            }
        };
        renderThread.runTaskAsynchronously(Main.thisPlugin());
    }
    public void crash(){
        reRenderCount = 10;
        reRender();
    }
    private Object renderLock = new Object();

    public Object getRenderLock() {
        return renderLock;
    }

    private synchronized void render(){
        if (rerender) {
            if (graphics == null) {
                return;
            }
            if (reRenderCount == 10) {
                reRenderCount++;
                for (MControl i : getChildControls()) {
                    removeChildControl(i);
                }
                graphics.setClip(0, 0, image.getWidth(), image.getHeight());
                graphics.setPaint(new Color(0x0078d7));
                graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
                graphics.setFont(new Font(graphics.getFont().getName(), Font.BOLD, 32));
                graphics.setPaint(new Color(0xffffff));
                LineMetrics metrics = graphics.getFont().getLineMetrics(crashedText, frc);
                graphics.drawString(crashedText, 0, metrics.getAscent());
                screen.sendView(ImageUtils.imageToMapColors(image));
                if(renderThread!=null){
                    renderThread.cancel();
                }
                return;
            }
            if (reRenderCount > 10) {
                return;
            }
            try {
                graphics.setPaint(new Color(0, 0, 0, 0));
                graphics.setStroke(new BasicStroke(1));
                graphics.setClip(0, 0, image.getWidth(), image.getHeight());
                graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
                MRenderer renderer = new MRenderer(this, graphics, frc);
                renderer.setControl(this);
                onRender(renderer);
                if (reRenderCount < 10) {
                    reRenderCount = 0;
                }
            } catch (Exception e) {
                reRenderCount++;
                e.printStackTrace();
                createImage();
                reRender();
            }
            screen.sendView(ImageUtils.imageToMapColors(image));
            synchronized (renderLock){
                renderLock.notifyAll();
            }
            rerender=false;
        }
    }
    @Override
    public void onUnload(){
        super.onUnload();
        if(renderThread!=null){
            renderThread.cancel();
        }
    }
    private BukkitRunnable renderThread;

    private void createImage() {
        if (graphics != null) {
            try {
                graphics.dispose();
            } catch (Exception e) {
            }
        }
        image = new BufferedImage((int) this.getWidth(), (int) this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        graphics = (Graphics2D) image.getGraphics();
        frc = graphics.getFontRenderContext();
    }

    public Screen getScreen() {
        return screen;
    }

    private short reRenderCount = 0;
    private static final String crashedText = ":((( Crashed / ScreenInMC 2.0";
    private boolean rerender = false;
    @Override
    public void reRender() {
        rerender = true;
    }

    public void clickAt(int x, int y, ClickType type) {
        try {
            if (canClick && (System.currentTimeMillis() - clickTime) >= minClickInterval) {
                canClick = false;
                clickTime = System.currentTimeMillis();
                for (MControl i : getAllChildMControls()) {
                    double left = i.getAbsoluteLeft();
                    double top = i.getAbsoluteTop();
                    if (i.isVisible() && left < x && (left + i.getWidth()) > x && top < y && (top + i.getHeight()) > y) {
                        i.onClick((int) (x - left), (int) (y - top), type);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        canClick = true;
    }
}
