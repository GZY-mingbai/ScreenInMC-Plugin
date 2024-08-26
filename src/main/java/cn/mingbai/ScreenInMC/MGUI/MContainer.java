package cn.mingbai.ScreenInMC.MGUI;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MContainer extends MControl {
    private static final String crashedText = ":((( Crashed / ScreenInMC 2.0";
    public static final String[] defaultFonts = {"PingFang SC", "Hiragino Sans GB", "Heiti SC", "Microsoft YaHei","微软雅黑", "WenQuanYi Micro Hei", "SimSun"};
    private final Screen screen;
    protected MControl activeControl;
    BufferedImage image;
    List<Rectangle2D.Double> reRenderRectangles = Collections.synchronizedList(new ArrayList<>());
    private Graphics2D graphics;
    private FontRenderContext frc;
    private short reRenderCount = 0;
    private Object renderLock = new Object();
    private Object renderLock2 = new Object();
    private ImmediatelyCancellableBukkitRunnable renderThread;
    private boolean rerender = false;
    private Object rerenderLock = new Object();

    public MContainer(Screen screen) {
        this.screen = screen;
        this.setWidth(screen.getWidth() * 128);
        this.setHeight(screen.getHeight() * 128);
        createImage();
    }

    public void load() {
        this.loaded = true;
        onLoad();
        addReRender(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
    }

    public void unload() {
        onUnload();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (renderThread != null) {
            renderThread.cancel();
        }
        renderThread = new ImmediatelyCancellableBukkitRunnable() {
            @Override
            public void run() {
                while (!isCancelled()) {
                    long startTime = System.currentTimeMillis();
                    render();
                    for (Runnable i : getAllRenderTasks()) {
                        i.run();
                    }
                    long sleepTime = 50 - System.currentTimeMillis() + startTime;
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (Exception e) {
                            return;
                        }
                    }
                }
            }
        };
        renderThread.runTaskAsynchronously(Main.thisPlugin());
    }

    public void crash() {
        reRenderCount = 10;
        reRender();
    }

    public Object getRenderLock() {
        return renderLock;
    }

    private synchronized void render() {
        boolean shouldRerender = false;
        synchronized (rerenderLock){
            shouldRerender = rerender;
        }
        if (shouldRerender) {

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
                if (renderThread != null) {
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
                MRenderer renderer = new MRenderer(graphics, frc);
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
            List<Rectangle2D.Double> nowReRenderRectangles = new ArrayList<>();
            synchronized (renderLock2) {
                for (Rectangle2D.Double i : reRenderRectangles) {
                    nowReRenderRectangles.add(new Rectangle2D.Double(i.x,i.y,i.width,i.height));
                }
            }
            for (int i = 0; i < nowReRenderRectangles.size(); i++) {
                int x = (int) nowReRenderRectangles.get(i).x;
                int y = (int) nowReRenderRectangles.get(i).y;
                int w = (int) nowReRenderRectangles.get(i).width;
                int h = (int) nowReRenderRectangles.get(i).height;
                if (x < 0) {
                    x = 0;
                }
                if (y < 0) {
                    y = 0;
                }
                if (w <= 0 || h <= 0) {
                    continue;
                }
                try {
                    if (loaded) {
                        if (x == 0 && y == 0 && w == (int) getWidth() && h == (int) getHeight()) {
                            screen.sendView(ImageUtils.imageToMapColors(image));
                        } else {
                            if(x+w>image.getWidth()){
                                x=0;
                                w= image.getWidth();
                            }
                            if(y+h>image.getHeight()){
                                y=0;
                                h= image.getHeight();
                            }
                            screen.sendView(ImageUtils.imageToMapColors(image.getSubimage(x, y, w, h)), x, y, w, h);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reRenderRectangles.clear();
            synchronized (renderLock) {
                renderLock.notifyAll();
            }
            synchronized (rerenderLock) {
                rerender = false;
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        loaded = false;
        if (renderThread != null) {
            renderThread.cancel();
        }
    }

    private void createImage() {
        if (graphics != null) {
            try {
                graphics.dispose();
            } catch (Exception e) {
            }
        }
        image = new BufferedImage((int) this.getWidth(), (int) this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        frc = graphics.getFontRenderContext();
        setDefaultFont(graphics);
    }
    protected static void setDefaultFont(Graphics2D graphics) {

        String defaultFontSetting = Main.getConfiguration().getString("default-font");
        if(defaultFontSetting!=null&&defaultFontSetting.length()!=0){
            graphics.setFont(new Font(defaultFontSetting,0,16));
            return;
        }
        Font font = null;
        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();
        for(Font f:fonts) {
            for (String i : defaultFonts) {
                if (f.getFontName().equals(i)) {
                    font = new Font(f.getFontName(),f.getStyle(),16);
                }
            }
        }
        if (font == null) {
            font = graphics.getFont();
            if(font!=null){
                font = new Font(font.getFontName(), font.getStyle(), 16);
            }
        }
        if (font != null) {
            graphics.setFont(font);
        }
    }

    @Override
    public void onTextInput(String text) {
        super.onTextInput(text);
        if (activeControl != null) {
            activeControl.onTextInput(text);
        }
    }

    public Screen getScreen() {
        return screen;
    }

    @Override
    public void reRender() {
        synchronized (rerenderLock) {
            rerender = true;
        }
    }
    public void reRenderAll(){
        this.addReRender(new Rectangle2D.Double(0,0,this.getWidth(),this.getHeight()));
        this.reRender();
    }

    public void addReRender(Rectangle2D.Double rect) {
        synchronized (renderLock2) {
            for (Rectangle2D.Double i : reRenderRectangles) {
                if (i.equals(rect)) {
                    return;
                }
            }
            if (rect.width == 0 || rect.height == 0) {
                return;
            }
            reRenderRectangles.add((Rectangle2D.Double) rect.clone());
        }
    }

    public void addReRender(Rectangle2D.Double[] rect) {
        synchronized (renderLock2) {
            for (Rectangle2D.Double i : rect) {
                if (i.width == 0 || i.height == 0) {
                    continue;
                }
                reRenderRectangles.add((Rectangle2D.Double) i.clone());
            }
        }
    }

    public void inputText(String text) {
        onTextInput(text);
    }
}
