package me.matl114.slimefuncailorclickgui;

import me.matl114.hacks.MainTasks;
import me.matl114.plugins.SlimefunHelperPlugin;
import me.matl114.hacks.modules.task.WindowGuiModule;
import me.matl114.versioned.ModVersion;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class CailorClickGuiPlugin implements SlimefunHelperPlugin {
    public static final String MOD_ID = "slimefun-cailor-clickgui";
    @Override
    public void onConfigBootstrap() {}

    @Override
    public void onListenerBootstrap() {}

    @Override
    public void onModuleBootstrap() {
        MainTasks.getModuleManager().registerFactories(manager -> new WindowGuiModule().register(manager));
    }

    @Override
    public void postInitialize() {}

    @Override
    public String getPluginId() {
        return MOD_ID;
    }

    @Override
    public ModVersion getMinimumVersion() {
        return ModVersion.parse("1.2.1.3");
    }

    @Override
    public ModContainer getContainer() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Cailor ClickGui plugin mod container not found."));
    }
}
