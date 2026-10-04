package me.brynview.navidrohim.client.screen;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.TimedPinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.time.GameTime;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.FrameLayout;
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
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class PinCreationScreen extends Screen
{
    private final class TimeUnitSelectionList extends ObjectSelectionList<TimeUnitSelectionList.UnitEntry>
    {
        private final class UnitEntry extends Entry<TimeUnitSelectionList.UnitEntry>
        {
            private final TimeUnit unit;
            private final String unitDisplayName;

            private UnitEntry(TimeUnit unit)
            {
                this.unit = unit;
                this.unitDisplayName = unit.name().toLowerCase();
            }

            @Override
            public Component getNarration()
            {
                return Component.literal(unit.toString());
            }

            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
            {
                guiGraphicsExtractor.centeredText(minecraft.font, this.unitDisplayName, this.getContentXMiddle(), this.getContentYMiddle() - (minecraft.font.lineHeight / 2), ColorHelper.WHITE);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
            {
                PinCreationScreen.this.selectedTimeUnit = this.unit;
                return true;
            }
        }

        public TimeUnitSelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight)
        {
            super(minecraft, width, height, y, itemHeight);

            for (TimeUnit unit : TimeUnit.values())
            {
                this.addEntryToTop(new TimeUnitSelectionList.UnitEntry(unit));
            }
        }

        @Override
        public int getRowWidth()
        {
            return width;
        }
    }

    private final class IntegerEditBox extends EditBox
    {

        public IntegerEditBox(Font font, Component narration)
        {
            super(font, narration);
            this.setHint(Component.literal("Expire in..."));
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

        @Nullable
        public Integer getIntValue()
        {
            return Integer.valueOf(this.getValue());
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

    private final Minecraft mc;
    private final Vec3 pinPosition;
    private final String level;

    Checkbox isPersistentCheckbox;
    TimeUnitSelectionList objectSelectionList;
    TimeUnit selectedTimeUnit = TimeUnit.HOURS;

    IntegerEditBox integerEditBox;

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
         LinearLayout mainLayout = LinearLayout.horizontal().spacing(7);

        // Define main layout elements
        this.isPersistentCheckbox = Checkbox.builder(Component.literal("P"), mc.font).build(); // TODO: Replace with JM style
        this.integerEditBox = new IntegerEditBox(mc.font, Component.empty());
        this.objectSelectionList = new TimeUnitSelectionList(mc, 75, 20, 0, 15);

        // Add elements to main layout
        mainLayout.addChild(this.integerEditBox);
        mainLayout.addChild(isPersistentCheckbox);
        mainLayout.addChild(this.objectSelectionList);

        // Position elements within main layout and render
        mainLayout.arrangeElements();
        FrameLayout.centerInRectangle(mainLayout, 0, 0, this.width, this.height / 4);
        mainLayout.visitWidgets(this::addRenderableWidget);
    }

    private void enterSelection(@Nullable Button ignored)
    {
        boolean isPersistent = isPersistent();
        @Nullable Integer expiryTime = this.integerEditBox.getIntValue();

        if (expiryTime == null)
        {
            Compass.addEntry(new PinEntry(pinPosition, getMarker(), level, isPersistent, getColour()));
        } else {
            Compass.addEntry(new TimedPinEntry(pinPosition, level, getMarker(), this.selectedTimeUnit, expiryTime, isPersistent));
        }

        this.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (event.keycode() == 13) // Enter key
        {
            enterSelection(null);
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
        Compass.getInstance().render(graphics, mc.getWindow().getGuiScaledWidth(), a, this, this.height / 2);
    }

    public String getMarker()
    {
        return "P";
    }

    public boolean isPersistent()
    {
        return this.isPersistentCheckbox.selected();
    }

    public int getColour()
    {
        return ColorHelper.WHITE;
    }
}
