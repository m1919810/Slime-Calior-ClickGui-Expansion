package me.matl114.hacks.utils.gui;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import me.matl114.gui.basic.ButtonAction;
import me.matl114.gui.basic.ColorSampler;
import me.matl114.gui.basic.ElementHandler;
import me.matl114.gui.basic.RenderHandler;
import me.matl114.gui.basic.TextProvider;
import me.matl114.gui.basic.TooltipHandler;
import me.matl114.gui.interfaces.CustomElementFactory;
import me.matl114.gui.interfaces.ElementFactory;
import me.matl114.hacks.utils.config.GuiSetting;
import me.matl114.gui.elements.ModernPanelElements;
import me.matl114.gui.elements.RoundedSelectorTabElement;
import me.matl114.gui.elements.SelectBackgroundElement;
import me.matl114.utils.config.Value;
import me.matl114.utils.config.ValueAccessor;
import me.matl114.utils.config.WrapperFactory;
import org.jetbrains.annotations.Nullable;

/** ElementFactory implementation for the window-style ClickGui. */
public class WindowElementFactory implements ElementFactory {
    private final Supplier<GuiSetting> setting;
    private final CustomElementFactory customFactory;

    public WindowElementFactory(Supplier<GuiSetting> setting) {
        this.setting = setting;
        this.customFactory = new CustomElementFactory(setting::get);
    }

    public ModernPalette palette() {
        return ModernPalette.of(setting.get());
    }

    @Override
    public SelectBackgroundElement createSelectorTab(ValueAccessor<Boolean> selected) {
        return createSelectorTab(
                selected,
                () -> setting.get().selectorSelectedColor().color(),
                () -> setting.get().selectorHoverColor().color(),
                () -> setting.get().selectorNormalColor().color());
    }

    @Override
    public SelectBackgroundElement createSelectorTab(
            ValueAccessor<Boolean> selected,
            ColorSampler selectedColor,
            ColorSampler hoverColor,
            ColorSampler normalColor) {
        return new RoundedSelectorTabElement(selected, selectedColor, hoverColor, normalColor);
    }

    public RenderHandler createPanel(IntSupplier width, IntSupplier height) {
        return ModernPanelElements.panel(this::palette, width, height);
    }

    public RenderHandler createPanelHeader(TextProvider title, BooleanSupplier expanded, IntSupplier enabledCount) {
        return ModernPanelElements.header(this::palette, title, expanded, enabledCount);
    }

    @Override
    public ElementHandler createLabel(TextProvider provider, TooltipHandler handler) {
        return customFactory.createLabel(provider, handler);
    }

    @Override
    public ElementHandler createLabel(TextProvider provider, TooltipHandler handler, boolean splitter) {
        return customFactory.createLabel(provider, handler, splitter);
    }

    @Override
    public ElementHandler createText(TextProvider provider, TooltipHandler handler) {
        return customFactory.createText(provider, handler);
    }

    @Override
    public ElementHandler createText(TextProvider provider, TooltipHandler handler, boolean splitter) {
        return customFactory.createText(provider, handler, splitter);
    }

    @Override
    public ElementHandler createBackground() {
        return ModernPanelElements.roundedBackground(this::palette);
    }

    @Override
    public ElementHandler createBackground(@Nullable ColorSampler overrideColor) {
        return overrideColor == null ? createBackground() : customFactory.createBackground(overrideColor);
    }

    @Override
    public ElementHandler createPlate(boolean hasInteract, @Nullable ColorSampler overrideColor) {
        return customFactory.createPlate(hasInteract, overrideColor);
    }

    @Override
    public ElementHandler createTitle(TextProvider provider, TooltipHandler handler) {
        return customFactory.createTitle(provider, handler);
    }

    @Override
    public ElementHandler createTitle(TextProvider provider, TooltipHandler handler, boolean splitter) {
        return customFactory.createTitle(provider, handler, splitter);
    }

    @Override
    public ElementHandler createButton(
            TextProvider provider,
            TooltipHandler handler,
            ButtonAction action,
            BooleanSupplier active,
            BooleanSupplier ignoredEnable) {
        return ModernPanelElements.button(this::palette, provider, handler, action, active);
    }

    @Override
    public ElementHandler createElement(
            ElementHandler handler, @Nullable TooltipHandler tooltips, @Nullable ButtonAction action) {
        return customFactory.createElement(handler, tooltips, action);
    }

    @Override
    public <T> ElementHandler createInput(Value<T> value) {
        return customFactory.createInput(value);
    }

    @Override
    public <T> ElementHandler createSlidingInput(Value<T> value, WrapperFactory<Double, T> sliderFactory) {
        return customFactory.createSlidingInput(value, sliderFactory);
    }
}
