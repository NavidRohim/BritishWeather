package me.brynview.navidrohim.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.util.ColorHelper;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.brynview.navidrohim.util.ColorHelper.WHITE;

public class PinSearchScreen extends Screen
{
    private final class StyledEditBox extends EditBox
    {
        public StyledEditBox(Font font, Component narration)
        {
            super(font, narration);
            this.setTextColor(WHITE);
            this.setTextColorUneditable(WHITE);
            this.setTextShadow(false);
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics)
        {
            int colour = selectedEntry != null ? selectedEntry.highlightColor : WHITE;
            graphics.outline(getX(), getY(), width, height, colour);
            graphics.fill(getX(), getY(), getX() + width, getY() + height, minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
            this.setTextColor(colour);

            super.extractWidgetRenderState(graphics);
        }

        @Override
        public boolean isBordered()
        {
            return false;
        }
    }

    private static final Component PIN_SEARCH_NARRATION = Component.translatable("br.search_screen.pin_search.narration");
    private static final List<String> EXAMPLES = List.of("Base", "Village", "Cave", "Farm", "Ravine", "Stronghold", "Home", "Forest", "Dungeon", "Camp");
    private static final double FAT_TEXT_SCALE = 1.8D;
    private static final double SCALED_LINE_HEIGHT = Constants.LINE_HEIGHT * FAT_TEXT_SCALE;
    private static final float NANOS_TO_SECONDS_MAGIC =  1_000_000_000.0F;

    private EditBox searchBox;
    private @Nullable AbstractEntry selectedEntry;

    private final LocalPlayer localPlayer;
    private final HashMap<String, AbstractEntry> entriesMatching = new HashMap<>();
    private final ResourceKey<Level> levelKey;

    private List<Map.Entry<String, AbstractEntry>> hints = List.of();
    private int entryIndex = 0;

    private static final float SCROLL_EASE_SPEED = 18.0F; // Higher = snappier. ~18 settles in roughly 200ms.
    private float animatedIndex = 0.0F; // Smoothly follows entryIndex for the scroll animation.
    private long lastAnimationNanos = -1L;
    private int animationColour = ColorHelper.WHITE;


    public PinSearchScreen(ResourceKey<Level> level, LocalPlayer localPlayer)
    {
        super(Component.literal("Search"));
        this.levelKey = level;
        this.localPlayer = localPlayer;
    }

    @Override
    protected void init()
    {
        //LinearLayout master = LinearLayout.horizontal().spacing(3);

        searchBox = new StyledEditBox(font, PIN_SEARCH_NARRATION);
        searchBox.setWidth(200);
        searchBox.setResponder(this::typedString);
        searchBox.setX(10);
        searchBox.setY(height / 2);

        /*
        SpriteIconButton doneButton = SpriteIconButton.builder(CommonComponents.GUI_CONTINUE, (_) -> this.closeWithResult(), true)
                .bounds(0, 0, 20, 20)
                .sprite(Identifier.withDefaultNamespace("icon/search"), 15, 15)
                .build();

        //master.addChild(searchBox);
        //master.addChild(doneButton);
        //master.arrangeElements();
        //master.visitWidgets(this::addRenderableWidget);*/
        this.addRenderableWidget(searchBox);
        this.setInitialFocus(searchBox);
        setExample();
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
        selectedEntry = hints.get(entryIndex).getValue();
        
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
        entriesMatching.clear();
        searchBox.setSuggestion("");
        entryIndex = 0;
        animatedIndex = 0.0F; // New results, so snap rather than animate from the old list.
        hints = List.of();
    }

    private void setExample()
    {
        Random randomSrc = new Random(); // Doing random operations in Java is so shit compared to Python.
        searchBox.setSuggestion(" Maybe: " + EXAMPLES.get(randomSrc.nextInt(0, EXAMPLES.size())));
    }

    private void typedString(String string)
    {
        reset();

        if (string.isEmpty())
        {
            setExample();
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
        if (selectedEntry != null)
        {
            localPlayer.setYRot(selectedEntry.getRenderer().angleFromHeading);
        }
        this.onClose();
    }


    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics)
    {
        int xForHint = searchBox.getX();
        int baseHintY = searchBox.getY() + searchBox.getHeight();

        updateScrollAnimation();
        
        if (!entriesMatching.isEmpty())
        {
            for (int i = 0; i < hints.size(); i++)
            {
                Map.Entry<String, AbstractEntry> hint = hints.get(i);

                // How far from selected slot `i` is
                float offset = i - animatedIndex;
                // 1 when selected (highlighted entry) 0 is normal size. Any number in between is the animation
                float selectedness = Math.clamp(1.0F - Math.abs(offset), 0.0F, 1.0F);

                float y = yForSlotOffset(offset, baseHintY);
                float scale = Mth.lerp(selectedness, 1.0F, (float) FAT_TEXT_SCALE);

                // Selected entry is white, do colour change animation to white
                animationColour = ARGB.srgbLerp(selectedness, ColorHelper.shiftColourToOpacity(hint.getValue().highlightColor, 190), hint.getValue().highlightColor);
                drawScaledText(graphics, hint.getKey(), xForHint, y, scale, animationColour);
            }
        } else {
            graphics.text(minecraft.font, Component.literal("Type pin name. Use up / down arrow keys to scroll through results."), searchBox.getX(), baseHintY + 5, ColorHelper.FADED_WHITE);
        }
        super.extractRenderState(graphics);
    }

    /**
     * Eases {@link #animatedIndex} towards {@link #entryIndex}. Uses exponential smoothing so it is
     * framerate independent and naturally re-targets if the user scrolls again mid-animation.
     */
    private void updateScrollAnimation()
    {
        // Slow transition animatedIndex to entryIndex

        long now = Util.getNanos();
        float deltaSeconds = lastAnimationNanos < 0 ? 0.0F : (now - lastAnimationNanos) / NANOS_TO_SECONDS_MAGIC;
        lastAnimationNanos = now;

        float remaining = entryIndex - animatedIndex;
        // If we are close enough to the index, just set it
        if (Math.abs(remaining) < 0.001F)
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
