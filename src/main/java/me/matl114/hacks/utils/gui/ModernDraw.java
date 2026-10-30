/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.util.Language
 *  net.minecraft.text.Text
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.text.StringVisitable
 *  net.minecraft.text.OrderedText
 */
package me.matl114.hacks.utils.gui;

import me.matl114.gui.basic.RenderHandler;
import me.matl114.gui.basic.TextAlign;
import me.matl114.renderer.text.TextDrawer;
import me.matl114.versioned.api.VDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

public final class ModernDraw {
    public static final int RED = -41620;

    private ModernDraw() {}

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int withAlpha(int color, int alpha) {
        return Math.max(0, Math.min(255, alpha)) << 24 | color & 0xFFFFFF;
    }

    public static int scaleAlpha(int color, float factor) {
        return ModernDraw.withAlpha(color, Math.round((float) ModernDraw.alpha(color) * factor));
    }

    public static int mix(int a, int b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int aa = a >>> 24;
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int ba = b >>> 24;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return Math.round((float) aa + (float) (ba - aa) * t) << 24
                | Math.round((float) ar + (float) (br - ar) * t) << 16
                | Math.round((float) ag + (float) (bg - ag) * t) << 8
                | Math.round((float) ab + (float) (bb - ab) * t);
    }

    public static int shade(int color, float t) {
        int target = t >= 0.0f ? 0xFFFFFF : 0;
        return ModernDraw.withAlpha(
                ModernDraw.mix(color | 0xFF000000, target | 0xFF000000, Math.abs(t)), ModernDraw.alpha(color));
    }

    public static int pixelScale() {
        var window = MinecraftClient.getInstance().getWindow();
        // Derive the integer GUI scale from stable dimensions; getScaleFactor changes return type across versions.
        int scaledWidth = window.getScaledWidth();
        int scaledHeight = window.getScaledHeight();
        int framebufferWidth = window.getFramebufferWidth();
        int framebufferHeight = window.getFramebufferHeight();
        double scaleX = scaledWidth > 0 && framebufferWidth > 0
                ? (double) framebufferWidth / scaledWidth
                : 0.0;
        double scaleY = scaledHeight > 0 && framebufferHeight > 0
                ? (double) framebufferHeight / scaledHeight
                : 0.0;
        double scale = Math.max(scaleX, scaleY);
        return scale > 0.0 ? Math.max(1, (int) Math.round(scale)) : 1;
    }

    private static void enterPixels(VDrawContext context, int k) {
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(1.0f / (float) k, 1.0f / (float) k);
    }

    private static void exitPixels(VDrawContext context) {
        context.getMatrices().popMatrix();
    }

    private static float cornerInset(float radius, int row) {
        float dy = radius - ((float) row + 0.5f);
        if (dy <= 0.0f) {
            return 0.0f;
        }
        return radius - (float) Math.sqrt(Math.max(0.0f, radius * radius - dy * dy));
    }

    private static void fillRowAA(VDrawContext context, int left, int right, int y, float inset, int color) {
        int full = (int) Math.ceil(inset);
        if (left + full >= right - full) {
            return;
        }
        context.fill(left + full, y, right - full, y + 1, color);
        float coverage = (float) full - inset;
        if (full > 0 && coverage > 0.03f) {
            int edge = ModernDraw.scaleAlpha(color, coverage);
            context.fill(left + full - 1, y, left + full, y + 1, edge);
            context.fill(right - full, y, right - full + 1, y + 1, edge);
        }
    }

    public static void roundRect(VDrawContext context, int x1, int y1, int x2, int y2, float radius, int color) {
        ModernDraw.roundRect(context, x1, y1, x2, y2, radius, color, true, true);
    }

    public static void roundRect(
            VDrawContext context,
            int x1,
            int y1,
            int x2,
            int y2,
            float radius,
            int color,
            boolean roundTop,
            boolean roundBottom) {
        if (x2 <= x1 || y2 <= y1 || ModernDraw.alpha(color) == 0) {
            return;
        }
        int k = ModernDraw.pixelScale();
        ModernDraw.roundRectPx(
                context, x1 * k, y1 * k, x2 * k, y2 * k, radius * (float) k, color, roundTop, roundBottom, k);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void roundRectPx(
            VDrawContext context,
            int px1,
            int py1,
            int px2,
            int py2,
            float r,
            int color,
            boolean roundTop,
            boolean roundBottom,
            int k) {
        r = Math.max(0.0f, Math.min(r, (float) Math.min(px2 - px1, py2 - py1) / 2.0f));
        int rows = (int) Math.ceil(r);
        ModernDraw.enterPixels(context, k);
        try {
            int bottom;
            int top = roundTop ? py1 + rows : py1;
            int n = bottom = roundBottom ? py2 - rows : py2;
            if (bottom > top) {
                context.fill(px1, top, px2, bottom, color);
            }
            for (int i = 0; i < rows; ++i) {
                float inset = ModernDraw.cornerInset(r, i);
                if (roundTop) {
                    ModernDraw.fillRowAA(context, px1, px2, py1 + i, inset, color);
                }
                if (!roundBottom) continue;
                ModernDraw.fillRowAA(context, px1, px2, py2 - 1 - i, inset, color);
            }
        } finally {
            ModernDraw.exitPixels(context);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void roundRectCorners(
            VDrawContext context,
            int x1,
            int y1,
            int x2,
            int y2,
            float radius,
            int color,
            boolean tl,
            boolean tr,
            boolean bl,
            boolean br) {
        if (x2 <= x1 || y2 <= y1 || ModernDraw.alpha(color) == 0) {
            return;
        }
        int k = ModernDraw.pixelScale();
        int px1 = x1 * k;
        int py1 = y1 * k;
        int px2 = x2 * k;
        int py2 = y2 * k;
        float r = Math.max(0.0f, Math.min(radius * (float) k, (float) Math.min(px2 - px1, py2 - py1) / 2.0f));
        int rows = (int) Math.ceil(r);
        ModernDraw.enterPixels(context, k);
        try {
            int bottom;
            int top = tl || tr ? py1 + rows : py1;
            int n = bottom = bl || br ? py2 - rows : py2;
            if (bottom > top) {
                context.fill(px1, top, px2, bottom, color);
            }
            for (int i = 0; i < rows; ++i) {
                float inset = ModernDraw.cornerInset(r, i);
                if (tl || tr) {
                    ModernDraw.cornerRow(context, px1, px2, py1 + i, tl ? inset : 0.0f, tr ? inset : 0.0f, color);
                }
                if (!bl && !br) continue;
                ModernDraw.cornerRow(context, px1, px2, py2 - 1 - i, bl ? inset : 0.0f, br ? inset : 0.0f, color);
            }
        } finally {
            ModernDraw.exitPixels(context);
        }
    }

    private static void cornerRow(VDrawContext context, int left, int right, int y, float li, float ri, int color) {
        int fr;
        int fl = (int) Math.ceil(li);
        if (left + fl >= right - (fr = (int) Math.ceil(ri))) {
            return;
        }
        context.fill(left + fl, y, right - fr, y + 1, color);
        float cl = (float) fl - li;
        float cr = (float) fr - ri;
        if (fl > 0 && cl > 0.03f) {
            context.fill(left + fl - 1, y, left + fl, y + 1, ModernDraw.scaleAlpha(color, cl));
        }
        if (fr > 0 && cr > 0.03f) {
            context.fill(right - fr, y, right - fr + 1, y + 1, ModernDraw.scaleAlpha(color, cr));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void vHairline(VDrawContext context, int x, int y1, int y2, int color) {
        int k = ModernDraw.pixelScale();
        ModernDraw.enterPixels(context, k);
        try {
            context.fill(x * k, y1 * k, x * k + 1, y2 * k, color);
        } finally {
            ModernDraw.exitPixels(context);
        }
    }

    public static void roundRing(VDrawContext context, int x1, int y1, int x2, int y2, float radius, int color) {
        ModernDraw.roundRing(context, x1, y1, x2, y2, radius, color, true, true, 1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void roundRing(
            VDrawContext context,
            int x1,
            int y1,
            int x2,
            int y2,
            float radius,
            int color,
            boolean roundTop,
            boolean roundBottom,
            int thicknessPx) {
        if (x2 <= x1 || y2 <= y1 || ModernDraw.alpha(color) == 0) {
            return;
        }
        int k = ModernDraw.pixelScale();
        int px1 = x1 * k;
        int py1 = y1 * k;
        int px2 = x2 * k;
        int py2 = y2 * k;
        float r = Math.max(0.0f, Math.min(radius * (float) k, (float) Math.min(px2 - px1, py2 - py1) / 2.0f));
        int t = Math.max(1, thicknessPx);
        float ri = Math.max(0.0f, r - (float) t);
        int rows = (int) Math.ceil(r);
        ModernDraw.enterPixels(context, k);
        try {
            int bottomRows;
            int topRows = roundTop ? Math.max(rows, t) : t;
            int n = bottomRows = roundBottom ? Math.max(rows, t) : t;
            if (py2 - bottomRows > py1 + topRows) {
                context.fill(px1, py1 + topRows, px1 + t, py2 - bottomRows, color);
                context.fill(px2 - t, py1 + topRows, px2, py2 - bottomRows, color);
            }
            for (int i = 0; i < Math.max(topRows, bottomRows); ++i) {
                if (i < topRows) {
                    ModernDraw.ringRow(context, px1, px2, py1 + i, i, r, ri, t, roundTop, color);
                }
                if (i >= bottomRows) continue;
                ModernDraw.ringRow(context, px1, px2, py2 - 1 - i, i, r, ri, t, roundBottom, color);
            }
        } finally {
            ModernDraw.exitPixels(context);
        }
    }

    private static void ringRow(
            VDrawContext context, int px1, int px2, int y, int i, float r, float ri, int t, boolean round, int color) {
        int outer;
        int n = outer = round ? (int) Math.ceil(ModernDraw.cornerInset(r, i)) : 0;
        if (i < t) {
            if (px1 + outer < px2 - outer) {
                context.fill(px1 + outer, y, px2 - outer, y + 1, color);
            }
            return;
        }
        int inner = t + (round ? (int) Math.ceil(ModernDraw.cornerInset(ri, i - t)) : 0);
        if (px1 + (inner = Math.max(inner, outer + 1)) > px2 - inner) {
            context.fill(px1 + outer, y, px2 - outer, y + 1, color);
            return;
        }
        context.fill(px1 + outer, y, px1 + inner, y + 1, color);
        context.fill(px2 - inner, y, px2 - outer, y + 1, color);
    }

    public static void shadow(VDrawContext context, int x1, int y1, int x2, int y2, float radius, int strength) {
        int k = ModernDraw.pixelScale();
        int spread = 3 * k;
        int per = Math.max(2, strength / spread);
        int px1 = x1 * k;
        int py1 = y1 * k + k / 2;
        int px2 = x2 * k;
        int py2 = y2 * k + k / 2;
        for (int i = spread; i >= 1; --i) {
            ModernDraw.roundRectPx(
                    context,
                    px1 - i,
                    py1 - i,
                    px2 + i,
                    py2 + i,
                    radius * (float) k + (float) i,
                    per << 24,
                    true,
                    true,
                    k);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void hairline(VDrawContext context, int x1, int x2, int y, int color) {
        int k = ModernDraw.pixelScale();
        ModernDraw.enterPixels(context, k);
        try {
            context.fill(x1 * k, y * k, x2 * k, y * k + 1, color);
        } finally {
            ModernDraw.exitPixels(context);
        }
    }

    public static void hGradient(VDrawContext context, int x1, int y1, int x2, int y2, int left, int right) {
        context.fillGuiGradient(x1, y1, x2, y2, left, left, right, right, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void chevron(VDrawContext context, float cx, float cy, float size, boolean expanded, int color) {
        int k = ModernDraw.pixelScale();
        float arm = Math.max(2.0f, size * (float) k / 2.0f);
        int thick = Math.max(2, Math.round((float) k * 0.6f));
        float px = cx * (float) k;
        float py = cy * (float) k;
        ModernDraw.enterPixels(context, k);
        try {
            if (expanded) {
                ModernDraw.line(context, px - arm, py - arm / 2.0f, px, py + arm / 2.0f, thick, color);
                ModernDraw.line(context, px, py + arm / 2.0f, px + arm, py - arm / 2.0f, thick, color);
            } else {
                ModernDraw.line(context, px - arm / 2.0f, py - arm, px + arm / 2.0f, py, thick, color);
                ModernDraw.line(context, px + arm / 2.0f, py, px - arm / 2.0f, py + arm, thick, color);
            }
        } finally {
            ModernDraw.exitPixels(context);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void check(VDrawContext context, int x1, int y1, int x2, int y2, int color) {
        int k = ModernDraw.pixelScale();
        int w = (x2 - x1) * k;
        int h = (y2 - y1) * k;
        int s = Math.min(w, h);
        int ox = x1 * k + (w - s) / 2;
        int oy = y1 * k + (h - s) / 2;
        int thick = Math.max(2, Math.round((float) s * 0.13f));
        ModernDraw.enterPixels(context, k);
        try {
            ModernDraw.line(
                    context,
                    (float) ox + (float) s * 0.22f,
                    (float) oy + (float) s * 0.5f,
                    (float) ox + (float) s * 0.42f,
                    (float) oy + (float) s * 0.7f,
                    thick,
                    color);
            ModernDraw.line(
                    context,
                    (float) ox + (float) s * 0.42f,
                    (float) oy + (float) s * 0.7f,
                    (float) ox + (float) s * 0.8f,
                    (float) oy + (float) s * 0.3f,
                    thick,
                    color);
        } finally {
            ModernDraw.exitPixels(context);
        }
    }

    private static void line(VDrawContext context, float x1, float y1, float x2, float y2, int thick, int color) {
        int steps = Math.max(1, Math.round(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))));
        int half = thick / 2;
        for (int i = 0; i <= steps; ++i) {
            float t = (float) i / (float) steps;
            int x = Math.round(x1 + (x2 - x1) * t);
            int y = Math.round(y1 + (y2 - y1) * t);
            context.fill(x - half, y - half, x - half + thick, y - half + thick, color);
        }
    }

    public static void toggleSwitch(
            VDrawContext context, int x1, int y1, int w, int h, float t, boolean hover, int accent) {
        float radius = (float) h / 2.0f;
        ModernDraw.roundRect(context, x1, y1, x1 + w, y1 + h, radius, hover ? 0x40FFFFFF : 0x2CFFFFFF);
        if (t > 0.0f) {
            ModernDraw.roundRect(context, x1, y1, x1 + w, y1 + h, radius, ModernDraw.scaleAlpha(accent, t));
        }
        int k = ModernDraw.pixelScale();
        float inset = Math.max(1.0f, (float) k * 0.75f);
        float knob = (float) (h * k) - 2.0f * inset;
        float travel = (float) (w * k) - 2.0f * inset - knob;
        int kx = Math.round((float) (x1 * k) + inset + travel * t);
        int ky = Math.round((float) (y1 * k) + inset);
        int size = Math.round(knob);
        int knobColor = ModernDraw.mix(-4604471, -1, t);
        ModernDraw.roundRectPx(context, kx, ky, kx + size, ky + size, (float) size / 2.0f, knobColor, true, true, k);
    }

    public static float crispScale(float wanted) {
        int k = ModernDraw.pixelScale();
        int target = (int) Math.floor((float) k * wanted);
        return (float) Math.max(1, target) / (float) k;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void textScaled(VDrawContext context, Text text, float x, float y, float scale, int color) {
        if (text == null) {
            return;
        }
        context.getMatrices().pushMatrix();
        try {
            context.getMatrices().translate(x, y);
            context.getMatrices().scale(scale, scale);
            context.drawText(context.getTextDrawer(), text.asOrderedText(), 0, 0, color, false);
        } finally {
            context.getMatrices().popMatrix();
        }
    }

    public static float textTop(VDrawContext context, float y1, float y2) {
        return (y1 + y2 - context.getTextDrawer().getTextHeight()) / 2.0f + 0.5f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void text(
            VDrawContext context,
            Text text,
            int x1,
            int y1,
            int x2,
            int y2,
            int color,
            TextAlign align,
            boolean scroll) {
        if (text == null) {
            return;
        }
        TextDrawer drawer = context.getTextDrawer();
        OrderedText ordered = text.asOrderedText();
        int width = drawer.getWidth(ordered);
        int available = x2 - x1;
        if (available <= 0) {
            return;
        }
        if (width > available) {
            if (scroll) {
                RenderHandler.drawScrollingText(
                        context, drawer, ordered, x1 - 2, y1, x2 + 2, y2, color, TextAlign.LEFT);
                return;
            }
            String dots = "..";
            StringVisitable trimmed =
                    drawer.trimToWidth((StringVisitable) text, Math.max(0, available - drawer.getWidth(dots)));
            ordered = Language.getInstance().reorder(StringVisitable.concat((StringVisitable[])
                    new StringVisitable[] {trimmed, StringVisitable.plain((String) dots)}));
            width = drawer.getWidth(ordered);
            align = TextAlign.LEFT;
        }
        float x =
                switch (align == null ? TextAlign.LEFT : align) {
                    default -> throw new MatchException(null, null);
                    case TextAlign.LEFT -> x1;
                    case TextAlign.RIGHT -> x2 - width;
                    case TextAlign.MIDDLE -> (float) (x1 + x2 - width) / 2.0f;
                };
        context.getMatrices().pushMatrix();
        try {
            context.getMatrices().translate(Math.round(x), ModernDraw.textTop(context, y1, y2));
            context.drawText(drawer, ordered, 0, 0, color, false);
        } finally {
            context.getMatrices().popMatrix();
        }
    }
}
