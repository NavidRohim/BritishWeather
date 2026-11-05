package me.brynview.navidrohim.client.screen;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.TrackedJourney;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.TimedPin;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;
import me.brynview.navidrohim.client.screen.elements.StyledCheckbox;
import me.brynview.navidrohim.client.screen.elements.ColourEditBox;
import me.brynview.navidrohim.client.screen.elements.abs.AbstractIntegerEditBox;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class PinManageScreen extends Screen
{
    private final Vec3 pinPosition;
    private final String level;

    protected boolean isSelectingColour = false;
    protected int frameCounter = 0;
    protected final Minecraft mc;

    private static final int PICKER_BG = ColorHelper.rgb(110, 110, 110, 255);
    private static final int PICKER_GLASS_SIZE = 18;
    private static final int PICKER_GLASS_OFFSET = 22;
    private static final int PICKER_COLOUR_SPACING = 4;
    private static final String DUMMY_DISTANCE_MARKER = "0m -";
    private static final int DUMMY_MARKER_WIDTH_HALF = 11;

    private static final Component pressAnyToExit = Component.translatable("br.pin_creation_screen.press_any_to_exit");
    private static final Component title = Component.literal("Pin manager");

    private static final Tooltip colourDropperTooltip = Tooltip.create(Component.translatable("br.pin_creation_screen.colour_picker.tooltip"));
    private static final Tooltip shouldShowDistSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_show_dist.selected.tooltip"));
    private static final Tooltip shouldShowDistUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_show_dist.unselected.tooltip"));
    private static final Tooltip shouldDisappearWhenOOBTooltipSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_disappear_oob.selected.tooltip"));
    private static final Tooltip shouldDisappearWhenOOBTooltipUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_disappear_oob.unselected.tooltip"));
    private static final Tooltip showBackgroundSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.show_background.tooltip"));
    private static final Tooltip showBackgroundUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.show_background.unselected.tooltip"));
    private static final Tooltip showShadowSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.show_shadow.tooltip"));
    private static final Tooltip showShadowUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.show_shadow.unselected.tooltip"));

    private int highlightedColourOnPicker = 0;
    private int highlightedColourPickerR = 0;
    private int highlightedColourPickerG = 0;
    private int highlightedColourPickerB = 0;

    private boolean persistent = EntrySettings.PERSISTENT_DEFAULT;
    private boolean disappearWhenOOB = EntrySettings.SHOULD_DISAPPEAR_OOB;
    private boolean showDistance = EntrySettings.SHOW_DISTANCE_DEFAULT;
    private boolean showBackground = EntrySettings.SHOW_BACKGROUND_DEFAULT;
    private boolean showShadow = EntrySettings.SHOW_SHADOWS_DEFAULT;

    GpuBuffer pixelBuffer;
    RenderTarget mainRenderTarget;
    CommandEncoder encoder;

    private EditBox markerBox;

    private ColourEditBox rBox;
    private ColourEditBox gBox;
    private ColourEditBox bBox;

    PinEntry pin;
    boolean isEditing;

    IntegerEditBox integerEditBox;
    TimeKeeper.TimeMethods timeMethod = TimeKeeper.TimeMethods.GAMETIME;
    TimeUnit selectedTimeUnit = TimeUnit.SECONDS;

    Tooltip selectedPersistent = Tooltip.create(Component.translatable("br.pin_creation_screen.persistent_checkbox.selected.tooltip"));
    Tooltip unselectedPersistent = Tooltip.create(Component.translatable("br.pin_creation_screen.persistent_checkbox.unselected.tooltip"));

    public PinManageScreen(@NotNull LocalPlayer player, @Nullable PinEntry editablePin)
    {
        super(title);
        this.pinPosition = player.position();
        this.level = GeneralUtils.getDimensionFromPlayer(player);
        this.pin = editablePin;
        this.isEditing = editablePin != null;
        this.mc = Minecraft.getInstance();

        GpuDevice device = RenderSystem.getDevice();

        mainRenderTarget = mc.gameRenderer.mainRenderTarget();
        encoder = device.createCommandEncoder();
        pixelBuffer = device.createBuffer(
                () -> "Colour picker pixel readback",
                GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
                4
        );
    }

    @Override
    protected void init()
    {
        LinearLayout masterLayout = LinearLayout.horizontal().spacing(7);
        LinearLayout colourLayout = LinearLayout.vertical().spacing(5);
        GridLayout personalisationLayout = new GridLayout();
        LinearLayout central = LinearLayout.vertical().spacing(5);

        this.markerBox = new EditBox(mc.font, Component.empty());
        this.markerBox.setValue("Pin label..");

        this.rBox = new ColourEditBox(mc.font, Component.empty(), 255, "Red..", ColorHelper.RED);
        this.gBox = new ColourEditBox(mc.font, Component.empty(), 255, "Green..", ColorHelper.GREEN);
        this.bBox = new ColourEditBox(mc.font, Component.empty(), 255, "Blue..", ColorHelper.BLUE);
        Button colourDropperButton = Button.builder(Component.literal("\uD83D\uDCA7"), (_) -> this.toggleDropper())
                .width(50)
                .tooltip(colourDropperTooltip)
                .build();

        Button enterButton = Button.builder(CommonComponents.GUI_DONE, (_) -> enterSelection()).build();

        // Define main layout elements
        this.integerEditBox = new IntegerEditBox(mc.font, Component.empty());

        CycleButton<TimeUnit> timeUnitCycleButton = CycleButton.builder((timeUnit -> Component.literal(timeUnit.name().toLowerCase())), selectedTimeUnit)
                .withValues(TimeUnit.values())
                .create(Component.literal("Time"), (_, newTimeUnit) -> this.selectedTimeUnit = newTimeUnit);

        CycleButton<TimeKeeper.TimeMethods> timeMethodCycleButton = CycleButton.builder(tu -> Component.literal(tu.getDisplayName()), timeMethod)
                .withValues(TimeKeeper.TimeMethods.values())
                .withTooltip(TimeKeeper.TimeMethods::getDescription)
                .create(Component.literal("Method"), (_, newTimeMethod) -> timeMethod = newTimeMethod);

        StyledCheckbox showBg = StyledCheckbox.buildCheckbox((b) -> showBackground = b.isChecked, showBackgroundSelected, showBackgroundUnselected, EntrySettings.SHOW_BACKGROUND_DEFAULT);
        StyledCheckbox isPersistentCheckbox = StyledCheckbox.buildCheckbox((b) -> persistent = b.isChecked, selectedPersistent, unselectedPersistent, EntrySettings.PERSISTENT_DEFAULT);
        StyledCheckbox shouldShowDistCheckbox = StyledCheckbox.buildCheckbox((b) -> showDistance = b.isChecked, shouldShowDistSelected, shouldShowDistUnselected, EntrySettings.SHOW_DISTANCE_DEFAULT);
        StyledCheckbox shouldDisappearWhenOOB = StyledCheckbox.buildCheckbox((b) -> disappearWhenOOB = b.isChecked, shouldDisappearWhenOOBTooltipSelected, shouldDisappearWhenOOBTooltipUnselected, EntrySettings.SHOULD_DISAPPEAR_OOB);
        StyledCheckbox shouldShowShadow = StyledCheckbox.buildCheckbox((b) -> showShadow = b.isChecked, showShadowSelected, showShadowUnselected, EntrySettings.SHOW_SHADOWS_DEFAULT);

        if (!isEditing) {
            personalisationLayout.addChild(isPersistentCheckbox, 0, 0, d -> d.padding(2));
        }
        personalisationLayout.addChild(shouldShowDistCheckbox, 1, 0, d -> d.padding(2));
        personalisationLayout.addChild(shouldDisappearWhenOOB, 2, 0, d -> d.padding(2));
        personalisationLayout.addChild(showBg, 3, 0, d -> d.padding(2));
        personalisationLayout.addChild(shouldShowShadow, 4, 0, d -> d.padding(2));

        colourLayout.addChild(this.rBox);
        colourLayout.addChild(this.gBox);
        colourLayout.addChild(this.bBox);
        colourLayout.addChild(colourDropperButton);

        central.addChild(this.markerBox);
        if (!isEditing)
        {
            central.addChild(this.integerEditBox);
            central.addChild(timeUnitCycleButton);
            central.addChild(timeMethodCycleButton);
        }
        central.addChild(enterButton);

        masterLayout.addChild(personalisationLayout);
        masterLayout.addChild(central);
        masterLayout.addChild(colourLayout);

        // Position elements within main layout and render

        if (isEditing)
        {
            EntrySettings settings = pin.getSettings();

            markerBox.setValue(pin.getMarker());
            shouldDisappearWhenOOB.setChecked(settings.shouldDisappearWhenOOB());
            shouldShowDistCheckbox.setChecked(settings.shouldShowDistance());
            showBg.setChecked(settings.shouldShowBackground());
            shouldShowShadow.setChecked(settings.shouldShowShadow());

            showShadow = settings.shouldShowShadow();
            disappearWhenOOB = settings.shouldDisappearWhenOOB();
            showBackground = settings.shouldShowBackground();
            persistent = settings.isPersistent();
            showDistance = settings.shouldShowDistance();

            int colour = pin.getHighlightColor();

            rBox.setIntValue(ARGB.red(colour));
            gBox.setIntValue(ARGB.green(colour));
            bBox.setIntValue(ARGB.blue(colour));
        }

        masterLayout.arrangeElements();
        FrameLayout.centerInRectangle(masterLayout, 0, height / 4, width, mc.getWindow().getGuiScaledHeight() / 4);
        masterLayout.visitWidgets(this::addRenderableWidget);
    }

    private void create()
    {
        int expiryTime = this.integerEditBox.getIntValue();

        EntrySettings settings = new EntrySettings()
                .setDisappearWhenOOB(disappearWhenOOB)
                .setShowBackground(showBackground)
                .setShowDistance(showDistance)
                .setPersistent(persistent)
                .setShadow(showShadow);

        if (expiryTime == 0)
        {
            Compass.addEntry(new PinEntry(getMarker(), level, pinPosition, getHighlightColor(), settings, TrackedJourney.empty())); // no
        } else
        {
            TimeKeeper chosenMethod = timeMethod.createTimeMethod(selectedTimeUnit, expiryTime);
            Compass.addEntry(new TimedPin(getMarker(), level, pinPosition, getHighlightColor(), settings, TrackedJourney.empty(), chosenMethod)); // no
        }
    }

    private void edit()
    {
        pin.getSettings()
            .setShowBackground(showBackground)
            .setShowDistance(showDistance)
            .setDisappearWhenOOB(disappearWhenOOB)
            .setPersistent(persistent)
            .setShadow(showShadow);

        pin.setMarker(getMarker());
        pin.setHighlightColor(getHighlightColor());
    }

    private void enterSelection()
    {
        if (!isEditing)
        {
            create();
        } else {
            edit();
        }

        EntryManager.save();
        this.onClose();
    }

    private void enableColourDropper(boolean enabled)
    {
        isSelectingColour = enabled;
        frameCounter = 0;
    }

    private void toggleDropper()
    {
        enableColourDropper(!isSelectingColour);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics)
    {
        if (!isSelectingColour)
        {
            super.extractRenderState(graphics);
        } else
        {
            renderColourDropper(graphics);
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics)
    {
        if (!isSelectingColour)
        {
            super.extractBackground(graphics);
            renderDummy(graphics);
        }
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event)
    {
        if (isSelectingColour)
        {
            enableColourDropper(false);
            colourPicked(highlightedColourPickerR, highlightedColourPickerG, highlightedColourPickerB);
            return true;
        } else if (event.keycode() == 13)
        {
            enterSelection();
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick)
    {
        boolean didAction = super.mouseClicked(event, doubleClick);
        if (isSelectingColour && frameCounter >= 10)
        {
            enableColourDropper(false);
            colourPicked(highlightedColourPickerR, highlightedColourPickerG, highlightedColourPickerB);
        }

        return didAction;
    }

    private void colourPicked(int r, int g, int b)
    {
        this.rBox.setIntValue(r);
        this.gBox.setIntValue(g);
        this.bBox.setIntValue(b);
        frameCounter = 0;
    }

    private int getHighlightColor()
    {
        return ColorHelper.rgb(rBox.getIntValue(), gBox.getIntValue(), bBox.getIntValue(), 255);
    }

    private String getMarker()
    {
        return this.markerBox.getValue();
    }

    private void renderColourDropper(GuiGraphicsExtractor guiGraphicsExtractor)
    {
        frameCounter++;
        guiGraphicsExtractor.centeredText(font, pressAnyToExit, width / 2, 25, ColorHelper.WHITE);
        int mX = guiGraphicsExtractor.mouseX();
        int mY = guiGraphicsExtractor.mouseY();

        if (frameCounter % 10 == 0)
        {
            int width = mc.getWindow().getWidth();
            int height = mc.getWindow().getHeight();

            int x = mX * width / mc.getWindow().getGuiScaledWidth();
            int y = height - 1 - mY * height / mc.getWindow().getGuiScaledHeight();

            // Check `FrontendCommandEncoder` for actual param names
            encoder.copyTextureToBuffer(
                    mainRenderTarget.getColorTexture(),
                    pixelBuffer,
                    0, 0, // offset, mip
                    x, y, // x, y of screen
                    1, 1 // width, height of area to capture relative to x, y
            );

            readPixel(pixelBuffer);
        }

        int mXOutline = mX - PICKER_GLASS_OFFSET;
        int mYOutline = mY - PICKER_GLASS_OFFSET;

        guiGraphicsExtractor.requestCursor(CursorTypes.CROSSHAIR);

        guiGraphicsExtractor.fill(mXOutline, mYOutline, mXOutline + PICKER_GLASS_SIZE, mYOutline + PICKER_GLASS_SIZE, PICKER_BG);
        guiGraphicsExtractor.outline(mXOutline, mYOutline, PICKER_GLASS_SIZE, PICKER_GLASS_SIZE, ColorHelper.COMPASS_BG_COLOR);
        guiGraphicsExtractor.fill(mXOutline + PICKER_COLOUR_SPACING, mYOutline + PICKER_COLOUR_SPACING, mXOutline + (PICKER_GLASS_SIZE - PICKER_COLOUR_SPACING), mYOutline + (PICKER_GLASS_SIZE - PICKER_COLOUR_SPACING), highlightedColourOnPicker);
    }

    private void readPixel(GpuBuffer pixelBuffer)
    {
        try (GpuBufferSlice.MappedView view = pixelBuffer.map(0L, 4L, true, false))
        {

            ByteBuffer data = view.data();
            highlightedColourPickerR = Byte.toUnsignedInt(data.get(0));
            highlightedColourPickerG = Byte.toUnsignedInt(data.get(1));
            highlightedColourPickerB = Byte.toUnsignedInt(data.get(2));
            highlightedColourOnPicker = ColorHelper.rgb(highlightedColourPickerR, highlightedColourPickerG, highlightedColourPickerB, 255);
        }
    }

    private void renderDummy(GuiGraphicsExtractor guiGraphicsExtractor)
    {
        Minecraft mc = Minecraft.getInstance();
        Compass compass = Compass.getInstance();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();

        compass.render(guiGraphicsExtractor, scaledWidth, BritishWeather.getConfig().getCompassY(), true);

        int entryDummyX = compass.context.centerX;
        int colour = getHighlightColor();

        String marker = getMarker();
        int finalMarkerLen = mc.font.width(marker) / 2;
        int textCorner = entryDummyX - finalMarkerLen;

        // 11 represents half the text width of the dummy distance marker. Because the distance marker in this case is final (won't change) there is no reason to calculate the width every time.
        int dummyXWidthHalf = entryDummyX - DUMMY_MARKER_WIDTH_HALF;
        int backgroundY = compass.context.getYRowOnCompass(-1) - 1;

        if (showBackground)
        {
            int x = (textCorner + finalMarkerLen * 2) + 2;
            guiGraphicsExtractor.fill(textCorner - 2, backgroundY - 2, x, backgroundY + mc.font.lineHeight, ColorHelper.getBackgroundColour(colour));
            guiGraphicsExtractor.outline(textCorner - 3, backgroundY - 3, (finalMarkerLen * 2) + 6, mc.font.lineHeight + 4, colour);
        }

        if (showDistance)
        {
            guiGraphicsExtractor.text(mc.font, DUMMY_DISTANCE_MARKER, dummyXWidthHalf, compass.context.getYRowOnCompass(1), colour, showShadow);
        }

        guiGraphicsExtractor.text(mc.font, marker, textCorner, backgroundY, colour, showShadow);
        guiGraphicsExtractor.verticalLine(entryDummyX, compass.context.getYRowOnCompass(0) - 2, compass.context.getYRowOnCompass(0.5), colour);
    }

    private final class IntegerEditBox extends AbstractIntegerEditBox
    {

        public IntegerEditBox(Font font, Component narration)
        {
            super(font, narration);
            this.setHint(Component.literal("Expire in..."));
        }

        @Override
        public void setFocused(boolean focused)
        {
            super.setFocused(focused);
            if (focused)
            {
                this.setSuggestion(" %s".formatted(selectedTimeUnit.name().toLowerCase()));
            } else
            {
                this.setSuggestion("");
            }
        }
    }
}
