package me.brynview.navidrohim.client.screen;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.DefaultPinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.TimedPinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.TimeMethod;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.screen.elements.BRCheckbox;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class PinCreationScreen extends Screen
{
    private static class BaseIntegerEditBox extends EditBox
    {
        public BaseIntegerEditBox(Font font, Component narration)
        {
            super(font, narration);
        }

        @Override
        public boolean charTyped(CharacterEvent event)
        {
            if (StringUtils.isNumeric(event.codepointAsString()))
            {
                super.charTyped(event);
            }
            return false;
        }


        @Override
        public boolean keyPressed(KeyEvent event)
        {
            if (!event.isPaste() || (event.isPaste() && StringUtils.isNumeric(Minecraft.getInstance().keyboardHandler.getClipboard())))
            {
                return super.keyPressed(event);
            }
            return false;
        }

        public int getIntValue()
        {
            String value = getValue();
            if (value.isEmpty())
            {
                return 0;
            }
            return Integer.parseInt(this.getValue());
        }
    }

    private final class IntegerEditBox extends BaseIntegerEditBox
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
                this.setSuggestion(" %s".formatted(PinCreationScreen.this.selectedTimeUnit.name().toLowerCase()));
            } else {
                this.setSuggestion("");
            }
        }
    }

    private static class ColourEditBox extends BaseIntegerEditBox
    {
        private final int defaultValue;
        private final int outlineColour;

        public ColourEditBox(Font font, Component narration, int defaultValue, String hint, int outlineColour)
        {
            super(font, narration);
            this.setMaxLength(3);
            this.setHint(Component.literal(hint));
            this.setValue(String.valueOf(defaultValue));
            this.setWidth(50);

            this.defaultValue = defaultValue;
            this.outlineColour = outlineColour;
        }

        @Override
        public int getIntValue()
        {
            int v = super.getIntValue();
            return Math.min(v, this.defaultValue);
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
        {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.outlineColour);
        }
    }

    private final Minecraft mc;
    private final Vec3 pinPosition;
    private final String level;

    LinearLayout masterLayout;

    BRCheckbox isPersistentCheckbox;
    BRCheckbox shouldShowDistCheckbox;
    BRCheckbox shouldDisappearWhenOOB;

    TimeMethod.TimeMethods timeMethod = TimeMethod.TimeMethods.GAMETIME;
    TimeUnit selectedTimeUnit = TimeUnit.SECONDS;
    EditBox markerBox;

    IntegerEditBox integerEditBox;
    ColourEditBox rBox;
    ColourEditBox gBox;
    ColourEditBox bBox;
    Button colourDropperButton;

    Tooltip selectedPersistent = Tooltip.create(Component.translatable("br.pin_creation_screen.persistent_checkbox.selected.tooltip"));
    Tooltip unselectedPersistent = Tooltip.create(Component.translatable("br.pin_creation_screen.persistent_checkbox.unselected.tooltip"));

    Tooltip shouldShowDistSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_show_dist.selected.tooltip"));
    Tooltip shouldShowDistUnSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_show_dist.unselected.tooltip"));

    Tooltip shouldDisappearWhenOOBTooltipSelected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_disappear_oob.selected.tooltip"));
    Tooltip shouldDisappearWhenOOBTooltipUnselected = Tooltip.create(Component.translatable("br.pin_creation_screen.should_disappear_oob.unselected.tooltip"));

    Tooltip colourDropperTooltip = Tooltip.create(Component.translatable("br.pin_creation_screen.colour_picker.tooltip"));

    Component pressAnyToExit = Component.translatable("br.pin_creation_screen.press_any_to_exit");

    private static final Component EMPTY = Component.empty();
    private static final String DUMMY_DISTANCE_MARKER = "0m -";

    // Colour dropper values
    private static final int PICKER_BG = ColorHelper.rgb(110, 110, 110, 255);
    private static final int PICKER_GLASS_SIZE = 18;
    private static final int PICKER_GLASS_OFFSET = 22;
    private static final int PICKER_COLOUR_SPACING = 4;

    private boolean isSelectingColour = false;
    private int frameCounter = 0;

    private int highlightedColourOnPicker = 0;
    private int highlightedColourPickerR = 0;
    private int highlightedColourPickerG = 0;
    private int highlightedColourPickerB = 0;

    GpuBuffer pixelBuffer;
    RenderTarget mainRenderTarget;
    CommandEncoder encoder;

    public PinCreationScreen(Minecraft mc, @NotNull LocalPlayer player)
    {
        super(Component.empty());

        this.mc = mc;
        this.pinPosition = player.position();
        this.level = GeneralUtils.getDimensionFromPlayer(player);

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
        masterLayout = LinearLayout.horizontal().spacing(7);

        LinearLayout colourLayout = LinearLayout.vertical().spacing(5);
        LinearLayout timeLayout = LinearLayout.vertical().spacing(5);
        LinearLayout personalisationLayout = LinearLayout.vertical().spacing(5);

        // Define main layout elements
        this.isPersistentCheckbox = BRCheckbox.buildCheckbox(EMPTY, (_) -> {}, selectedPersistent, unselectedPersistent);
        this.shouldShowDistCheckbox = BRCheckbox.buildCheckbox(EMPTY, (_) -> {}, shouldShowDistSelected, shouldShowDistUnSelected);
        this.shouldDisappearWhenOOB = BRCheckbox.buildCheckbox(EMPTY, (_) -> {}, shouldDisappearWhenOOBTooltipSelected, shouldDisappearWhenOOBTooltipUnselected);

        this.integerEditBox = new IntegerEditBox(mc.font, Component.empty());

        this.markerBox = new EditBox(mc.font, Component.empty());
        this.markerBox.setValue("Pin label..");

        this.rBox = new ColourEditBox(mc.font, Component.empty(), 255, "Red..", ColorHelper.RED);
        this.gBox = new ColourEditBox(mc.font, Component.empty(), 255, "Green..", ColorHelper.GREEN);
        this.bBox = new ColourEditBox(mc.font, Component.empty(), 255, "Blue..", ColorHelper.BLUE);
        this.colourDropperButton = Button.builder(Component.literal("\uD83D\uDCA7"), (_) -> this.enableColourDropper(!isSelectingColour))
                .width(50)
                .tooltip(colourDropperTooltip)
                .build();

        CycleButton<TimeUnit> timeUnitCycleButton = CycleButton.builder((timeUnit -> Component.literal(timeUnit.name().toLowerCase())), selectedTimeUnit)
                .withValues(TimeUnit.values())
                .create(Component.literal("Time"), (_, newTimeUnit) -> this.selectedTimeUnit = newTimeUnit);

        CycleButton<TimeMethod.TimeMethods> timeMethodCycleButton = CycleButton.builder(tu -> Component.literal(tu.getDisplayName()), timeMethod)
                .withValues(TimeMethod.TimeMethods.values())
                .withTooltip(TimeMethod.TimeMethods::getDescription)
                .create(Component.literal("Method"), (_, newTimeMethod) -> timeMethod = newTimeMethod);

        // Add elements to main layout
        personalisationLayout.addChild(this.markerBox);

        personalisationLayout.addChild(this.isPersistentCheckbox, LayoutSettings::alignHorizontallyRight);
        personalisationLayout.addChild(this.shouldShowDistCheckbox, LayoutSettings::alignHorizontallyRight);
        personalisationLayout.addChild(this.shouldDisappearWhenOOB, LayoutSettings::alignHorizontallyRight);

        timeLayout.addChild(this.integerEditBox);
        timeLayout.addChild(timeUnitCycleButton);
        timeLayout.addChild(timeMethodCycleButton);

        colourLayout.addChild(this.rBox);
        colourLayout.addChild(this.gBox);
        colourLayout.addChild(this.bBox);
        colourLayout.addChild(this.colourDropperButton);

        masterLayout.addChild(personalisationLayout);
        masterLayout.addChild(timeLayout);
        masterLayout.addChild(colourLayout);

        // Position elements within main layout and render
        masterLayout.arrangeElements();
        FrameLayout.centerInRectangle(masterLayout, 0, 0, width, mc.getWindow().getGuiScaledWidth() / 4);
        masterLayout.visitWidgets(this::addRenderableWidget);
    }

    private void enableColourDropper(boolean enabled)
    {
        isSelectingColour = enabled;
        frameCounter = 0;
    }

    private void enterSelection()
    {
        boolean isPersistent = isPersistent();
        int expiryTime = this.integerEditBox.getIntValue();
        if (expiryTime == 0)
        {
            Compass.addEntry(new DefaultPinEntry(pinPosition, level, getMarker(), isPersistent, getHighlightColor(), shouldShowDistance(), shouldDisappearWhenOutsideView()));
        } else {
            TimeMethod chosenMethod = timeMethod.createTimeMethod(selectedTimeUnit, expiryTime);
            Compass.addEntry(new TimedPinEntry(pinPosition, level, getMarker(), chosenMethod, isPersistent, getHighlightColor(), shouldShowDistance(), shouldDisappearWhenOutsideView()));
        }

        this.onClose();
        EntryManager.save();
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (isSelectingColour)
        {
            enableColourDropper(false);
            return true;
        } else if (event.keycode() == 13) // Enter key
        {
            enterSelection();
            return true;
        } else {
            return super.keyPressed(event);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        if (!isSelectingColour)
        {
            super.extractRenderState(graphics, mouseX, mouseY, a);
            renderDummy(graphics, a);
        } else {
            renderColourDropper(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        if (!isSelectingColour)
        {
            super.extractBackground(graphics, mouseX, mouseY, a);
        }
    }

    public String getMarker()
    {
        return this.markerBox.getValue();
    }

    public boolean isPersistent()
    {
        return this.isPersistentCheckbox.isChecked;
    }

    public int getHighlightColor()
    {
        return ColorHelper.rgb(rBox.getIntValue(), gBox.getIntValue(), bBox.getIntValue(),  255);
    }

    public boolean shouldShowDistance()
    {
        return this.shouldShowDistCheckbox.isChecked;
    }

    public boolean shouldDisappearWhenOutsideView()
    {
        return this.shouldDisappearWhenOOB.isChecked;
    }

    @Override
    public boolean isFocused()
    {
        return true;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        boolean didAction = super.mouseClicked(event, doubleClick);
        if (isSelectingColour && frameCounter >= 10)
        {
            enableColourDropper(false);
            this.rBox.setValue(String.valueOf(highlightedColourPickerR));
            this.gBox.setValue(String.valueOf(highlightedColourPickerG));
            this.bBox.setValue(String.valueOf(highlightedColourPickerB));
        }

        return didAction;
    }


    private void renderColourDropper(GuiGraphicsExtractor guiGraphicsExtractor, int mX, int mY)
    {
        frameCounter++;
        guiGraphicsExtractor.centeredText(font, pressAnyToExit, width / 2, 25, ColorHelper.WHITE);

        if (frameCounter % 10 == 0)
        {
            int width = mc.getWindow().getWidth();
            int height = mc.getWindow().getHeight();

            int x = mX * width / mc.getWindow().getGuiScaledWidth();
            int y = height - 1 - mY * height / mc.getWindow().getGuiScaledHeight();

            encoder.copyTextureToBuffer(
                    mainRenderTarget.getColorTexture(),
                    pixelBuffer,
                    0L,
                    () -> readPixel(pixelBuffer),
                    0,
                    x,
                    y,
                    1,
                    1
            );
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
        try (GpuBufferSlice.MappedView view = pixelBuffer.map(0L, 4L, true, false)) {

            ByteBuffer data = view.data();
            highlightedColourPickerR = Byte.toUnsignedInt(data.get(0));
            highlightedColourPickerG = Byte.toUnsignedInt(data.get(1));
            highlightedColourPickerB = Byte.toUnsignedInt(data.get(2));
            highlightedColourOnPicker = ColorHelper.rgb(highlightedColourPickerR, highlightedColourPickerG, highlightedColourPickerB, 255);
        }
    }

    private void renderDummy(GuiGraphicsExtractor guiGraphicsExtractor, float ticks)
    {
        Minecraft mc = Minecraft.getInstance();
        Compass compass = Compass.getInstance();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();

        compass.render(guiGraphicsExtractor, scaledWidth, ticks, height, true);

        int entryDummyX = compass.compassX + 50;
        int colour = getHighlightColor();

        String marker = getMarker();
        int finalMarkerLen = mc.font.width(marker) / 2;
        FontHelper.draw(mc, guiGraphicsExtractor, marker, entryDummyX - finalMarkerLen, compass.util.getYRowOnCompass(-1), colour, true, FontHelper.TextType.NONE);

        if (shouldShowDistance())
        {
            // 11 represents half the text width of the dummy distance marker. Because the distance marker in this case is final (won't change) there is no reason to calculate the width every time.
            FontHelper.draw(mc, guiGraphicsExtractor, DUMMY_DISTANCE_MARKER, entryDummyX - 11, compass.util.getYRowOnCompass(1), colour, FontHelper.TextType.LABEL);
        }

        FontHelper.draw(mc, guiGraphicsExtractor, DefaultEntry.THINGY, entryDummyX, compass.util.getYRowOnCompass(0), colour, FontHelper.TextType.LABEL);
    }
}
