package org.cef.browser;

import org.cef.CefClient;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class ScreenInMCChromiumBrowser extends CefBrowserOsr {
    public int[] imageData = new int[0];
    public int imageWidth = 0;
    public int imageHeight = 0;

    public ScreenInMCChromiumBrowser(CefClient client, String url, boolean transparent) {
        super(client, url, transparent, null);
    }

    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {
//        super.onPaint(browser, popup, dirtyRects, buffer, width, height);
        int[] image = new int[width * height];
        for (int i = 0; i < image.length; i++) {
            image[i] = buffer.get(i * 4) & 0xFF |
                    (buffer.get(i * 4 + 1) & 0xFF) << 8 |
                    (buffer.get(i * 4 + 2) & 0xFF) << 16 |
                    (buffer.get(i * 4 + 3) & 0xFF) << 24;
        }
        synchronized (this) {
            imageData = image;
            imageWidth = width;
            imageHeight = height;
        }
    }

    public void clickAt(int x, int y, int type) {
        MouseEvent mouseEvent = new MouseEvent(this.getUIComponent(),
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis() - 10,
                0,
                x,
                y,
                1,
                false,
                type);
        sendMouseEvent(mouseEvent);
        mouseEvent = new MouseEvent(this.getUIComponent(),
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                1,
                false,
                type);
        sendMouseEvent(mouseEvent);
    }

    public void inputText(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            KeyEvent keyEvent = new KeyEvent(this.getUIComponent(),
                    KeyEvent.KEY_TYPED,
                    System.currentTimeMillis() - (chars.length * 10 - i * 10),
                    i,
                    KeyEvent.VK_UNDEFINED,
                    chars[i]);
            sendKeyEvent(keyEvent);
        }
    }

    public void setSize(int width, int height) {
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField("browser_rect_");
            field.setAccessible(true);
            Rectangle rect = (Rectangle) field.get(this);
            rect.setBounds(0, 0, width, height);
            wasResized(width, height);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
