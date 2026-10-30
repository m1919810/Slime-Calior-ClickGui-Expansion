package me.matl114.gui.elements;

import me.matl114.gui.basic.ColorSampler;
import me.matl114.gui.basic.DrawableWidget;
import me.matl114.hacks.utils.gui.ModernDraw;
import me.matl114.utils.config.ValueAccessor;
import me.matl114.versioned.api.VDrawContext;

/** Window-style selector tab, sharing selection input and state colors with the base element. */
public final class RoundedSelectorTabElement extends SelectBackgroundElement {
    public RoundedSelectorTabElement(
            ValueAccessor<Boolean> selected,
            ColorSampler selectedColor,
            ColorSampler hoverColor,
            ColorSampler normalColor) {
        super(selected, selectedColor, hoverColor, normalColor);
    }

    @Override
    public void renderCentered0(
            DrawableWidget element,
            VDrawContext context,
            int mouseX,
            int mouseY,
            float delta,
            float alpha,
            boolean shouldHighlight) {
        int width = element.getTextureWidth();
        int height = element.getTextureHeight();
        ModernDraw.roundRect(
                context, 0, 0, width, height, Math.min(3.5f, height / 2.0f),
                ModernDraw.scaleAlpha(backgroundColor(shouldHighlight), alpha));
    }
}
