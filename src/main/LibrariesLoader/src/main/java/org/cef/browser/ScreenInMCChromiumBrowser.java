package org.cef.browser;

import org.cef.CefClient;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class ScreenInMCChromiumBrowser extends CefBrowserOsr {
    private byte[] imageData = new byte[0];
    private int imageWidth = 0;
    private int imageHeight = 0;
    public boolean openedDevTools = false;
    public Object devToolsLock = new Object();
    private ScreenInMCChromiumBrowser devTools=null;
    public void setDevTools(boolean open){
        synchronized (devToolsLock) {
            this.openedDevTools=open;
            if (open && devTools == null) {
                devTools = (ScreenInMCChromiumBrowser) this.getDevTools();
                devTools.createImmediately();
                Rectangle rectangle = getSize();
                devTools.setSize((int) rectangle.getWidth(), (int) rectangle.getHeight());
            }
        }
    }

    public byte[] getImageData() {
        synchronized (devToolsLock) {
            if(openedDevTools) return devTools.imageData;
            else return imageData;
        }
    }
    public int[] getImageSize(){
        synchronized (devToolsLock) {
            if(openedDevTools) return devTools.getImageSize();
            else return new int[]{imageWidth, imageHeight};

        }
    }
    @Override
    protected ScreenInMCChromiumBrowser createDevToolsBrowser(CefClient client, String url, CefRequestContext context, CefBrowser_N parent, Point inspectAt) {
        return new ScreenInMCChromiumBrowser(client,url,false,context,parent,inspectAt);
    }

    public boolean isInDevTools(){
        return openedDevTools;
    }
    public ScreenInMCChromiumBrowser(CefClient client, String url, boolean transparent) {
        super(client, url, transparent, null);
    }
    private ScreenInMCChromiumBrowser(CefClient client, String url, boolean transparent,CefRequestContext context,CefBrowser_N parent, Point inspectAt){
        super(client,url,transparent,context);
        try {
            Field field = CefBrowser_N.class.getDeclaredField("parent_");
            field.setAccessible(true);
            field.set(this,parent);
            field = CefBrowser_N.class.getDeclaredField("inspectAt_");
            field.setAccessible(true);
            field.set(this,inspectAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {
//        super.onPaint(browser, popup, dirtyRects, buffer, width, height);
        synchronized (this) {
            imageData = new byte[buffer.remaining()];
            buffer.get(imageData);
            imageWidth = width;
            imageHeight = height;
        }
    }

    public void clickAt(int x, int y, int type) {
        synchronized (devToolsLock) {
            if(openedDevTools) {
                devTools.clickAt(x,y,type);
                return;
            }
        }
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
        synchronized (devToolsLock) {
            if(openedDevTools) {
                devTools.inputText(text);
                return;
            }
        }
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
    public boolean isTransparent(){
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField("isTransparent_");
            field.setAccessible(true);
            boolean value = (boolean) field.get(this);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Rectangle getSize(){
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField("browser_rect_");
            field.setAccessible(true);
            Rectangle rect = (Rectangle) field.get(this);
            return rect;
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    @Override
    public void close(boolean force) {
        synchronized (devToolsLock) {
            if(openedDevTools) {
                devTools.close(force);
            }
        }
        super.close(force);
    }
}
