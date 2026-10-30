package me.matl114.hacks.utils.gui;

/** Resolved colors used by the window-style ClickGui. */
public record ModernPalette(
        int accent,
        int title,
        int text,
        int muted,
        int panel,
        int header,
        int surface,
        int surfaceDim,
        int hairline,
        int label,
        int input,
        int slider) {

    public static ModernPalette of(GuiColorSamplier colors) {
        int text = colors.textColor().withAlpha(255).color();
        int panel = colors.backgroundColor().color();
        int header = ModernDraw.withAlpha(ModernDraw.shade(panel, -0.35f), Math.max(ModernDraw.alpha(panel), 242));
        return new ModernPalette(
                colors.activeFrameColor().withAlpha(255).color(),
                colors.titleColor().withAlpha(255).color(),
                text,
                ModernDraw.mix(text, -8617325, 0.5f),
                panel,
                header,
                colors.buttonActiveColor().color(),
                colors.buttonInactiveColor().color(),
                colors.inactiveFrameColor().color(),
                colors.labelColor().color(),
                colors.textBackground().color(),
                colors.sliderColor().color());
    }

    public int onAccent() {
        return -1;
    }

    public int hover() {
        return 0x1AFFFFFF;
    }

    public int accentSoft() {
        return ModernDraw.withAlpha(accent, 58);
    }
}
