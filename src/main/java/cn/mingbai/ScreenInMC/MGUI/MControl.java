package cn.mingbai.ScreenInMC.MGUI;

import org.bukkit.Bukkit;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MControl {
    private MControl parentMControl;
    private double left = 0;
    private double top = 0;
    private double width = 0;
    private double height = 0;
    private boolean visible = true;
    private Paint background = new Color(0, 0, 0, 0);
    private Paint borderPaint = new Color(0, 0, 0, 0);
    private Stroke borderStroke = new BasicStroke(2);
    private Alignment.HorizontalAlignment horizontalAlignment = Alignment.HorizontalAlignment.None;
    private Alignment.VerticalAlignment verticalAlignment = Alignment.VerticalAlignment.None;
    private final List<MControl> childMControls = Collections.synchronizedList(new ArrayList<>());

    public Paint getBorderPaint() {
        return borderPaint;
    }

    public synchronized void setBorderPaint(Paint borderPaint) {
        this.borderPaint = borderPaint;
        reRender();
    }

    public List<MControl> getAllChildMControls() {
        List<MControl> result = new ArrayList<>();
        List<MControl> children = getChildControls();
        result.addAll(children);
        for (MControl i : children) {
            result.addAll(i.getAllChildMControls());
        }
        return result;
    }

    public Stroke getBorderStroke() {
        return borderStroke;
    }

    public synchronized void setBorderStroke(Stroke borderStroke) {
        this.borderStroke = borderStroke;
        reRender();
    }

    public Paint getBackground() {
        return background;
    }

    public synchronized void setBackground(Paint background) {
        this.background = background;
        reRender();
    }

    public synchronized void onClick(int x, int y, ClickType type) {
        MContainer container = getMContainer();
        if(container!=null){
            if(container.activeControl==this){
                return;
            }
            if(container.activeControl!=null){
                container.activeControl.active=false;
                container.activeControl.onPassive();
            }
            container.activeControl = this;
            active=true;
            onActive();
        }
    }
    private boolean active;
    public void onActive(){
    }
    public void onPassive(){
    }

    public Alignment.HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public synchronized void setHorizontalAlignment(Alignment.HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        onResize();
    }

    public Alignment.VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public synchronized void setVerticalAlignment(Alignment.VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        onResize();
    }
    public void onResize() {
        onResize(true);
    }

    public boolean isActive() {
        return active;
    }

    private void addNowSize(){
        MContainer container = getMContainer();
        if(container!=null){
            container.addReRender(new Rectangle2D.Double(getAbsoluteLeft(),getAbsoluteTop(),getWidth(),getHeight()));
        }
    }

    private void onResize(boolean render) {
        double parentWidth;
        if (parentMControl == null) {
            parentWidth = 0;
        } else {
            parentWidth = parentMControl.getWidth();
        }
        switch (horizontalAlignment) {
            case Left:
                addNowSize();
                this.left = 0;
                break;
            case Right:
                addNowSize();
                this.left = parentWidth - getWidth();
                break;
            case Center:
                addNowSize();
                this.left = (parentWidth - getWidth()) / 2;
                break;
            case Stretch:
                addNowSize();
                this.left = 0;
                this.width = parentWidth;
                break;
        }
        double parentHeight;
        if (parentMControl == null) {
            parentHeight = 0;
        } else {
            parentHeight = parentMControl.getHeight();
        }
        switch (verticalAlignment) {
            case Top:
                addNowSize();
                this.top = 0;
                break;
            case Bottom:
                addNowSize();
                this.top = parentHeight - getHeight();
                break;
            case Center:
                addNowSize();
                this.top = (parentHeight - getHeight()) / 2;
                break;
            case Stretch:
                addNowSize();
                this.top = 0;
                this.height = parentHeight;
                break;
        }
        for(MControl i:getChildControls()){
            if(i.getVerticalAlignment()!= Alignment.VerticalAlignment.None ||
                    i.getHorizontalAlignment()!= Alignment.HorizontalAlignment.None){
                i.onResize(false);
            }
        }
        if(render){
            reRender();
        }
    }

    public double getWidth() {
        return width;
    }

    public synchronized void setWidth(double width) {
        if (horizontalAlignment != Alignment.HorizontalAlignment.Stretch) {
            addNowSize();
            this.width = width;
            onResize();
        } else {
            throw new RuntimeException("HorizontalAlignment can't be Stretch");
        }
    }

    public double getHeight() {
        return height;
    }

    public synchronized void setHeight(double height) {
        if (verticalAlignment != Alignment.VerticalAlignment.Stretch) {
            addNowSize();
            this.height = height;
            onResize();
        } else {
            throw new RuntimeException("VerticalAlignment can't be Stretch");
        }
    }

    public double getTop() {
        return top;
    }

    public synchronized void setTop(double top) {
        if (verticalAlignment == Alignment.VerticalAlignment.None) {
            addNowSize();
            this.top = top;
            onResize();
        } else {
            throw new RuntimeException("VerticalAlignment must be None");
        }
    }

    public MControl getParentControl() {
        return parentMControl;
    }

    public double getLeft() {
        return left;
    }

    public synchronized void setLeft(double left) {
        if (horizontalAlignment == Alignment.HorizontalAlignment.None) {
            addNowSize();
            this.left = left;
            onResize();
        } else {
            throw new RuntimeException("HorizontalAlignment must be None");
        }
    }

    public double getAbsoluteLeft() {
        double result = getLeft();
        if (parentMControl != null) {
            result += parentMControl.getAbsoluteLeft();
        } else {
            return result;
        }
        return result;
    }

    public double getAbsoluteTop() {
        double result = getTop();
        if (parentMControl != null) {
            result += parentMControl.getAbsoluteTop();
        } else {
            return result;
        }
        return result;
    }

    public List<MControl> getChildControls() {
        List<MControl> newList = new ArrayList<>();
        for (MControl i : childMControls) {
            newList.add(i);
        }
        return newList;
    }

    public boolean isVisible() {
        return visible;
    }

    public synchronized void setVisible(boolean visible) {
        this.visible = visible;
        reRender();
    }

    public boolean isVisibleActually() {
        if (parentMControl != null) {
            if (parentMControl.visible) {
                return parentMControl.isVisibleActually();
            } else {
                return false;
            }
        } else {
            return visible;
        }
    }

    public void addChildControl(MControl mControl) {
        if (mControl.parentMControl == null && !mControl.loaded) {
            childMControls.add(mControl);
            mControl.parentMControl = this;
            mControl.loaded=true;
            try {
                mControl.onLoad();
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("This control have had parent.");
        }
    }

    public synchronized void removeChildControl(MControl mControl) {
        for (int i = 0; i < childMControls.size(); i++) {
            MControl child = childMControls.get(i);
            if (mControl == child) {
                child.parentMControl = null;
                childMControls.get(i).loaded=false;
                try {
                    childMControls.get(i).onUnload();
                }catch (Exception e){
                    e.printStackTrace();
                }
                childMControls.remove(i);
                i--;
            }
        }
        reRender();
    }

    public synchronized void removeChildControlAt(int i) {
        if (i >= childMControls.size()) {
            throw new RuntimeException("Wrong index.");
        }
        removeChildControl(childMControls.get(i));
    }

    public void onRender(MRenderer mRenderer) {
        renderBackground(mRenderer);
        renderChildren(mRenderer);
    }
    protected synchronized void renderBackground(MRenderer mRenderer) {
        if (roundWidth == 0 && roundHeight == 0) {
            mRenderer.setPaint(background);
            mRenderer.drawRect(0, 0, (int) width, (int) height, true);
            mRenderer.setPaint(borderPaint);
            mRenderer.setStroke(borderStroke);
            mRenderer.drawRect(0, 0, (int) width, (int) height, false);
        }else{
            int rw = roundWidth;
            int rh = roundHeight;
            if(rh==ROUND_AUTO||rw==ROUND_AUTO){
                rh = (int) height;
                rw = (int) height;
            }
            mRenderer.setPaint(background);
            mRenderer.drawRoundRect(0, 0, (int) width, (int) height, rw,rh,true);
            mRenderer.setPaint(borderPaint);
            mRenderer.setStroke(borderStroke);
            mRenderer.drawRoundRect(0, 0, (int) width, (int) height, rw,rh, false);
        }
    }
    private int roundWidth = 0;
    private int roundHeight = 0;

    public int getRoundHeight() {
        return roundHeight;
    }

    public int getRoundWidth() {
        return roundWidth;
    }

    public synchronized void setRoundHeight(int roundHeight) {
        this.roundHeight = roundHeight;
        reRender();
    }
    public static final int ROUND_AUTO = -1;

    public synchronized void setRoundWidth(int roundWidth) {
        this.roundWidth = roundWidth;
        reRender();
    }

    private List<Runnable> renderTasks = Collections.synchronizedList(new ArrayList<>());

    public List<Runnable> getRenderTasks() {
        List<Runnable> result = new ArrayList<>();
        for (Runnable i : renderTasks) {
            result.add(i);
        }
        return result;
    }
    public synchronized void removeRenderTask(Runnable runnable){
        for (int i = 0; i < renderTasks.size(); i++) {
            if(runnable==renderTasks.get(i)){
                renderTasks.remove(i);
                i--;
            }
        }
    }
    public synchronized void removeRenderTaskAt(int i){
        if (i >= renderTasks.size()) {
            throw new RuntimeException("Wrong index.");
        }
        removeRenderTask(renderTasks.get(i));
    }
    public synchronized List<Runnable> getAllRenderTasks() {
        List<Runnable> result = new ArrayList<>();
        result.addAll(getRenderTasks());
        List<MControl> children = getAllChildMControls();
        for (MControl i : children) {
            result.addAll(i.getRenderTasks());
        }
        return result;
    }
    public synchronized void addRenderTask(Runnable runnable){
        renderTasks.add(runnable);
    }
    protected synchronized void renderChildren(MRenderer mRenderer){
        for (MControl i : childMControls) {
            if (i.visible) {
                MRenderer newMRenderer = ((MRenderer) mRenderer.clone());
                newMRenderer.setControl(i);
                i.onRender(newMRenderer);
            }
        }
    }
    protected boolean loaded=false;

    public boolean isLoaded() {
        return loaded;
    }

    public void onLoad(){
        onResize();
    }
    public void onUnload(){

    }
    public void onTextInput(String text){}
    public MContainer getMContainer(){
        if(this instanceof MContainer){
            return (MContainer) this;
        }else{
            if(parentMControl==null){
                return null;
            }
            return parentMControl.getMContainer();
        }
    }
    public void reRender() {
        MContainer container = getMContainer();
        if(container!=null) {
            container.addReRender(new Rectangle2D.Double(getAbsoluteLeft(), getAbsoluteTop(), getWidth(), getHeight()));
            container.reRender();
        }
    }
}
