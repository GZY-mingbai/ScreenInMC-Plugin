package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.ClickType;
import cn.mingbai.ScreenInMC.MGUI.MRenderer;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;

import java.awt.*;

public class MButton extends MTextBlock {
    private boolean pressed = false;
    private ImmediatelyCancellableBukkitRunnable pressedRunnable;
    private boolean disabled = false;

    public MButton() {
        this("");
    }

    public MButton(String text) {
        super(text);
        this.setBackground(new Color(100, 100, 100, 255));
        this.setRoundHeight(ROUND_AUTO);
        this.setRoundWidth(ROUND_AUTO);
        this.setForeground(new Color(255, 255, 255));
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        reRender();
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void onClick(int x, int y, ClickType type) {
        super.onClick(x, y, type);
        if(disabled) return;
        if (type.equals(ClickType.Left)) {
            pressed = true;
            reRender();
            if (pressedRunnable != null) {
                pressedRunnable.cancel();
            }
            pressedRunnable = new ImmediatelyCancellableBukkitRunnable() {
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
        if(disabled){
            mRenderer.setPaint(new Color(0, 0, 0, 80));
            mRenderer.drawRoundRect(0, 0, (int) getWidth(), (int) getHeight(), (int) getHeight(), (int) getHeight(), true);
        }
        if (pressed) {
            mRenderer.setPaint(new Color(0, 0, 0, 20));
            mRenderer.drawRoundRect(0, 0, (int) getWidth(), (int) getHeight(), (int) getHeight(), (int) getHeight(), true);
        }
        mRenderer.setPaint(new Color(0, 0, 0, 40));
        mRenderer.setStroke(new BasicStroke(2));
        mRenderer.drawRoundRect(2, 2, (int) getWidth() - 4, (int) getHeight() - 4, (int) getHeight() - 4, (int) getHeight() - 4, false);

    }
}
