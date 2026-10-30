/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.text.MutableText
 */
package me.matl114.gui.elements;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import me.matl114.gui.basic.AbstractElement;
import me.matl114.gui.basic.ButtonAction;
import me.matl114.gui.basic.DrawableWidget;
import me.matl114.gui.basic.ElementHandler;
import me.matl114.gui.basic.RenderHandler;
import me.matl114.gui.basic.TextAlign;
import me.matl114.gui.basic.TextProvider;
import me.matl114.gui.basic.TooltipHandler;
import me.matl114.gui.complex.BoxElement;
import me.matl114.hacks.utils.gui.CustomGuiConfig;
import me.matl114.hacks.utils.gui.ModernDraw;
import me.matl114.hacks.utils.gui.ModernPalette;
import me.matl114.versioned.api.VDrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class ModernPanelElements {
    public static final float PANEL_RADIUS = 4.0f;
    public static final int BACKDROP_PADDING = 6;
    private static Supplier<ModernPalette> backdropPalette = () -> ModernPalette.of(CustomGuiConfig.DEFAULT);

    private ModernPanelElements() {}

    public static void setBackdropPalette(Supplier<ModernPalette> palette) {
        backdropPalette = palette;
    }

    public static ModernPalette backdropPalette() {
        try {
            return backdropPalette.get();
        } catch (Throwable ignored) {
            return ModernPalette.of(CustomGuiConfig.DEFAULT);
        }
    }

    public static RenderHandler backdrop(final DrawableWidget content) {
        return new AbstractElement() {

            @Override
            public void renderCentered0(
                    DrawableWidget element,
                    VDrawContext context,
                    int mouseX,
                    int mouseY,
                    float delta,
                    float alpha,
                    boolean shouldHighlight) {
                ModernPalette p = ModernPanelElements.backdropPalette();
                int w = content.width() + 12;
                int h = content.height() + 12;
                ModernDraw.shadow(context, 0, 0, w, h, 5.0f, 112);
                ModernDraw.roundRect(context, 0, 0, w, h, 5.0f, p.panel());
                ModernDraw.roundRing(context, 0, 0, w, h, 5.0f, p.hairline());
            }
        };
    }

    public static RenderHandler panel(
            final Supplier<ModernPalette> palette, final IntSupplier width, final IntSupplier height) {
        return new AbstractElement() {

            @Override
            public void renderCentered0(
                    DrawableWidget element,
                    VDrawContext context,
                    int mouseX,
                    int mouseY,
                    float delta,
                    float alpha,
                    boolean shouldHighlight) {
                ModernPalette p = (ModernPalette) palette.get();
                int w = width.getAsInt();
                int h = height.getAsInt();
                ModernDraw.shadow(context, 0, 0, w, h, 4.0f, 96);
                int panel = ModernDraw.withAlpha(p.panel(), Math.max(ModernDraw.alpha(p.panel()), 246));
                ModernDraw.roundRect(context, 0, 0, w, h, 4.0f, panel);
                ModernDraw.roundRing(context, 0, 0, w, h, 4.0f, p.hairline());
            }
        };
    }

    public static ElementHandler roundedBackground(Supplier<ModernPalette> palette) {
        return new AbstractElement() {
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
                float radius = Math.min(4.0f, height / 2.0f);
                ModernPalette colors = palette.get();
                ModernDraw.roundRect(context, 0, 0, width, height, radius, colors.panel());
                ModernDraw.roundRing(context, 0, 0, width, height, radius, colors.hairline());
            }
        };
    }

    public static ElementHandler button(
            Supplier<ModernPalette> palette,
            TextProvider text,
            TooltipHandler tooltips,
            ButtonAction action,
            BooleanSupplier active) {
        ButtonAction effectiveAction = action == ButtonAction.EMPTY ? null : action;
        return new RoundedButton(palette, text, effectiveAction, active).withTooltips(tooltips);
    }

    private static final class RoundedButton extends BoxElement {
        private final Supplier<ModernPalette> palette;
        private final TextProvider text;
        private final BooleanSupplier active;

        private RoundedButton(
                Supplier<ModernPalette> palette,
                TextProvider text,
                ButtonAction action,
                BooleanSupplier active) {
            super(action);
            this.palette = palette;
            this.text = text;
            this.active = active;
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
            ModernPalette colors = palette.get();
            boolean isActive = active.getAsBoolean();
            int width = element.getTextureWidth();
            int height = element.getTextureHeight();
            int background = isActive ? colors.accentSoft() : colors.surfaceDim();
            if (shouldHighlight) {
                background = ModernDraw.mix(background, 0xFFFFFFFF, 0.12f);
            }
            ModernDraw.roundRect(context, 0, 0, width, height, height / 2.0f, background);
            ModernDraw.roundRing(context, 0, 0, width, height, height / 2.0f, colors.hairline());
            ModernDraw.text(
                    context,
                    text.getText(element),
                    5,
                    0,
                    width - 5,
                    height,
                    colors.text(),
                    text.getAlign(element),
                    false);
        }
    }

    public static RenderHandler header(
            final Supplier<ModernPalette> palette,
            final TextProvider title,
            final BooleanSupplier expanded,
            final IntSupplier enabledCount) {
        return new AbstractElement() {

            @Override
            public void renderCentered0(
                    DrawableWidget element,
                    VDrawContext context,
                    int mouseX,
                    int mouseY,
                    float delta,
                    float alpha,
                    boolean shouldHighlight) {
                int count;
                ModernPalette p = (ModernPalette) palette.get();
                int w = element.getTextureWidth();
                int h = element.getTextureHeight();
                boolean open = expanded.getAsBoolean();
                ModernDraw.roundRect(context, 0, 0, w, h, 4.0f, p.header(), true, !open);
                if (shouldHighlight) {
                    ModernDraw.roundRect(context, 0, 0, w, h, 4.0f, 0xFFFFFFF, true, !open);
                }
                if (open) {
                    ModernDraw.hGradient(context, 0, h - 1, w, h, p.title(), ModernDraw.withAlpha(p.title(), 16));
                }
                int markerH = Math.max(4, h - 9);
                int markerY = (h - markerH) / 2;
                ModernDraw.roundRect(context, 5, markerY, 7, markerY + markerH, 1.0f, p.title());
                int right = w - h + 2;
                int n = count = enabledCount == null ? 0 : enabledCount.getAsInt();
                if (count > 0) {
                    MutableText badge = Text.literal((String) String.valueOf(count));
                    int bw = Math.max(9, element.getTextureHeight() - 6 + (count > 9 ? 4 : 0));
                    int by1 = (h - 9) / 2;
                    ModernDraw.roundRect(
                            context, right - bw, by1, right, by1 + 9, 4.5f, ModernDraw.withAlpha(p.accent(), 85));
                    ModernDraw.text(
                            context, (Text) badge, right - bw, by1, right, by1 + 9, p.text(), TextAlign.MIDDLE, false);
                    right -= bw + 3;
                }
                ModernDraw.text(context, title.getText(element), 11, 0, right, h, p.text(), TextAlign.LEFT, false);
                int chevronColor = shouldHighlight ? p.text() : p.muted();
                ModernDraw.chevron(
                        context, (float) w - (float) h / 2.0f - 0.5f, (float) h / 2.0f, 4.0f, open, chevronColor);
            }
        };
    }

}
