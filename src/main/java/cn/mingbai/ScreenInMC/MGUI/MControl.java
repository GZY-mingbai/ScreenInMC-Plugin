package cn.mingbai.ScreenInMC.MGUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MControl {
    private MControl parentMControl;
    private double left = 0;
    private double top = 0;
    private double width = 0;
    private double height = 0;
    private boolean visible = true;
    private boolean clipToBounds = true;
    private Paint background = new Color(0, 0, 0, 0);
    private Paint borderPaint = new Color(0, 0, 0, 0);
    private Stroke borderStroke = new BasicStroke(2);
    private Alignment.HorizontalAlignment horizontalAlignment = Alignment.HorizontalAlignment.None;
    private Alignment.VerticalAlignment verticalAlignment = Alignment.VerticalAlignment.None;
    private final List<MControl> childMControls = Collections.synchronizedList(new ArrayList<>());

    public Paint getBorderPaint() {
        return borderPaint;
    }

    public void setBorderPaint(Paint borderPaint) {
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

    public void setBorderStroke(Stroke borderStroke) {
        this.borderStroke = borderStroke;
        reRender();
    }

    public Paint getBackground() {
        return background;
    }

    public void setBackground(Paint background) {
        this.background = background;
        reRender();
    }

    public void onClick(int x, int y, ClickType type) {
    }

    public Alignment.HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(Alignment.HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        onResize();
    }

    public Alignment.VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(Alignment.VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        onResize();
    }

    public void onResize() {
        double parentWidth;
        if (parentMControl == null) {
            parentWidth = 0;
        } else {
            parentWidth = parentMControl.getWidth();
        }
        switch (horizontalAlignment) {
            case Left:
                this.left = 0;
                break;
            case Right:
                this.left = parentWidth - getWidth();
                break;
            case Center:
                this.left = (parentWidth - getWidth()) / 2;
                break;
            case Stretch:
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
                this.top = 0;
                break;
            case Bottom:
                this.top = parentHeight - getHeight();
                break;
            case Center:
                this.top = (parentHeight - getHeight()) / 2;
                break;
            case Stretch:
                this.top = 0;
                this.height = parentHeight;
                break;
        }
        reRender();
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (horizontalAlignment != Alignment.HorizontalAlignment.Stretch) {
            this.width = width;
            onResize();
        } else {
            throw new RuntimeException("HorizontalAlignment can't be Stretch");
        }
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (verticalAlignment != Alignment.VerticalAlignment.Stretch) {
            this.height = height;
            onResize();
        } else {
            throw new RuntimeException("VerticalAlignment can't be Stretch");
        }
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        if (verticalAlignment == Alignment.VerticalAlignment.None) {
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

    public void setLeft(double left) {
        if (horizontalAlignment == Alignment.HorizontalAlignment.None) {
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

    public void setVisible(boolean visible) {
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

    public boolean isClipToBounds() {
        return clipToBounds;
    }

    public void setClipToBounds(boolean clipToBounds) {
        this.clipToBounds = clipToBounds;
        reRender();
    }

    public void addChildControl(MControl mControl) {
        if (mControl.parentMControl == null) {
            childMControls.add(mControl);
            mControl.parentMControl = this;
            mControl.onResize();
        } else {
            throw new RuntimeException("This control have had parent.");
        }
    }

    public void removeChildControl(MControl MControl) {
        for (int i = 0; i < childMControls.size(); i++) {
            MControl child = childMControls.get(i);
            if (MControl == child) {
                child.parentMControl = null;
                childMControls.remove(i);
                i--;
            }
        }
        reRender();
    }

    public void removeChildControlAt(int i) {
        if (i >= childMControls.size()) {
            throw new RuntimeException("Wrong index.");
        }
        removeChildControl(childMControls.get(i));
    }

    public void onRender(MRenderer MRenderer) {
        MRenderer.setPaint(background);
        MRenderer.drawRect(0, 0, (int) width, (int) height, true);
        MRenderer.setPaint(borderPaint);
        MRenderer.setStroke(borderStroke);
        MRenderer.drawRect(0, 0, (int) width, (int) height, false);
        for (MControl i : childMControls) {
            if (i.visible) {
                MRenderer newMRenderer = ((MRenderer) MRenderer.clone());
                newMRenderer.setControl(i);
                i.onRender(newMRenderer);
            }
        }
    }

    public void reRender() {
        if (parentMControl != null) {
            parentMControl.reRender();
        }
    }
}
