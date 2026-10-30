/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.util.Util
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.text.MutableText
 */
package me.matl114.hacks.modules.task;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import me.matl114.SlimefunHelper;
import me.matl114.gui.FilterService;
import me.matl114.gui.GenericScreen;
import me.matl114.gui.WidgetUtils;
import me.matl114.gui.basic.AbstractElement;
import me.matl114.gui.basic.ButtonAction;
import me.matl114.gui.basic.ContentDelegateWidget;
import me.matl114.gui.basic.DisplayWidget;
import me.matl114.gui.basic.DrawableWidget;
import me.matl114.gui.basic.DynamicListWidget;
import me.matl114.gui.basic.ElementHandler;
import me.matl114.gui.basic.ExecutableWidget;
import me.matl114.gui.basic.ModernScrollPane;
import me.matl114.gui.basic.SubScreenWidget;
import me.matl114.gui.basic.TextAlign;
import me.matl114.gui.basic.TextProvider;
import me.matl114.gui.basic.TooltipHandler;
import me.matl114.gui.complex.BoxElement;
import me.matl114.gui.elements.TextFieldElement;
import me.matl114.gui.elements.SelectBackgroundElement;
import me.matl114.gui.interfaces.CustomElementFactory;
import me.matl114.hacks.api.BaseModule;
import me.matl114.hacks.api.ModuleGroup;
import me.matl114.hacks.modules.HackModules;
import me.matl114.hacks.utils.gui.ModernDraw;
import me.matl114.hacks.utils.gui.ModernPalette;
import me.matl114.hacks.utils.gui.ClassicClickGuiScreen;
import me.matl114.hacks.utils.gui.WindowElementFactory;
import me.matl114.managers.config.FlagRef;
import me.matl114.plugins.MainPlugin;
import me.matl114.renderer.TextRenderers;
import me.matl114.utils.ChatUtils;
import me.matl114.utils.NBTUtils;
import me.matl114.utils.config.Value;
import me.matl114.utils.config.ValueAccessor;
import me.matl114.versioned.api.VDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ModernClickGuiScreen extends GenericScreen {
    private static final String VIEW_MODULES = "modules";
    private static final String VIEW_PAGE = "page";
    private static final Identifier WINDOW_STATE_DATA = Identifier.of("slimefun-cailor-clickgui", "window_state");
    private static final int ROW_HEIGHT = 16;
    private static final int NAV_HEIGHT = 15;
    private static final int HEADER_HEIGHT = 32;
    private static final float RADIUS = 6.0f;
    private final ClickGui clickGui;
    private final ClickGui.ClickGuiMetaData meta;
    private final WindowState windowState;
    private final CustomElementFactory customFactory;
    private final WindowElementFactory factory;
    private final Map<String, Function<Screen, DrawableWidget>> pages;
    private double categoryScroll;
    private double moduleScroll;
    private double settingsScroll;
    private boolean refocusSearch;
    private ModernScrollPane moduleListPane;
    private DynamicListWidget moduleListContent;
    private ModernScrollPane categoryPane;
    private ModernScrollPane settingsPane;
    private ExecutableWidget searchWidget;
    private int wx;
    private int wy;
    private int ww;
    private int wh;
    private int sidebar;
    private int listX;
    private int listW;
    private int paneX;
    private int paneW;
    private List<BaseModule> currentModules = List.of();

    public ModernClickGuiScreen(
            ClickGui clickGui,
            ClickGui.ClickGuiMetaData meta,
            Map<String, Function<Screen, DrawableWidget>> pages,
            CustomElementFactory customFactory,
            WindowElementFactory factory) {
        super((Text) Text.empty(), 0, 0);
        ModuleGroup group;
        this.clickGui = clickGui;
        this.meta = meta;
        this.windowState = WindowState.from(meta.getExtraData(WINDOW_STATE_DATA));
        this.customFactory = customFactory;
        this.factory = factory;
        this.pages = pages;
        if (HackModules.getModuleGroup(this.windowState.getWindowCategory()) == null) {
            List<String> groups = clickGui.getModules();
            this.windowState.setWindowCategory(groups.isEmpty() ? "" : groups.get(0));
        }
        if (!this.pages.containsKey(this.windowState.getWindowPage()) || this.pages.get(this.windowState.getWindowPage()) == null) {
            this.windowState.setWindowPage(this.pages.keySet().iterator().next());
        }
        if (this.selectedModule() == null && (group = HackModules.getModuleGroup(this.windowState.getWindowCategory())) != null) {
            clickGui.getShowModuleList(group)
                    .findFirst()
                    .ifPresent(m -> this.windowState.setWindowModule(ModernClickGuiScreen.moduleKey(m)));
        }
    }

    public void saveWindowState() {
        NbtCompound encoded = (NbtCompound) WindowState.CODEC.encodeStart(NbtOps.INSTANCE, this.windowState).getOrThrow();
        this.meta.getExtraDatas().put(WINDOW_STATE_DATA, encoded);
    }

    private ModernPalette palette() {
        return this.factory.palette();
    }

    @Override
    protected void init0() {
        super.init0();
        this.x = 0;
        this.y = 0;
    }

    private boolean isPageView() {
        return VIEW_PAGE.equals(this.windowState.getWindowView());
    }

    private boolean isSearching() {
        return this.meta.getSearching() != null && !this.meta.getSearching().isEmpty();
    }

    private void refresh() {
        GenericScreen.mc.execute(() -> {
            this.rememberScroll();
            this.resetScreen();
        });
    }

    private void rememberScroll() {
        if (this.categoryPane != null) {
            this.categoryScroll = this.categoryPane.getScrollPercentage();
        }
        if (this.moduleListPane != null) {
            this.moduleScroll = this.moduleListPane.getScrollPercentage();
        }
        if (this.settingsPane != null) {
            this.settingsScroll = this.settingsPane.getScrollPercentage();
        }
    }

    private void selectCategory(String group) {
        this.windowState.setWindowView(VIEW_MODULES);
        this.windowState.setWindowCategory(group);
        this.meta.setSearching("");
        this.moduleScroll = 0.0;
        ModuleGroup moduleGroup = HackModules.getModuleGroup(group);
        BaseModule selected = this.selectedModule();
        if (!(moduleGroup == null
                || selected != null && moduleGroup.getModules().contains(selected))) {
            this.clickGui
                    .getShowModuleList(moduleGroup)
                    .findFirst()
                    .ifPresent(m -> this.windowState.setWindowModule(ModernClickGuiScreen.moduleKey(m)));
            this.settingsScroll = 0.0;
        }
        this.refresh();
    }

    private void selectModule(BaseModule module) {
        if (!ModernClickGuiScreen.moduleKey(module).equals(this.windowState.getWindowModule())) {
            this.settingsScroll = 0.0;
        }
        this.windowState.setWindowModule(ModernClickGuiScreen.moduleKey(module));
        this.refresh();
    }

    private void selectPage(String page) {
        if (this.pages.get(page) == null) {
            return;
        }
        if (!page.equals(this.windowState.getWindowPage()) || !this.isPageView() || this.isSearching()) {
            this.settingsScroll = 0.0;
        }
        this.meta.setSearching("");
        this.windowState.setWindowView(VIEW_PAGE);
        this.windowState.setWindowPage(page);
        this.refresh();
    }

    private static String moduleKey(BaseModule module) {
        return module.getModuleManager().getName() + "." + module.getName();
    }

    private BaseModule selectedModule() {
        String key = this.windowState.getWindowModule();
        if (key == null || key.isEmpty()) {
            return null;
        }
        for (ModuleGroup group : HackModules.getModuleGroups()) {
            for (BaseModule module : group.getModules()) {
                if (!ModernClickGuiScreen.moduleKey(module).equals(key)) continue;
                return module;
            }
        }
        return null;
    }

    private List<BaseModule> visibleModules() {
        if (this.isSearching()) {
            String filter = this.meta.getSearching();
            ArrayList<BaseModule> byName = new ArrayList<BaseModule>();
            ArrayList bySetting = new ArrayList();
            for (ModuleGroup group : HackModules.getModuleGroups()) {
                this.clickGui.getShowModuleList(group).forEach(module -> {
                    String name = module.getName();
                    String translated = ChatUtils.textToPlainString(this.clickGui.getModuleName((BaseModule) module));
                    if (FilterService.nameMatch(name, filter)
                            || !Objects.equals(name, translated) && FilterService.nameMatch(translated, filter)) {
                        byName.add((BaseModule) module);
                    } else if (module.getEditableConfig().stream()
                            .anyMatch(c -> FilterService.nameMatch(ChatUtils.parseTranslation(c.keyName()), filter))) {
                        bySetting.add(module);
                    }
                });
            }
            byName.addAll(bySetting);
            return byName;
        }
        ModuleGroup group = HackModules.getModuleGroup(this.windowState.getWindowCategory());
        return group == null
                ? List.of()
                : this.clickGui.getShowModuleList(group).toList();
    }

    private static boolean isEnabled(BaseModule module) {
        FlagRef flag = module.getBindFlag();
        return flag != null && flag.get();
    }

    private int enabledCount(ModuleGroup group) {
        return (int) this.clickGui
                .getShowModuleList(group)
                .filter(ModernClickGuiScreen::isEnabled)
                .count();
    }

    private MutableText groupName(String group) {
        return Text.translatableWithFallback((String) ("widget.click-gui.module-group-name." + group), (String) group);
    }

    private MutableText pageName(String page) {
        return Text.translatableWithFallback((String) ("widget.click-gui.selection." + page), (String) page);
    }

    @Override
    protected void refreshWidgets() {
        super.refreshWidgets();
        this.moduleListContent = null;
        this.moduleListPane = null;
        this.settingsPane = null;
        if (GenericScreen.mc.world != null) {
            DisplayWidget.instance(0, 0, this.getWidth(), this.getHeight())
                    .setRenderHandler((element, context, mouseX, mouseY, delta, alpha, highlight) -> context
                            .fillGuiGradient(
                                    0,
                                    0,
                                    this.getWidth(),
                                    this.getHeight(),
                                    0x70000000,
                                    0x50000000,
                                    0))
                    .addTo(this);
        }
        this.ww = Math.min(Math.max(this.getWidth() - 24, 340), Math.min(this.getWidth() - 8, 640));
        this.wh = Math.min(Math.max(this.getHeight() - 24, 200), Math.min(this.getHeight() - 8, 380));
        this.wx = (this.getWidth() - this.ww) / 2;
        this.wy = (this.getHeight() - this.wh) / 2;
        this.sidebar = Math.max(96, Math.min(124, Math.round((float) this.ww * 0.22f)));
        this.listX = this.wx + this.sidebar;
        int contentW = this.ww - this.sidebar;
        this.listW = Math.max(112, Math.min(160, Math.round((float) contentW * 0.38f)));
        this.paneX = this.listX + this.listW;
        this.paneW = this.ww - this.sidebar - this.listW;
        ((DrawableWidget) DisplayWidget.instance(this.wx, this.wy, this.ww, this.wh)
                        .setRenderHandler(this::renderFrame))
                .addTo(this);
        this.initSidebar();
        if (this.isPageView() && !this.isSearching()) {
            this.initPageView();
        } else {
            this.initModuleList();
            this.initSettingsPane();
        }
        if (this.refocusSearch && this.searchWidget != null) {
            this.refocusSearch = false;
            this.setFocused(this.searchWidget);
            this.searchWidget.setSelected(true);
        }
    }

    private void renderFrame(
            DrawableWidget element,
            VDrawContext context,
            int mouseX,
            int mouseY,
            float delta,
            float alpha,
            boolean highlight) {
        boolean pageView;
        ModernPalette p = this.palette();
        int w = this.ww;
        int h = this.wh;
        int base = ModernDraw.withAlpha(ModernDraw.shade(p.panel(), -0.05f), 244);
        int side = ModernDraw.withAlpha(ModernDraw.shade(p.panel(), -0.45f), 248);
        int list = ModernDraw.withAlpha(ModernDraw.shade(p.panel(), -0.22f), 246);
        ModernDraw.shadow(context, 0, 0, w, h, 6.0f, 144);
        ModernDraw.roundRectCorners(context, 0, 0, this.sidebar, h, 6.0f, side, true, false, true, false);
        boolean bl = pageView = this.isPageView() && !this.isSearching();
        if (pageView) {
            ModernDraw.roundRectCorners(context, this.sidebar, 0, w, h, 6.0f, base, false, true, false, true);
        } else {
            ModernDraw.roundRectCorners(
                    context, this.sidebar, 0, this.sidebar + this.listW, h, 6.0f, list, false, false, false, false);
            ModernDraw.roundRectCorners(
                    context, this.sidebar + this.listW, 0, w, h, 6.0f, base, false, true, false, true);
            ModernDraw.vHairline(context, this.sidebar + this.listW, 0, h, p.hairline());
        }
        ModernDraw.vHairline(context, this.sidebar, 0, h, p.hairline());
        ModernDraw.roundRing(context, 0, 0, w, h, 6.0f, ModernDraw.scaleAlpha(p.hairline(), 1.4f));
    }

    private void initSidebar() {
        int pad = 7;
        String version = SlimefunHelper.getModContainer() == null
                ? ""
                : SlimefunHelper.getModContainer().getMetadata().getVersion().getFriendlyString();
        ((DrawableWidget) DisplayWidget.instance(this.wx, this.wy, this.sidebar, 30)
                        .setRenderHandler((el, ctx, mx, my, d, a, hl) -> {
                            ModernPalette p = this.palette();
                            int markY = 9;
                            ModernDraw.roundRect(ctx, pad + 1, markY, pad + 9, markY + 8, 2.5f, p.accent());
                            ModernDraw.roundRect(
                                    ctx,
                                    pad + 3,
                                    markY + 2,
                                    pad + 7,
                                    markY + 6,
                                    1.0f,
                                    ModernDraw.withAlpha(0xFFFFFF, 208));
                            MutableText brand = Text.literal((String) "Slimefun")
                                    .append((Text)
                                            Text.literal((String) "Helper").withColor(p.accent() & 0xFFFFFF));
                            ModernDraw.text(
                                    ctx,
                                    (Text) brand,
                                    pad + 13,
                                    4,
                                    this.sidebar - 4,
                                    18,
                                    p.text(),
                                    TextAlign.LEFT,
                                    false);
                            ModernDraw.text(
                                    ctx,
                                    (Text) Text.literal((String) ("v" + version)),
                                    pad + 13,
                                    14,
                                    this.sidebar - 4,
                                    28,
                                    ModernDraw.scaleAlpha(p.muted(), 0.8f),
                                    TextAlign.LEFT,
                                    false);
                        }))
                .addTo(this);
        Value<String> searchValue = new Value<String>() {

            @Override
            public String getInput() {
                return ModernClickGuiScreen.this.meta.getSearching();
            }

            @Override
            public String get() {
                return ModernClickGuiScreen.this.meta.getSearching();
            }

            @Override
            public boolean setOriginValue(String value) {
                this.setInput(value);
                return true;
            }

            @Override
            public boolean isValueValid(String value) {
                return value != null;
            }

            @Override
            public boolean isValidate() {
                return true;
            }

            @Override
            public String toInput(String value) {
                return value == null ? "" : value;
            }

            @Override
            public void setInput(String value) {
                String next;
                String string = next = value == null ? "" : value;
                if (Objects.equals(next, ModernClickGuiScreen.this.meta.getSearching())) {
                    return;
                }
                boolean wasModuleView =
                        !ModernClickGuiScreen.this.isPageView() || ModernClickGuiScreen.this.isSearching();
                ModernClickGuiScreen.this.meta.setSearching(next);
                ModernClickGuiScreen.this.moduleScroll = 0.0;
                if (wasModuleView
                        && ModernClickGuiScreen.this.moduleListContent != null
                        && (ModernClickGuiScreen.this.isSearching() || !ModernClickGuiScreen.this.isPageView())) {
                    GenericScreen.mc.execute(ModernClickGuiScreen.this::fillModuleList);
                } else {
                    ModernClickGuiScreen.this.refocusSearch = true;
                    ModernClickGuiScreen.this.refresh();
                }
            }
        };
        ElementHandler input = this.customFactory.createInput(searchValue);
        if (input instanceof TextFieldElement) {
            TextFieldElement field = (TextFieldElement) input;
            field.setPlaceholder((Text) Text.literal((String) "\u641c\u7d22\u6a21\u5757 / \u8bbe\u7f6e"));
        }
        this.searchWidget = ExecutableWidget.instance(this.wx + pad, this.wy + 31, this.sidebar - 2 * pad, 14)
                .setElementHandler(input);
        this.searchWidget.addTo(this);
        int captionY = this.wy + 51;
        this.caption(this.wx + pad + 2, captionY, this.sidebar - 2 * pad, (Text)
                Text.literal((String) "\u6a21\u5757\u5206\u7c7b"));
        int bottomReserve = 26;
        int navTop = captionY + 11;
        int navHeight = this.wy + this.wh - bottomReserve - navTop;
        this.categoryPane = new ModernScrollPane(this.wx + 4, navTop, this.sidebar - 8 - 4, navHeight);
        DynamicListWidget categories = new DynamicListWidget(0, 0, this.sidebar - 12);
        for (String groupId : this.clickGui.getModules()) {
            ModuleGroup group = HackModules.getModuleGroup(groupId);
            if (group == null) continue;
            categories.addDrawableChild((DrawableWidget) ExecutableWidget.instance(0, 1, this.sidebar - 12, 15)
                    .setElementHandler(new NavItem(
                            ButtonAction.run(() -> this.selectCategory(groupId)),
                            null,
                            this.groupName(groupId),
                            () -> !this.isSearching()
                                    && !this.isPageView()
                                    && groupId.equals(this.windowState.getWindowCategory()),
                            () -> this.enabledCount(group))));
        }
        this.categoryPane.addScrollingWidget(categories);
        this.categoryPane.setScrollPercentage(this.categoryScroll);
        this.categoryPane.addTo(this);
        int settingsY = this.wy + this.wh - bottomReserve + 4;
        ((DrawableWidget) DisplayWidget.instance(this.wx + 8, settingsY - 4, this.sidebar - 16, 1)
                        .setRenderHandler((el, ctx, mx, my, d, a, hl) -> ModernDraw.hairline(
                                ctx, 0, el.getTextureWidth(), 0, this.palette().hairline())))
                .addTo(this);
        ((DrawableWidget) ExecutableWidget.instance(this.wx + 4, settingsY, this.sidebar - 8 - 4, 16)
                        .setElementHandler(new NavItem(
                                ButtonAction.run(() -> this.selectPage(this.windowState.getWindowPage())),
                                null,
                                (Text) Text.literal((String) "\u8bbe\u7f6e\u4e0e\u5de5\u5177"),
                                () -> this.isPageView() && !this.isSearching(),
                                () -> 0)))
                .addTo(this);
    }

    private void caption(int x, int y, int w, Text text) {
        ((DrawableWidget) DisplayWidget.instance(x, y, w, 10)
                        .setRenderHandler((el, ctx, mx, my, d, a, hl) -> ModernDraw.text(
                                ctx,
                                text,
                                0,
                                0,
                                el.getTextureWidth(),
                                el.getTextureHeight(),
                                ModernDraw.scaleAlpha(this.palette().muted(), 0.85f),
                                TextAlign.LEFT,
                                false)))
                .addTo(this);
    }

    private void initModuleList() {
        ((DrawableWidget) DisplayWidget.instance(this.listX, this.wy, this.listW, 32)
                        .setRenderHandler((el, ctx, mx, my, d, a, hl) -> {
                            ModernPalette p = this.palette();
                            MutableText title = this.isSearching()
                                    ? Text.literal((String) "\u641c\u7d22\u7ed3\u679c")
                                    : this.groupName(this.windowState.getWindowCategory());
                            ModernDraw.text(
                                    ctx, (Text) title, 10, 6, this.listW - 8, 17, p.text(), TextAlign.LEFT, false);
                            List<BaseModule> modules = this.currentModules;
                            int on = (int) modules.stream()
                                    .filter(ModernClickGuiScreen::isEnabled)
                                    .count();
                            MutableText sub = this.isSearching()
                                    ? Text.literal((String) (modules.size() + " \u4e2a\u5339\u914d"))
                                    : Text.literal((String) (on + " / " + modules.size() + " \u5df2\u542f\u7528"));
                            ModernDraw.text(
                                    ctx, (Text) sub, 10, 16, this.listW - 8, 27, p.muted(), TextAlign.LEFT, false);
                        }))
                .addTo(this);
        int top = this.wy + 32;
        this.moduleListPane = new ModernScrollPane(this.listX + 4, top, this.listW - 8 - 5, this.wh - 32 - 5);
        this.moduleListContent = new DynamicListWidget(0, 0, this.listW - 13);
        this.moduleListPane.addScrollingWidget(this.moduleListContent);
        this.fillModuleList();
        this.moduleListPane.setScrollPercentage(this.moduleScroll);
        this.moduleListPane.addTo(this);
    }

    private void fillModuleList() {
        if (this.moduleListContent == null) {
            return;
        }
        this.moduleListContent.clearChildren();
        this.currentModules = this.visibleModules();
        int rowW = this.listW - 13;
        if (this.currentModules.isEmpty()) {
            this.moduleListContent.addDrawableChild((DrawableWidget) DisplayWidget.instance(0, 8, rowW, 20)
                    .setRenderHandler((el, ctx, mx, my, d, a, hl) -> ModernDraw.text(
                            ctx,
                            (Text) Text.literal((String) "\u6ca1\u6709\u5339\u914d\u7684\u6a21\u5757"),
                            0,
                            0,
                            rowW,
                            20,
                            this.palette().muted(),
                            TextAlign.MIDDLE,
                            false)));
        }
        for (BaseModule module : this.currentModules) {
            SubScreenWidget row = new SubScreenWidget(0, 1, rowW, 16);
            FlagRef flag = module.getBindFlag();
            if (flag != null) {
                ((DrawableWidget) ExecutableWidget.instance(rowW - 24, 3, 20, 9)
                                .setElementHandler(new SwitchElement(flag::toggle, flag::get)
                                        .withTooltips(TooltipHandler.of(List.of(
                                                Text.literal((String) "\u70b9\u51fb\u5f00\u5173\u6a21\u5757"))))))
                        .addToSub(row, 2);
            }
            ArrayList<Text> tips = new ArrayList<Text>(this.clickGui.getModuleDescriptionTooltips(module));
            tips.add((Text) Text.empty());
            tips.add((Text) Text.literal((String)
                    (flag != null
                            ? "\u5de6\u952e\u67e5\u770b\u8bbe\u7f6e \u00b7 \u53f3\u952e\u5f00\u5173"
                            : "\u5de6\u952e\u67e5\u770b\u8bbe\u7f6e")));
            ((DrawableWidget) ExecutableWidget.instance(0, 0, rowW, 16)
                            .setElementHandler(new ModuleRow(
                                            ButtonAction.isLeft(left -> {
                                                if (!left.booleanValue() && flag != null) {
                                                    flag.toggle();
                                                } else {
                                                    this.selectModule(module);
                                                }
                                            }),
                                            this.clickGui.getModuleName(module),
                                            this.isSearching()
                                                    ? this.groupName(module.getModuleManager()
                                                            .getName())
                                                    : null,
                                            () -> ModernClickGuiScreen.moduleKey(module)
                                                    .equals(this.windowState.getWindowModule()),
                                            () -> ModernClickGuiScreen.isEnabled(module),
                                            flag != null)
                                    .withTooltips(TooltipHandler.of(tips))))
                    .addToSub(row, 1);
            this.moduleListContent.addDrawableChild(row);
        }
        if (this.moduleListPane != null) {
            this.moduleListPane.setScrollPercentage(this.moduleScroll);
        }
    }

    private WidgetUtils.ConfigScreenLayout layoutFor(int total, int maxIndex) {
        int index = Math.min(maxIndex, Math.round((float) total * 0.46f));
        int blank = 4;
        return new WidgetUtils.ConfigScreenLayout(index, blank, total - index - blank, 16, 2);
    }

    private void initSettingsPane() {
        BaseModule module = this.selectedModule();
        if (module == null) {
            ((DrawableWidget) DisplayWidget.instance(this.paneX, this.wy, this.paneW, this.wh)
                            .setRenderHandler((el, ctx, mx, my, d, a, hl) -> {
                                ModernPalette p = this.palette();
                                int cy = this.wh / 2;
                                ModernDraw.text(
                                        ctx,
                                        (Text) Text.literal((String) "\u9009\u62e9\u4e00\u4e2a\u6a21\u5757"),
                                        0,
                                        cy - 12,
                                        this.paneW,
                                        cy - 2,
                                        p.text(),
                                        TextAlign.MIDDLE,
                                        false);
                                ModernDraw.text(
                                        ctx,
                                        (Text)
                                                Text.literal(
                                                        (String)
                                                                "\u5728\u5de6\u4fa7\u5217\u8868\u4e2d\u70b9\u51fb\u5373\u53ef\u67e5\u770b\u548c\u4fee\u6539\u5b83\u7684\u8bbe\u7f6e"),
                                        8,
                                        cy,
                                        this.paneW - 8,
                                        cy + 10,
                                        p.muted(),
                                        TextAlign.MIDDLE,
                                        false);
                            }))
                    .addTo(this);
            return;
        }
        int pad = 12;
        FlagRef flag = module.getBindFlag();
        Text name = this.clickGui.getModuleName(module);
        List<Text> description = this.clickGui.getModuleDescriptionTooltips(module);
        int switchW = 24;
        ((DrawableWidget) ExecutableWidget.instance(this.paneX, this.wy, this.paneW, 40)
                        .setElementHandler(new AbstractElement()
                                .combineRender((el, ctx, mx, my, d, a, hl) -> {
                                    ModernPalette p = this.palette();
                                    float scale = ModernDraw.crispScale(1.5f);
                                    int right = this.paneW - pad - (flag != null ? switchW + 8 : 0);
                                    Text title = name;
                                    int titleWidth = Math.round(
                                            (float) TextRenderers.get().getWidth(title) * scale);
                                    if (titleWidth > right - pad) {
                                        scale = 1.0f;
                                    }
                                    ModernDraw.textScaled(ctx, title, pad, 9.0f, scale, p.text());
                                    MutableText line =
                                            description.isEmpty() ? Text.empty() : (MutableText) description.get(0);
                                    ModernDraw.text(
                                            ctx,
                                            (Text) line,
                                            pad,
                                            24,
                                            this.paneW - pad,
                                            34,
                                            p.muted(),
                                            TextAlign.LEFT,
                                            hl);
                                    ModernDraw.hairline(ctx, pad - 2, this.paneW - pad + 2, 39, p.hairline());
                                })
                                .withTooltips(TooltipHandler.of(description))))
                .addTo(this);
        if (flag != null) {
            ((DrawableWidget)
                            ExecutableWidget.instance(this.paneX + this.paneW - pad - switchW, this.wy + 9, switchW, 12)
                                    .setElementHandler(new SwitchElement(flag::toggle, flag::get)
                                            .withTooltips(TooltipHandler.of(List.of(Text.literal(
                                                    (String) "\u542f\u7528 / \u7981\u7528\u8be5\u6a21\u5757"))))))
                    .addTo(this);
        }
        int top = this.wy + 44;
        int viewW = this.paneW - 20 - 5;
        this.settingsPane = new ModernScrollPane(this.paneX + 10, top, viewW, this.wh - 44 - 6);
        DynamicListWidget list =
                this.clickGui.createModuleSettingsList(module, this.meta, this.layoutFor(viewW, 150), false);
        if (list.height() <= 0) {
            list.addDrawableChild((DrawableWidget) DisplayWidget.instance(0, 10, viewW, 20)
                    .setRenderHandler((el, ctx, mx, my, d, a, hl) -> ModernDraw.text(
                            ctx,
                            (Text) Text.literal((String)
                                    "\u8fd9\u4e2a\u6a21\u5757\u6ca1\u6709\u53ef\u8c03\u6574\u7684\u8bbe\u7f6e"),
                            0,
                            0,
                            viewW,
                            20,
                            this.palette().muted(),
                            TextAlign.MIDDLE,
                            false)));
        }
        this.settingsPane.addScrollingWidget(list);
        this.settingsPane.setScrollPercentage(this.settingsScroll);
        this.settingsPane.addTo(this);
    }

    private void initPageView() {
        DrawableWidget page;
        int contentX = this.listX;
        int contentW = this.ww - this.sidebar;
        int tabTop = this.wy + 9;
        int tabH = 14;
        ArrayList<String> keys = new ArrayList<String>(this.pages.keySet());
        int[] widths = new int[keys.size()];
        int total = 0;
        for (int i = 0; i < keys.size(); ++i) {
            widths[i] = Math.max(30, TextRenderers.get().getWidth(this.pageName((String) keys.get(i))) + 14);
            total += widths[i];
        }
        int available = contentW - 24;
        if (total > available) {
            total = 0;
            for (int i = 0; i < keys.size(); ++i) {
                widths[i] = available / keys.size();
                total += widths[i];
            }
        }
        int barX = contentX + (contentW - total) / 2;
        ((DrawableWidget) ExecutableWidget.instance(barX - 2, tabTop - 2, total + 4, tabH + 4)
                        .setElementHandler(this.factory.createButton(
                                TextProvider.of(Text.empty()),
                                TooltipHandler.of(List.of()),
                                ButtonAction.empty(),
                                () -> false,
                                () -> true)))
                .addTo(this);
        int x = barX;
        for (int i = 0; i < keys.size(); ++i) {
            String key = (String) keys.get(i);
            ((DrawableWidget) ExecutableWidget.instance(x, tabTop, widths[i], tabH)
                            .setElementHandler(this.factory.createSelectorTab(ValueAccessor.of(
                                            () -> key.equals(this.windowState.getWindowPage()),
                                            selected -> {
                                                if (selected) this.selectPage(key);
                                            }))
                                    .withElement(this.factory.createText(
                                            TextProvider.of(this.pageName(key)), TooltipHandler.of(List.of())))
                                    .withTooltips(TooltipHandler.of(ChatUtils.parseTooltipsTranslation(
                                            "widget.click-gui.selection." + key + ".tooltips", "")))))
                    .addTo(this);
            x += widths[i];
        }
        int top = this.wy + 32 + 4;
        int viewW = contentW - 28 - 5;
        this.settingsPane = new ModernScrollPane(contentX + 14, top, viewW, this.wh - 32 - 4 - 6);
        this.settingsPane.setNestedScrollHandling(true);
        Function<Screen, DrawableWidget> builder = this.pages.get(this.windowState.getWindowPage());
        Screen pageViewport = new GenericScreen(Text.empty(), 0, 0);
        pageViewport.width = viewW;
        pageViewport.height = this.settingsPane.height() + 2 * ClassicClickGuiScreen.BUTTON_HEIGHT;
        DrawableWidget drawableWidget = page = builder == null ? null : builder.apply(pageViewport);
        if (page instanceof ContentDelegateWidget<?> contentDelegate
                && (contentDelegate.width() == 0 || contentDelegate.height() == 0)) {
            contentDelegate.width(viewW);
            contentDelegate.height(this.settingsPane.height());
        }
        if (page != null) {
            this.settingsPane.addScrollingWidget(page);
        }
        this.settingsPane.setScrollPercentage(this.settingsScroll);
        this.settingsPane.addTo(this);
    }

    private static final class WindowState {
        private static final Codec<WindowState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.STRING.optionalFieldOf("view", VIEW_MODULES).forGetter(WindowState::getWindowView),
                        Codec.STRING.optionalFieldOf("category", "").forGetter(WindowState::getWindowCategory),
                        Codec.STRING.optionalFieldOf("module", "").forGetter(WindowState::getWindowModule),
                        Codec.STRING.optionalFieldOf("page", "").forGetter(WindowState::getWindowPage))
                .apply(instance, WindowState::new));

        private String view;
        private String category;
        private String module;
        private String page;

        private static WindowState from(NbtCompound data) {
            WindowState stored = NBTUtils.toValue(data, CODEC);
            return stored == null ? new WindowState(VIEW_MODULES, "", "", "") : stored;
        }

        private WindowState(String view, String category, String module, String page) {
            this.view = view;
            this.category = category;
            this.module = module;
            this.page = page;
        }

        private String getWindowView() {
            return view;
        }

        private void setWindowView(String view) {
            this.view = view;
        }

        private String getWindowCategory() {
            return category;
        }

        private void setWindowCategory(String category) {
            this.category = category;
        }

        private String getWindowModule() {
            return module;
        }

        private void setWindowModule(String module) {
            this.module = module;
        }

        private String getWindowPage() {
            return page;
        }

        private void setWindowPage(String page) {
            this.page = page;
        }
    }

    private class NavItem extends BoxElement {
        private final Identifier icon;
        private final Text text;
        private final BooleanSupplier selected;
        private final IntSupplier badge;
        private final SelectBackgroundElement selector;

        NavItem(ButtonAction action, Identifier icon, Text text, BooleanSupplier selected, IntSupplier badge) {
            super(action);
            this.icon = icon;
            this.text = text;
            this.selected = selected;
            this.badge = badge;
            this.selector = ModernClickGuiScreen.this.factory.createSelectorTab(
                    ValueAccessor.ofIgnore(selected::getAsBoolean));
        }

        @Override
        public void renderCentered0(
                DrawableWidget element,
                VDrawContext context,
                int mouseX,
                int mouseY,
                float delta,
                float alpha,
                boolean hl) {
            ModernPalette p = ModernClickGuiScreen.this.palette();
            int w = element.getTextureWidth();
            int h = element.getTextureHeight();
            boolean on = this.selected.getAsBoolean();
            this.selector.renderAtCentered(element, context, mouseX, mouseY, delta, alpha, hl);
            if (on) {
                ModernDraw.roundRect(context, 0, 3, 2, h - 3, 1.0f, p.accent());
            }
            int right = w - 5;
            int count = this.badge.getAsInt();
            if (count > 0) {
                MutableText number = Text.literal((String) String.valueOf(count));
                int bw = Math.max(11, TextRenderers.get().getWidth((Text) number) + 6);
                int by = (h - 9) / 2;
                ModernDraw.roundRect(
                        context,
                        right - bw,
                        by,
                        right,
                        by + 9,
                        4.5f,
                        on ? ModernDraw.withAlpha(p.accent(), 176) : 0x24FFFFFF);
                ModernDraw.text(
                        context,
                        (Text) number,
                        right - bw,
                        by,
                        right,
                        by + 9,
                        on ? p.onAccent() : p.muted(),
                        TextAlign.MIDDLE,
                        false);
                right -= bw + 3;
            }
            int textX = 8;
            if (this.icon != null) {
                int size = 9;
                int color = on ? p.accent() : (hl ? p.text() : p.muted());
                context.setShaderColor(
                        (float) (color >> 16 & 0xFF) / 255.0f,
                        (float) (color >> 8 & 0xFF) / 255.0f,
                        (float) (color & 0xFF) / 255.0f,
                        1.0f);
                context.drawGuiTexture(this.icon, 7, (h - size) / 2, size, size);
                context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                textX = 21;
            }
            ModernDraw.text(
                    context, this.text, textX, 0, right, h, on || hl ? p.text() : p.muted(), TextAlign.LEFT, false);
        }
    }

    private class SwitchElement extends BoxElement {
        private final BooleanSupplier value;
        private float progress;
        private long lastFrame;

        SwitchElement(Runnable toggle, BooleanSupplier value) {
            super(ButtonAction.run(toggle));
            this.progress = -1.0f;
            this.value = value;
        }

        @Override
        public void renderCentered0(
                DrawableWidget element,
                VDrawContext context,
                int mouseX,
                int mouseY,
                float delta,
                float alpha,
                boolean hl) {
            float target = this.value.getAsBoolean() ? 1.0f : 0.0f;
            long now = Util.getMeasuringTimeMs();
            if (this.progress < 0.0f) {
                this.progress = target;
            } else {
                this.progress += (target - this.progress) * Math.min(1.0f, (float) (now - this.lastFrame) / 90.0f);
                if (Math.abs(target - this.progress) < 0.01f) {
                    this.progress = target;
                }
            }
            this.lastFrame = now;
            ModernDraw.toggleSwitch(
                    context,
                    0,
                    0,
                    element.getTextureWidth(),
                    element.getTextureHeight(),
                    this.progress,
                    hl,
                    ModernClickGuiScreen.this.palette().accent());
        }
    }

    private class ModuleRow extends BoxElement {
        private final Text name;
        private final Text tag;
        private final BooleanSupplier selected;
        private final BooleanSupplier enabled;
        private final boolean hasSwitch;

        ModuleRow(
                ButtonAction action,
                Text name,
                Text tag,
                BooleanSupplier selected,
                BooleanSupplier enabled,
                boolean hasSwitch) {
            super(action);
            this.name = name;
            this.tag = tag;
            this.selected = selected;
            this.enabled = enabled;
            this.hasSwitch = hasSwitch;
        }

        @Override
        public void renderCentered0(
                DrawableWidget element,
                VDrawContext context,
                int mouseX,
                int mouseY,
                float delta,
                float alpha,
                boolean hl) {
            ModernPalette p = ModernClickGuiScreen.this.palette();
            int w = element.getTextureWidth();
            int h = element.getTextureHeight();
            boolean sel = this.selected.getAsBoolean();
            boolean on = this.enabled.getAsBoolean();
            if (sel) {
                ModernDraw.roundRect(
                        context, 0, 0, w, h, 3.5f, ModernDraw.withAlpha(ModernDraw.shade(p.surface(), 0.06f), 240));
                ModernDraw.roundRing(context, 0, 0, w, h, 3.5f, ModernDraw.withAlpha(p.accent(), 144));
            } else if (hl) {
                ModernDraw.roundRect(context, 0, 0, w, h, 3.5f, p.hover());
            }
            int dot = on ? p.accent() : 0x40FFFFFF;
            ModernDraw.roundRect(context, 6, h / 2 - 2, 10, h / 2 + 2, 2.0f, dot);
            int right = w - (this.hasSwitch ? 28 : 6);
            if (this.tag != null) {
                int tw = TextRenderers.get().getWidth(this.tag);
                ModernDraw.text(
                        context,
                        this.tag,
                        right - tw,
                        0,
                        right,
                        h,
                        ModernDraw.scaleAlpha(p.muted(), 0.8f),
                        TextAlign.RIGHT,
                        false);
                right -= tw + 4;
            }
            int color = on || sel ? p.text() : (hl ? ModernDraw.mix(p.muted(), p.text(), 0.5f) : p.muted());
            ModernDraw.text(context, this.name, 14, 0, right, h, color, TextAlign.LEFT, hl);
        }
    }
}
