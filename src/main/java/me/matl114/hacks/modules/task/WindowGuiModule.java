package me.matl114.hacks.modules.task;

import me.matl114.gui.interfaces.ElementFactories;
import me.matl114.gui.interfaces.ElementStyle;
import me.matl114.hacks.api.BaseModule;
import me.matl114.hacks.api.ModulePath;
import me.matl114.hacks.utils.config.GuiSetting;
import me.matl114.hacks.utils.gui.ClickGuiStyle;
import me.matl114.hacks.utils.gui.ClickGuiStyles;
import me.matl114.hacks.utils.gui.CustomGuiConfig;
import me.matl114.managers.config.NBTRef;
import me.matl114.managers.Configs;
import me.matl114.hacks.utils.gui.ModernPalette;
import me.matl114.hacks.utils.gui.WindowClickGuiFactory;
import me.matl114.hacks.utils.gui.WindowElementFactory;
import me.matl114.gui.elements.ModernPanelElements;
import net.minecraft.util.Identifier;

/** Adds the optional Window ClickGui and element styles after ClickGui has initialized. */
public final class WindowGuiModule extends BaseModule {
    private static final Identifier WINDOW_STYLE = Identifier.of("slimefunhelper", "window");

    public static WindowGuiModule INSTANCE;

    private ClickGuiStyle clickGuiStyle;
    private ElementStyle elementStyle;
    private final ModulePath windowWidgetType =
            makePath(Configs.MISC_CONFIG, "click-gui.window-widget-style");
    private final NBTRef<GuiSetting> windowGuiSetting = builder(windowWidgetType.add("setting"), GuiSetting.class)
            .defaultValue(GuiSetting.DEFAULT)
            .build();

    public WindowGuiModule() {
        super("WindowGui");
        INSTANCE = this;
    }

    @Override
    public void registerAll() {
        super.registerAll();
        clickGuiStyle = ClickGuiStyles.register(new ClickGuiStyle(
                WINDOW_STYLE,
                WindowGuiModule::openWindow));
        elementStyle = ElementFactories.register(
                new ElementStyle(
                        WindowElementFactory::new,
                        true,
                        WINDOW_STYLE,
                        CustomGuiConfig.DEFAULT));
        ModernPanelElements.setBackdropPalette(() -> ModernPalette.of(windowGuiSetting.get()));
    }

    @Override
    public <W> void unregisterAll() {
        if (clickGuiStyle != null) {
            ClickGuiStyles.unregister(clickGuiStyle);
            clickGuiStyle = null;
        }
        if (elementStyle != null) {
            ElementFactories.unregister(elementStyle);
            elementStyle = null;
        }
        ModernPanelElements.setBackdropPalette(() -> ModernPalette.of(CustomGuiConfig.DEFAULT));
        super.unregisterAll();
    }

    private static void openWindow(ClickGui clickGui, ClickGui.ClickGuiMetaData meta) {
        new WindowClickGuiFactory(() -> INSTANCE.windowGuiSetting.get()).openClickGui(clickGui, meta);
    }
}
