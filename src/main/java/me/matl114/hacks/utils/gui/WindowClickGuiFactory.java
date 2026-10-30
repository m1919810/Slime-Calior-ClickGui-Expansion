package me.matl114.hacks.utils.gui;

import java.util.function.Supplier;
import me.matl114.accessors.gui.ScreenAccess;
import me.matl114.gui.interfaces.CustomElementFactory;
import me.matl114.hacks.modules.task.ClickGui;
import me.matl114.hacks.modules.task.ClickGui.ClickGuiMetaData;
import me.matl114.hacks.utils.config.GuiSetting;
import me.matl114.hacks.modules.task.ModernClickGuiScreen;

/** Opens the modern window ClickGui using its custom and window element factories. */
public final class WindowClickGuiFactory implements ClickGuiFactory {
    private final CustomElementFactory customElementFactory;
    private final WindowElementFactory windowElementFactory;

    public WindowClickGuiFactory(Supplier<GuiSetting> setting) {
        this.customElementFactory = new CustomElementFactory(setting::get);
        this.windowElementFactory = new WindowElementFactory(setting);
    }

    @Override
    public void openClickGui(ClickGui clickGui, ClickGuiMetaData meta) {
        ModernClickGuiScreen screen = new ModernClickGuiScreen(
                clickGui, meta, clickGui.getExtraSelections(meta), customElementFactory, windowElementFactory);
        ScreenAccess.of(screen).addCloseFuture(() -> {
            screen.saveWindowState();
            clickGui.setClickGuiMeta(meta);
        });
        ScreenAccess.of(screen).openFromCurrent();
    }
}
