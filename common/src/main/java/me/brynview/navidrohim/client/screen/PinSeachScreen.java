package me.brynview.navidrohim.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class PinSeachScreen extends Screen
{
    private final class StyledEditBox extends EditBox
    {
        public StyledEditBox(Font font, Component narration)
        {
            super(font, narration);
            this.setTextColor(ColorHelper.WHITE);
            this.setTextColorUneditable(ColorHelper.WHITE);
            this.setTextShadow(false);
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics)
        {
            graphics.outline(getX(), getY(), width, height, ColorHelper.WHITE);
            graphics.fill(getX(), getY(), getX() + width, getY() + height, minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
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

    private EditBox searchBox;
    private @Nullable AbstractEntry selectedEntry;
    private final HashMap<String, AbstractEntry> entriesMatching = new HashMap<>();
    private final ResourceKey<Level> levelKey;

    private List<Map.Entry<String, AbstractEntry>> hints = List.of();
    private int entryIndex = 0;
    private int maxEntryIndex = 1;

    public PinSeachScreen(ResourceKey<Level> level)
    {
        super(Component.literal("Search"));
        this.levelKey = level;
    }

    @Override
    protected void init()
    {
        LinearLayout master = LinearLayout.horizontal().spacing(3);

        searchBox = new StyledEditBox(font, PIN_SEARCH_NARRATION);
        searchBox.setWidth(200);
        searchBox.setResponder(this::typedString);

        SpriteIconButton doneButton = SpriteIconButton.builder(CommonComponents.GUI_CONTINUE, (_) -> this.closeWithResult(), true)
                .bounds(0, 0, 20, 20)
                .sprite(Identifier.withDefaultNamespace("icon/search"), 15, 15)
                .build();

        master.addChild(searchBox);
        master.addChild(doneButton);
        master.arrangeElements();

        FrameLayout.centerInRectangle(master, getRectangle());
        master.visitWidgets(this::addRenderableWidget);

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

        entryIndex = Math.clamp(entryIndex + incBy, 0, Math.min(hints.size() - 1, maxEntryIndex - 1));
        selectedEntry = hints.get(entryIndex).getValue();
        searchBox.setSuggestion(" " + selectedEntry.getMarker());

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
        maxEntryIndex = 1;
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
            minecraft.player.setYRot(selectedEntry.getRenderer().angleFromHeading);
        }
        this.onClose();
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics)
    {
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics)
    {
        super.extractRenderState(graphics);
        int xForHint = searchBox.getX();
        int baseHintY = searchBox.getY() + searchBox.getHeight();

        if (!entriesMatching.isEmpty())
        {
            int i = 0;
            for (Map.Entry<String, AbstractEntry> stringAbstractEntryEntry : hints)
            {
                int renderY = baseHintY + (minecraft.font.lineHeight * (i + 1));
                if (renderY + maxEntryIndex >= height)
                {
                    graphics.text(minecraft.font, "> Be more specific!", xForHint, renderY, ColorHelper.WHITE);
                    return;
                }

                maxEntryIndex = i + 1;
                int renderColour = ColorHelper.shiftColourToOpacity(stringAbstractEntryEntry.getValue().highlightColor, 210);
                String displayableKey = stringAbstractEntryEntry.getKey();

                if (stringAbstractEntryEntry.getValue() == selectedEntry)
                {
                    renderColour = ColorHelper.WHITE;
                    displayableKey = "→ " + displayableKey;
                }

                graphics.text(minecraft.font, displayableKey, xForHint, renderY, renderColour);
                i++;
            }
        } else {
            graphics.centeredText(minecraft.font, Component.literal("Type pin name. Use up / down arrow keys to scroll through results."), xForHint + searchBox.getWidth() / 2, baseHintY + 5, ColorHelper.FADED_WHITE);
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
