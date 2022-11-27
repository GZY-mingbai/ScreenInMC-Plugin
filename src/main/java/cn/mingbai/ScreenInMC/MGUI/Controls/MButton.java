package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.ClickType;
import cn.mingbai.ScreenInMC.MGUI.MRenderer;
import cn.mingbai.ScreenInMC.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;

import static cn.mingbai.ScreenInMC.MGUI.MContainer.minClickInterval;

public class MButton extends MTextBlock {
    private boolean pressed = false;
    private BukkitRunnable pressedRunnable;
    private final Paint pressedPaint = new Color(0, 0, 0, 20);

    public MButton() {
        super();
        this.setBackground(new Color(255, 255, 255, 255));
    }

    public MButton(String text) {
        super(text);
        this.setBackground(new Color(255, 255, 255, 255));
    }

    @Override
    public void onClick(int x, int y, ClickType type) {
        super.onClick(x, y, type);

        if (type.equals(ClickType.Left)) {
            pressed = true;
            reRender();
            if (pressedRunnable != null) {
                pressedRunnable.cancel();
            }
            pressedRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(minClickInterval);
                        pressed = false;
                        reRender();
                    } catch (Exception e) {
                    }
                }
            };
            pressedRunnable.runTaskAsynchronously(Main.thisPlugin());
        }
    }

    @Override
    public void onRender(MRenderer mRenderer) {
        super.onRender(mRenderer);
        if (pressed) {
            mRenderer.setPaint(pressedPaint);
            mRenderer.drawRect(0, 0, (int) getWidth(), (int) getHeight(), true);
        }
    }
}
