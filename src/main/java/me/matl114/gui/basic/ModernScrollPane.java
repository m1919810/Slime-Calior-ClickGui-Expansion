/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.util.Util
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.client.gui.screen.Screen
 */
package me.matl114.gui.basic;

import me.matl114.gui.elements.AdvancedScrollElement;
import me.matl114.versioned.api.VDrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class ModernScrollPane extends ScrollableListWidget {
    private static final double WHEEL_STEP = 26.0;
    private double targetPercentage = -1.0;
    private long lastFrame;
    private boolean nestedScrollHandling;

    public ModernScrollPane(int x, int y, int dx, int dy) {
        super(x, y, dx, dy);
        InputHandler inputHandler;
        if (this.scoll != null && (inputHandler = this.scoll.getHandler()) instanceof AdvancedScrollElement) {
            AdvancedScrollElement bar = (AdvancedScrollElement) inputHandler;
            bar.setBackGround(false);
        }
    }

    private int scrollRange() {
        return Math.max(0, this.maxHeight - this.height());
    }

    public double getScrollPercentage() {
        return this.targetPercentage >= 0.0 ? this.targetPercentage : this.percentage;
    }

    public void setScrollPercentage(double value) {
        this.percentage = MathHelper.clamp((double) value, (double) 0.0, (double) 1.0);
        this.targetPercentage = -1.0;
    }

    public void setNestedScrollHandling(boolean nestedScrollHandling) {
        this.nestedScrollHandling = nestedScrollHandling;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        boolean overPane = this.isOver(mouseX, mouseY);
        boolean overScrollbar = this.scoll != null && this.scoll.isOver(mouseX, mouseY);
        if (!overPane && !overScrollbar) {
            return false;
        }
        if (this.nestedScrollHandling && overPane) {
            double translatedMouseX = mouseX - this.x();
            double translatedMouseY = mouseY - this.y() + this.getCurrentPose();
            for (DrawableWidget widget : this.widgets) {
                widget.onMouseScrolled(translatedMouseX, translatedMouseY, horizontalAmount, verticalAmount);
            }
            return true;
        }
        int range = this.scrollRange();
        if (range <= 0) {
            return false;
        }
        double from = this.getScrollPercentage();
        this.targetPercentage =
                MathHelper.clamp((double) (from - verticalAmount * 26.0 / (double) range), (double) 0.0, (double) 1.0);
        this.lastFrame = Util.getMeasuringTimeMs();
        return true;
    }

    @Override
    public boolean startDrag(Screen screen, double mouseX, double mouseY) {
        boolean result = super.startDrag(screen, mouseX, mouseY);
        if (result && this.draggingElement == this.scoll) {
            this.targetPercentage = -1.0;
        }
        return result;
    }

    @Override
    public void render0(VDrawContext context, int mouseX, int mouseY, float delta, boolean disableSelect) {
        if (this.targetPercentage >= 0.0) {
            long now = Util.getMeasuringTimeMs();
            double step = Math.min(1.0, (double) (now - this.lastFrame) / 70.0);
            this.lastFrame = now;
            this.percentage += (this.targetPercentage - this.percentage) * step;
            if (Math.abs(this.targetPercentage - this.percentage) * (double) Math.max(1, this.scrollRange()) < 0.5) {
                this.percentage = this.targetPercentage;
                this.targetPercentage = -1.0;
            }
        }
        super.render0(context, mouseX, mouseY, delta, disableSelect);
    }
}
