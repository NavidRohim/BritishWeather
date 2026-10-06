package me.brynview.navidrohim.client.screen;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.TimedPinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.TimeMethod;
import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.EntryRenderable;
import me.brynview.navidrohim.client.screen.elements.BRCheckbox;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class PinCreationScreen extends Screen implements EntryRenderable
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

        public Integer getIntValue()
        {
            String value = getValue();
            if (value.isEmpty())
            {
                return 0;
            }
            return Integer.valueOf(this.getValue());
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
        private int defaultValue;
        private int outlineColour;

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
        public @Nullable Integer getIntValue()
        {
            Integer v = super.getIntValue();
            return Math.min(v, this.defaultValue);
        }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
        {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.outlineColour);
        }
    }

    private final Minecraft mc;
    private final Vec3 pinPosition;
    private final String level;

    BRCheckbox isPersistentCheckbox;
    TimeMethod.TimeMethods timeMethod = TimeMethod.TimeMethods.GAMETIME;
    TimeUnit selectedTimeUnit = TimeUnit.SECONDS;
    EditBox markerBox;

    IntegerEditBox integerEditBox;
    ColourEditBox rBox;
    ColourEditBox gBox;
    ColourEditBox bBox;

    Tooltip selectedPersistent = Tooltip.create(Component.translatable("br.pin_creation_screen.persistent_checkbox.selected.tooltip"));
    Tooltip unselectedPersistent = Tooltip.create(Component.translatable("br.pin_creation_screen.persistent_checkbox.unselected.tooltip"));
    private static final Component EMPTY = Component.empty();

    public PinCreationScreen(Minecraft mc, @NotNull LocalPlayer player)
    {
        super(Component.empty());

        this.mc = mc;
        this.pinPosition = player.position();
        this.level = GeneralUtils.getDimensionFromPlayer(player);


    }

    @Override
    protected void init()
    {
        LinearLayout masterLayout = LinearLayout.horizontal().spacing(7);

        LinearLayout colourLayout = LinearLayout.vertical().spacing(5);
        LinearLayout timeLayout = LinearLayout.vertical().spacing(5);
        LinearLayout generalLayout = LinearLayout.vertical().spacing(5);

        // Define main layout elements
        this.isPersistentCheckbox = BRCheckbox.buildCheckbox(EMPTY, (_) -> {}, selectedPersistent, unselectedPersistent);

        this.integerEditBox = new IntegerEditBox(mc.font, Component.empty());

        this.markerBox = new EditBox(mc.font, Component.empty());
        this.markerBox.setValue("Pin label..");

        this.rBox = new ColourEditBox(mc.font, Component.empty(), 255, "Red..", ColorHelper.RED);
        this.gBox = new ColourEditBox(mc.font, Component.empty(), 255, "Green..", ColorHelper.GREEN);
        this.bBox = new ColourEditBox(mc.font, Component.empty(), 255, "Blue..", ColorHelper.BLUE);

        CycleButton<TimeUnit> b = CycleButton.builder((timeUnit -> Component.literal(timeUnit.name().toLowerCase())), selectedTimeUnit)
                .withValues(TimeUnit.values())
                .create(Component.literal("Time"), (_, newTimeUnit) -> this.selectedTimeUnit = newTimeUnit);

        CycleButton<TimeMethod.TimeMethods> timeMethodCycleButton = CycleButton.builder(tu -> Component.literal(tu.getDisplayName()), timeMethod)
                .withValues(TimeMethod.TimeMethods.values())
                .withTooltip(TimeMethod.TimeMethods::getDescription)
                .create(Component.literal("Method"), (_, newTimeMethod) -> timeMethod = newTimeMethod);

        // Add elements to main layout
        generalLayout.addChild(this.markerBox);
        generalLayout.addChild(this.isPersistentCheckbox, LayoutSettings::alignHorizontallyRight);

        timeLayout.addChild(this.integerEditBox);
        timeLayout.addChild(b);
        timeLayout.addChild(timeMethodCycleButton);

        colourLayout.addChild(this.rBox);
        colourLayout.addChild(this.gBox);
        colourLayout.addChild(this.bBox);

        masterLayout.addChild(generalLayout);
        masterLayout.addChild(timeLayout);
        masterLayout.addChild(colourLayout);

        // Position elements within main layout and render
        masterLayout.arrangeElements();
        FrameLayout.centerInRectangle(masterLayout, 0, 0, width, mc.getWindow().getGuiScaledWidth() / 4);
        masterLayout.visitWidgets(this::addRenderableWidget);
    }

    private void enterSelection()
    {
        boolean isPersistent = isPersistent();
        Integer expiryTime = this.integerEditBox.getIntValue();
        if (expiryTime == 0)
        {
            Compass.addEntry(new PinEntry(pinPosition, level, getMarker(), isPersistent, getColour()));
        } else {
            TimeMethod chosenMethod = timeMethod.createTimeMethod(selectedTimeUnit, expiryTime);
            Compass.addEntry(new TimedPinEntry(pinPosition, level, getMarker(), chosenMethod, isPersistent, getColour()));
        }

        this.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (event.keycode() == 13) // Enter key
        {
            enterSelection();
            return true;
        } else {
            return super.keyPressed(event);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        super.extractBackground(graphics, mouseX, mouseY, a);
        Minecraft mc = Minecraft.getInstance();
        Compass.getInstance().render(graphics, mc.getWindow().getGuiScaledWidth(), a, this, this.height);
    }

    public String getMarker()
    {
        return this.markerBox.getValue();
    }

    @Override
    public int getMarkerHalfWidth()
    {
        return mc.font.width(getMarker()) / 2;
    }

    public boolean isPersistent()
    {
        return this.isPersistentCheckbox.isChecked;
    }

    @Override
    public Vec3 getPosition()
    {
        return pinPosition.add(10, 10, 10);
    }

    public int getColour()
    {
        return ColorHelper.rgb(rBox.getIntValue(), gBox.getIntValue(), bBox.getIntValue(),  255);
    }

    @Override
    public int getHighlightColour()
    {
        return 0;
    }
}
