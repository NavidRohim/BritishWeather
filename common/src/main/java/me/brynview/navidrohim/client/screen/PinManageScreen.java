package me.brynview.navidrohim.client.screen;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
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
import net.minecraft.client.gui.layouts.LayoutSettings;
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

    private static final Tooltip colourDropperTooltip = Tooltip.create(Component.translatable("br.pin_creation_screen.colour_picker.tooltip"));
    private static final Tooltip shouldShowDistSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_show_dist.selected.tooltip"));
    private static final Tooltip shouldShowDistUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_show_dist.unselected.tooltip"));
    private static final Tooltip shouldDisappearWhenOOBTooltipSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_disappear_oob.selected.tooltip"));
    private static final Tooltip shouldDisappearWhenOOBTooltipUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_disappear_oob.unselected.tooltip"));
    private static final Tooltip showBackgroundSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.show_background.tooltip"));
    private static final Tooltip showBackgroundUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.show_background.unselected.tooltip."));

    private int highlightedColourOnPicker = 0;
    private int highlightedColourPickerR = 0;
    private int highlightedColourPickerG = 0;
    private int highlightedColourPickerB = 0;

    private boolean persistent = true;
    private boolean disappearWhenOOB = true;
    private boolean showDistance = false;
    private boolean showBackground = true;

    Component pressAnyToExit = Component.translatable("br.pin_creation_screen.press_any_to_exit");
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
        super(Component.empty());
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
        LinearLayout personalisationLayout = LinearLayout.vertical().spacing(5);
        LinearLayout timeLayout = LinearLayout.vertical().spacing(5);

        StyledCheckbox showBg = StyledCheckbox.buildCheckbox(CommonComponents.EMPTY, (b) -> showBackground = b.isChecked, showBackgroundSelected, showBackgroundUnselected, true);
        StyledCheckbox isPersistentCheckbox = StyledCheckbox.buildCheckbox(CommonComponents.EMPTY, (b) -> persistent = b.isChecked, selectedPersistent, unselectedPersistent, true);
        StyledCheckbox shouldShowDistCheckbox = StyledCheckbox.buildCheckbox(CommonComponents.EMPTY, (b) -> showDistance = b.isChecked, shouldShowDistSelected, shouldShowDistUnselected, false);
        StyledCheckbox shouldDisappearWhenOOB = StyledCheckbox.buildCheckbox(CommonComponents.EMPTY, (b) -> disappearWhenOOB = b.isChecked, shouldDisappearWhenOOBTooltipSelected, shouldDisappearWhenOOBTooltipUnselected, true);

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

        personalisationLayout.addChild(this.markerBox);
        if (!isEditing) {
            personalisationLayout.addChild(isPersistentCheckbox, LayoutSettings::alignHorizontallyRight);
        }
        personalisationLayout.addChild(shouldShowDistCheckbox, LayoutSettings::alignHorizontallyRight);
        personalisationLayout.addChild(shouldDisappearWhenOOB, LayoutSettings::alignHorizontallyRight);
        personalisationLayout.addChild(showBg, LayoutSettings::alignHorizontallyRight);

        colourLayout.addChild(this.rBox);
        colourLayout.addChild(this.gBox);
        colourLayout.addChild(this.bBox);
        colourLayout.addChild(colourDropperButton);

        timeLayout.addChild(this.integerEditBox);
        timeLayout.addChild(timeUnitCycleButton);
        timeLayout.addChild(timeMethodCycleButton);

        masterLayout.addChild(personalisationLayout);
        if (!isEditing)
        {
            masterLayout.addChild(timeLayout);
        }
        masterLayout.addChild(colourLayout);

        // Position elements within main layout and render

        if (isEditing)
        {
            EntrySettings settings = pin.getSettings();

            markerBox.setValue(pin.getMarker());
            shouldDisappearWhenOOB.setChecked(settings.isDisappearWhenOOB());
            shouldShowDistCheckbox.setChecked(settings.isShowDistance());
            showBg.setChecked(settings.isShowBackground());

            int colour = pin.getHighlightColor();

            rBox.setIntValue(ARGB.red(colour));
            gBox.setIntValue(ARGB.green(colour));
            bBox.setIntValue(ARGB.blue(colour));
        }

        masterLayout.arrangeElements();
        FrameLayout.centerInRectangle(masterLayout, 0, 0, width, mc.getWindow().getGuiScaledHeight() / 4);
        masterLayout.visitWidgets(this::addRenderableWidget);

        FrameLayout.centerInRectangle(enterButton, 0, height / 2, width, mc.getWindow().getGuiScaledHeight() / 4);
        this.addRenderableWidget(enterButton);
    }

    private void create()
    {
        int expiryTime = this.integerEditBox.getIntValue();

        EntrySettings settings = new EntrySettings()
                .setDisappearWhenOOB(disappearWhenOOB)
                .setShowBackground(showBackground)
                .setShowDistance(showDistance);

        if (expiryTime == 0)
        {
            Compass.addEntry(new PinEntry(getMarker(), level, pinPosition, getHighlightColor(), settings));
        } else
        {
            TimeKeeper chosenMethod = timeMethod.createTimeMethod(selectedTimeUnit, expiryTime);
            Compass.addEntry(new TimedPin(getMarker(), level, pinPosition, getHighlightColor(), settings, chosenMethod));
        }
    }

    private void edit()
    {
        pin.getSettings()
            .setShowBackground(showBackground)
            .setShowDistance(showDistance)
            .setDisappearWhenOOB(disappearWhenOOB);

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
        this.rBox.setValue(String.valueOf(r));
        this.gBox.setValue(String.valueOf(g));
        this.bBox.setValue(String.valueOf(b));
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

        compass.render(guiGraphicsExtractor, scaledWidth, height / 2, true);

        int entryDummyX = compass.centerX + 50;
        int colour = getHighlightColor();

        String marker = getMarker();
        int finalMarkerLen = mc.font.width(marker) / 2;
        guiGraphicsExtractor.text(mc.font, marker, entryDummyX - finalMarkerLen, compass.util.getYRowOnCompass(-1), colour, true);

        if (showDistance)
        {
            // 11 represents half the text width of the dummy distance marker. Because the distance marker in this case is final (won't change) there is no reason to calculate the width every time.
            guiGraphicsExtractor.text(mc.font, DUMMY_DISTANCE_MARKER, entryDummyX - 11, compass.util.getYRowOnCompass(1), colour);
        }

        guiGraphicsExtractor.verticalLine(entryDummyX, compass.util.getYRowOnCompass(0) - 2, compass.util.getYRowOnCompass(0.5), colour);
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
