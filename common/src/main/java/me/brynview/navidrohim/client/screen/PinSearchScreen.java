package me.brynview.navidrohim.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.List;

import static me.brynview.navidrohim.util.ColorHelper.FADED_WHITE;
import static me.brynview.navidrohim.util.ColorHelper.WHITE;

public class PinSearchScreen extends Screen
{
    private final class StyledEditBox extends EditBox
    {
        private int renderColour = FADED_WHITE;
        public StyledEditBox(Font font, Component narration)
        {
            super(font, narration);
            this.setTextColor(FADED_WHITE);
            this.setTextColorUneditable(FADED_WHITE);
            this.setTextShadow(false);
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics)
        {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
            graphics.outline(getX(), getY(), width, height, renderColour);
            super.extractWidgetRenderState(graphics);
        }

        public void setRenderColour(int colour)
        {
            this.renderColour = colour;
            this.setTextColor(colour);
        }

        @Override
        public boolean isBordered()
        {
            return false;
        }
    }

    private static final Component PIN_SEARCH_NARRATION = Component.translatable("br.search_screen.pin_search.narration");
    private static final double FAT_TEXT_SCALE = 1.8D;
    private static final double SCALED_LINE_HEIGHT = Constants.LINE_HEIGHT * FAT_TEXT_SCALE;
    private static final int INFO_BOX_WIDTH = 130;
    private static final int INFO_BOX_MINIMISED_WIDTH_HEIGHT = 3;

    private static final int SLOT_SPACING = 2; // 2px for top and bottom. Total is 4px.
    private static final int SLOT_HEIGHT = Constants.LINE_HEIGHT + (SLOT_SPACING * 2);
    //private static final int AVAILABLE_SLOTS = INFO_BOX_WIDTH_HEIGHT / SLOT_HEIGHT;

    private StyledEditBox searchBox;
    private @Nullable AbstractEntry selectedEntryOld;
    private @Nullable AbstractEntry selectedEntry;

    private final LocalPlayer localPlayer;
    private final HashMap<String, AbstractEntry> entriesMatching = new HashMap<>();
    private final ResourceKey<Level> levelKey;

    private List<Map.Entry<String, AbstractEntry>> hints = List.of();
    private int entryIndex = 0;

    private static final float SCROLL_EASE_SPEED = 18F; // Higher = snappier. ~18 settles in roughly 200ms.
    private float animatedIndex = 0.0F; // Smoothly follows entryIndex for the scroll animation.
    private long nowOld = -1L;


    public PinSearchScreen(ResourceKey<Level> level, LocalPlayer localPlayer)
    {
        super(Component.literal("Search"));
        this.levelKey = level;
        this.localPlayer = localPlayer;
    }

    @Override
    protected void init()
    {
        searchBox = new StyledEditBox(font, PIN_SEARCH_NARRATION);
        searchBox.setWidth(200);
        searchBox.setResponder(this::typedString);
        searchBox.setX(10);
        searchBox.setY(height / 2);

        this.addRenderableWidget(searchBox);
        this.setInitialFocus(searchBox);
    }

    private void setYRot(float yRot)
    {
        localPlayer.setYRot(yRot);
        if (localPlayer.getVehicle() != null)
        {
            localPlayer.getVehicle().setYRot(yRot);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (event.key() == InputConstants.KEY_RETURN) // enter
        {
            closeWithResult();
            return true;
        } else if (event.key() == InputConstants.KEY_DOWN)
        {
            return scrollEntry(1);
        } else if (event.key() == InputConstants.KEY_UP)
        {
            return scrollEntry(-1);
        } else {
            return super.keyPressed(event);
        }
    }

    public boolean scrollEntry(int incBy)
    {
        if (entriesMatching.isEmpty())
        {
            return false;
        }

        entryIndex = Math.clamp(entryIndex + incBy, 0, hints.size() - 1);
        selectedEntryOld = selectedEntry;
        selectedEntry = hints.get(entryIndex).getValue();

        Constants.LOG.info(String.valueOf(selectedEntry.getRenderer().angleYawFromHeading));
        //setYRot(selectedEntry.getRenderer().angleYawFromHeading);
        //localPlayer.setXRot(selectedEntry.getRenderer().angleTiltFromHeading);

        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY)
    {
        if (scrollY >= 0.45)
        {
            return scrollEntry(1);
        } else if  (scrollY <= -0.45)
        {
            return scrollEntry(-1);
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private void reset()
    {
        selectedEntry = null;
        selectedEntryOld = null;
        entriesMatching.clear();
        searchBox.setSuggestion("");
        entryIndex = 0;
        animatedIndex = 0.0F; // New results, so snap rather than animate from the old list.
        hints = List.of();
    }

    private void typedString(String string)
    {
        reset();

        if (string.isEmpty())
        {
            return;
        }

        for (AbstractEntry allEntry : EntryManager.getEntriesInDimension(levelKey))
        {
            if (allEntry.getMarker().toLowerCase().contains(string.toLowerCase()))
            {
                String marker = allEntry.getMarker();
                if (entriesMatching.containsKey(marker))
                {
                    marker += " (%s)".formatted(allEntry.getReadablePosition());
                }

                entriesMatching.put(marker, allEntry);
            }
        }

        hints = new ArrayList<>(entriesMatching.entrySet());
        scrollEntry(entryIndex);
    }

    private void closeWithResult()
    {
        if (selectedEntry != null && selectedEntry instanceof PinEntry trackablePin)
        {
            Compass.getInstance().setTrackedPin(trackablePin);
        }
        this.onClose();
    }


    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics)
    {
        int xForHint = searchBox.getX();
        int baseHintY = searchBox.getY() + searchBox.getHeight();
        float selectedEntryAnimationProgress = 1;

        updateScrollAnimation();

        if (!entriesMatching.isEmpty() && selectedEntry != null)
        {
            // Calculate if pin is behind player by using dot method.
            for (int i = 0; i < hints.size(); i++)
            {
                Map.Entry<String, AbstractEntry> hint = hints.get(i);
                AbstractEntry entry = hint.getValue();

                // How far from selected slot `i` is
                float offset = i - animatedIndex;
                // 1 when selected (highlighted entry) 0 is normal size. Any number in between is the animation
                float selectedAnimationProgress = Math.clamp(1.0F - Math.abs(offset), 0.0F, 1.0F);
                float y = yForSlotOffset(offset, baseHintY);
                float scale = Mth.lerp(selectedAnimationProgress, 1.0F, (float) FAT_TEXT_SCALE);

                // Selected entry is white, do colour change animation to white
                int animationColour = ARGB.srgbLerp(selectedAnimationProgress, ColorHelper.shiftColourToOpacity(hint.getValue().highlightColor, 190), hint.getValue().highlightColor);

                if (hint.getValue() == selectedEntry)
                {
                    int fadeColourBox = ARGB.srgbLerp(selectedAnimationProgress, selectedEntryOld != null ? selectedEntryOld.highlightColor : searchBox.renderColour, selectedEntry.highlightColor);
                    searchBox.setRenderColour(fadeColourBox);
                    selectedEntryAnimationProgress = selectedAnimationProgress;
                }

                drawScaledText(graphics, hint.getKey(), xForHint, y, scale, animationColour);

                boolean pinAheadOfPlayer = entry.getPosition()
                        .subtract(localPlayer.position())
                        .dot(localPlayer.getViewVector(0)) > 0;

                if (pinAheadOfPlayer && entry != selectedEntry)
                {
                    drawPinInfo(graphics, selectedAnimationProgress, entry);
                } else if (!pinAheadOfPlayer && entry == selectedEntry)
                {
                    setYRot(selectedEntry.getRenderer().angleYawFromHeading);
                }

                if (BritishWeather.getConfig().debug())
                {
                    graphics.centeredText(minecraft.font, "%.2f".formatted(selectedAnimationProgress), xForHint + searchBox.getWidth(), (int) y, WHITE);
                }
            }

            drawPinInfo(graphics, selectedEntryAnimationProgress, selectedEntry);
        } else {
            graphics.text(minecraft.font, Component.literal("Type pin name. Up / down arrow keys to scroll through results."), searchBox.getX(), baseHintY + 5, ColorHelper.FADED_WHITE);
            searchBox.setRenderColour(FADED_WHITE);
        }
        super.extractRenderState(graphics);
    }

    private void drawPinInfo(@NotNull GuiGraphicsExtractor graphics, float animationProgress, AbstractEntry entry)
    {
        Vec3 pinPos = entry.getPosition(graphics.partialTicks());
        Vec3 ndc = minecraft.gameRenderer.projectPointToScreen(pinPos);

        int x = (int) ((ndc.x + 1.0) * 0.5 * width);
        int y = (int) ((1.0 - ndc.y) * 0.5 * height);
        int finalInfoBoxHeight = (SLOT_HEIGHT * 2) + SLOT_SPACING;

        if ( (x < 0 || y < 0 || x + INFO_BOX_WIDTH > width || y + finalInfoBoxHeight > height) && entry == selectedEntry)
        {
            setYRot(selectedEntry.getRenderer().angleYawFromHeading);
        }

        int lerpInfoBoxHeight = (int) Mth.lerp(animationProgress, INFO_BOX_MINIMISED_WIDTH_HEIGHT, (SLOT_HEIGHT * 2) + SLOT_SPACING);
        int lerpInfoBoxWidth = (int) Mth.lerp(animationProgress, INFO_BOX_MINIMISED_WIDTH_HEIGHT, INFO_BOX_WIDTH);

        graphics.fill(x, y, x + lerpInfoBoxWidth, y + lerpInfoBoxHeight, entry.getRenderer().backgroundColour);
        graphics.outline(x, y, lerpInfoBoxWidth, lerpInfoBoxHeight, entry.highlightColor);

        int markerWidth = minecraft.font.width(entry.getMarker());

        if (entry == selectedEntry)
        {
            int markerY = y - (minecraft.font.lineHeight + EntryRenderer.LEFT_RIGHT_MARGINS);
            int textColour = ARGB.srgbLerp(animationProgress, ColorHelper.INVISIBLE, entry.highlightColor);
            // 4 and 6 is padding.

            graphics.fill(x, markerY, x + markerWidth + EntryRenderer.HORIZONTAL_BACKGROUND_MARGINS, markerY + minecraft.font.lineHeight + EntryRenderer.VERTICAL_BACKGROUND_MARGINS, entry.getRenderer().backgroundColour);
            graphics.outline(x, markerY, markerWidth + EntryRenderer.HORIZONTAL_BACKGROUND_MARGINS, minecraft.font.lineHeight + EntryRenderer.VERTICAL_BACKGROUND_MARGINS, textColour);
            graphics.text(minecraft.font, entry.getMarker(), x + EntryRenderer.LEFT_RIGHT_MARGINS, y - minecraft.font.lineHeight, textColour, false);

            int baseSlotYPos = y + (SLOT_SPACING * 2);
            graphics.text(minecraft.font, "XYZ: %s".formatted(selectedEntry.getReadablePosition()), x + 4, baseSlotYPos, textColour, false);
            graphics.text(minecraft.font, "Distance: %s blocks".formatted(MathHelper.getDistance(selectedEntry.getPosition(), localPlayer.position())), x + 4, baseSlotYPos + SLOT_HEIGHT, textColour, false);
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics)
    {

    }

    /**
     * Eases {@link #animatedIndex} towards {@link #entryIndex}. Uses exponential smoothing so it is
     * framerate independent and naturally re-targets if the user scrolls again mid-animation.
     */
    private void updateScrollAnimation()
    {
        // Slow transition animatedIndex to entryIndex

        long now = Util.getNanos();
        float deltaSeconds = nowOld < 0 ? 0.0F : (now - nowOld) / Constants.NANOS_TO_SECONDS_MAGIC;
        nowOld = now;

        float remaining = entryIndex - animatedIndex;
        // If we are close enough to the index, just set it
        if (Math.abs(remaining) < 0.01F)
        {
            animatedIndex = entryIndex;
            return;
        }

        animatedIndex += remaining * (1.0F - (float) Math.exp(-SCROLL_EASE_SPEED * deltaSeconds));
    }

    /**
     * Y position for an entry {@code offset} slots away from the selected slot.
     * Integer offsets match the static layout exactly. Fractional offsets interpolate between neighbouring slots.
     */
    private float yForSlotOffset(float offset, int baseHintY)
    {
        int lower = Mth.floor(offset);
        float progress = offset - lower;
        return Mth.lerp(progress, yForSlot(lower, baseHintY), yForSlot(lower + 1, baseHintY));
    }

    private float yForSlot(int slot, int baseHintY)
    {
        float selectedY = Mth.floor(searchBox.getY() - SCALED_LINE_HEIGHT);
        if (slot < 0) // Above the selected entry.
        {
            return selectedY - 2 + Constants.LINE_HEIGHT * slot;
        } else if (slot == 0) // The selected entry.
        {
            return selectedY;
        }
        return baseHintY + Constants.LINE_HEIGHT * slot; // Below the search box.
    }

    private void drawScaledText(GuiGraphicsExtractor graphics, String text, int x, float y, float scale, int colour)
    {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);
        graphics.text(minecraft.font, text, 0, 0, colour);
        pose.popMatrix();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
