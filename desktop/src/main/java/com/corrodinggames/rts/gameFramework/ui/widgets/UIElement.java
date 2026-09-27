package com.corrodinggames.rts.gameFramework.ui.widgets;

import android.graphics.PointF;
import android.graphics.RectF;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.l */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/l.class */
public abstract class UIElement {
    /* JADX INFO: renamed from: g */
    float positionX;
    /* JADX INFO: renamed from: h */
    float positionY;
    /* JADX INFO: renamed from: e */
    static final PointF tmpPoint = new PointF();
    /* JADX INFO: renamed from: f */
    static final RectF tmpRect = new RectF();
    /* JADX INFO: renamed from: A */
    static final PointF tmpAbsolutePoint = new PointF();
    /* JADX INFO: renamed from: k */
    float paddingTop;
    /* JADX INFO: renamed from: o */
    float marginTop;
    /* JADX INFO: renamed from: p */
    float marginBottom;
    /* JADX INFO: renamed from: q */
    float marginLeft;
    /* JADX INFO: renamed from: r */
    float marginRight;
    /* JADX INFO: renamed from: u */
    boolean isHovered;
    /* JADX INFO: renamed from: v */
    UIElement parent;
    /* JADX INFO: renamed from: y */
    float layoutHeight;
    /* JADX INFO: renamed from: z */
    float layoutWidth;
    /* JADX INFO: renamed from: l */
    float paddingBottom;
    /* JADX INFO: renamed from: m */
    float paddingLeft;
    /* JADX INFO: renamed from: n */
    float paddingRight;
    /* JADX INFO: renamed from: B */
    UIEventHandler eventHandler;
    /* JADX INFO: renamed from: i */
    float width = 50.0f;
    /* JADX INFO: renamed from: j */
    float height = 50.0f;
    /* JADX INFO: renamed from: s */
    boolean needsLayout = false;
    /* JADX INFO: renamed from: t */
    boolean debugBounds = false;
    /* JADX INFO: renamed from: w */
    FastArrayList<UIElement> children = new FastArrayList();
    /* JADX INFO: renamed from: x */
    LayoutDirection layoutDirection = LayoutDirection.vertical;

    /* JADX INFO: renamed from: a */
    public static void layoutHorizontalCentered(float f2, float f3, FastArrayList fastArrayList) {
        float fG = 0.0f;
        Iterator it = fastArrayList.iterator();
        while (it.hasNext()) {
            fG += ((UIElement) it.next()).getTotalWidth();
        }
        float f4 = f2 - (fG * 0.5f);
        Iterator it2 = fastArrayList.iterator();
        while (it2.hasNext()) {
            UIElement uIElement = (UIElement) it2.next();
            float f5 = f4 + uIElement.marginLeft;
            uIElement.positionX = f5;
            f4 = f5 + uIElement.width + uIElement.marginRight;
            uIElement.setCenterY(f3);
        }
    }

    /* JADX INFO: renamed from: b */
    public static void layoutVerticalCentered(float f2, float f3, FastArrayList fastArrayList) {
        float fH = 0.0f;
        Iterator it = fastArrayList.iterator();
        while (it.hasNext()) {
            fH += ((UIElement) it.next()).getTotalHeight();
        }
        float f4 = f3 - (fH * 0.5f);
        Iterator it2 = fastArrayList.iterator();
        while (it2.hasNext()) {
            UIElement uIElement = (UIElement) it2.next();
            float f5 = f4 + uIElement.marginTop;
            uIElement.positionY = f5;
            f4 = f5 + uIElement.height + uIElement.marginBottom;
            uIElement.setCenterX(f2);
        }
    }

    /* JADX INFO: renamed from: a */
    public String getTypeName() {
        return getClass().getSimpleName();
    }

    /* JADX INFO: renamed from: d */
    public GraphicsEngine getGraphicsEngine() {
        return GameEngine.getInstance().renderGraphicsEngine;
    }

    /* JADX INFO: renamed from: a */
    public RectF getRectAt(RectF rectF, float f2, float f3) {
        rectF.a = 0.0f + f2;
        rectF.b = 0.0f + f3;
        rectF.c = 0.0f + this.width + f2;
        rectF.d = 0.0f + this.height + f3;
        return rectF;
    }

    /* JADX INFO: renamed from: a */
    public RectF getAbsoluteRect(RectF rectF) {
        tmpAbsolutePoint.x = this.positionX;
        tmpAbsolutePoint.y = this.positionY;
        if (this.parent != null) {
            this.parent.applyAbsoluteOffset(tmpAbsolutePoint);
        }
        rectF.a = 0.0f + tmpAbsolutePoint.x;
        rectF.b = 0.0f + tmpAbsolutePoint.y;
        rectF.c = 0.0f + this.width + tmpAbsolutePoint.x;
        rectF.d = 0.0f + this.height + tmpAbsolutePoint.y;
        return rectF;
    }

    /* JADX INFO: renamed from: b */
    public void layout() {
        Iterator it = this.children.iterator();
        while (it.hasNext()) {
            ((UIElement) it.next()).layout();
        }
        this.layoutHeight = 0.0f;
        this.layoutWidth = 0.0f;
        if (this.layoutDirection != LayoutDirection.none) {
            if (this.layoutDirection == LayoutDirection.vertical) {
                float fG = 0.0f;
                float fH = 0.0f;
                for (UIElement uIElement : this.children) {
                    if (uIElement.width > fG) {
                        fG = uIElement.getTotalWidth();
                    }
                    fH += uIElement.getTotalHeight();
                }
                this.layoutHeight = fH;
                this.layoutWidth = fG;
                layoutVerticalCentered(this.layoutWidth * 0.5f, this.layoutHeight * 0.5f, this.children);
            } else if (this.layoutDirection == LayoutDirection.horizontal) {
                float fH2 = 0.0f;
                float fG2 = 0.0f;
                for (UIElement uIElement2 : this.children) {
                    if (uIElement2.height > fH2) {
                        fH2 = uIElement2.getTotalHeight();
                    }
                    fG2 += uIElement2.getTotalWidth();
                }
                this.layoutHeight = fH2;
                this.layoutWidth = fG2;
                layoutHorizontalCentered(this.layoutWidth * 0.5f, this.layoutHeight * 0.5f, this.children);
            } else {
                throw new RuntimeException("Unknown layout style:" + this.layoutDirection);
            }
        }
        this.needsLayout = false;
    }

    /* JADX INFO: renamed from: a */
    public void applyAbsoluteOffset(PointF pointF) {
        if (this.parent != null) {
            this.parent.applyAbsoluteOffset(pointF);
        }
        pointF.x += this.positionX;
        pointF.y += this.positionY;
    }

    /* JADX INFO: renamed from: a */
    public void addChild(UIElement uIElement) {
        uIElement.setParent(this);
    }

    /* JADX INFO: renamed from: b */
    public void setParent(UIElement uIElement) {
        setParent(uIElement, false);
    }

    /* JADX INFO: renamed from: a */
    public void setParent(UIElement uIElement, boolean z) {
        if (this.parent == uIElement) {
            return;
        }
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = uIElement;
        if (uIElement != null) {
            if (!z) {
                uIElement.children.add(this);
            } else {
                uIElement.children.add(0, this);
            }
        }
        invalidateLayout();
    }

    /* JADX INFO: renamed from: e */
    public void invalidateLayout() {
        this.needsLayout = true;
        if (this.parent != null) {
            this.parent.invalidateLayout();
        }
    }

    /* JADX INFO: renamed from: b */
    public void update(float f2) {
        if (this.children.size() > 0) {
            Iterator it = this.children.iterator();
            while (it.hasNext()) {
                ((UIElement) it.next()).update(f2);
            }
        }
    }

    /* JADX INFO: renamed from: f */
    public void render() {
        tmpAbsolutePoint.x = this.positionX;
        tmpAbsolutePoint.y = this.positionY;
        if (this.parent != null) {
            this.parent.applyAbsoluteOffset(tmpAbsolutePoint);
        }
        draw(tmpAbsolutePoint.x, tmpAbsolutePoint.y);
        if (this.children.size() > 0) {
            Iterator it = this.children.iterator();
            while (it.hasNext()) {
                ((UIElement) it.next()).render();
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void draw(float f2, float f3) {
        if (this.debugBounds) {
            UIStyle.debugStyle.draw(getGraphicsEngine(), getRectAt(new RectF(), f2, f3));
        }
    }

    /* JADX INFO: renamed from: a */
    public void setEventHandler(UIEventHandler uIEventHandler) {
        this.eventHandler = uIEventHandler;
    }

    /* JADX INFO: renamed from: a */
    public boolean handleEvent(UIEvent uIEvent) {
        if (uIEvent.isMouseClick() && containsEvent(uIEvent)) {
            GameEngine.log("UI click " + getTypeName());
            if (this.eventHandler != null) {
                return this.eventHandler.handleEvent(uIEvent);
            }
            return false;
        }
        if (uIEvent.isMouseMove()) {
            if (containsEvent(uIEvent)) {
                this.isHovered = true;
                return false;
            }
            this.isHovered = false;
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public boolean dispatchEvent(UIEvent uIEvent) {
        if (this.children.size() > 0) {
            Iterator it = this.children.iterator();
            while (it.hasNext()) {
                if (((UIElement) it.next()).dispatchEvent(uIEvent)) {
                    return true;
                }
            }
        }
        if (handleEvent(uIEvent)) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: c */
    public boolean containsEvent(UIEvent uIEvent) {
        getAbsoluteRect(tmpRect);
        return tmpRect.b(uIEvent.x, uIEvent.y);
    }

    /* JADX INFO: renamed from: c */
    public void setCenterX(float f2) {
        this.positionX = f2 - (this.width * 0.5f);
    }

    /* JADX INFO: renamed from: d */
    public void setCenterY(float f2) {
        this.positionY = f2 - (this.height * 0.5f);
    }

    /* JADX INFO: renamed from: e */
    public void setMargin(float f2) {
        this.marginTop = f2;
        this.marginBottom = f2;
        this.marginLeft = f2;
        this.marginRight = f2;
    }

    /* JADX INFO: renamed from: f */
    public void setPadding(float f2) {
        this.paddingTop = f2;
        this.paddingBottom = f2;
        this.paddingLeft = f2;
        this.paddingRight = f2;
    }

    /* JADX INFO: renamed from: g */
    public float getTotalWidth() {
        return this.marginLeft + this.width + this.marginRight;
    }

    /* JADX INFO: renamed from: h */
    public float getTotalHeight() {
        return this.marginTop + this.height + this.marginBottom;
    }

    /* JADX INFO: renamed from: i */
    public void removeFromParent() {
        setParent((UIElement) null);
    }
}
