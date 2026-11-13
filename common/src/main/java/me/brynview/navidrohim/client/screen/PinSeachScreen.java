package me.brynview.navidrohim.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
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

    private Set<Map.Entry<String, AbstractEntry>> hints = Set.of();
    private int entryIndex = 0;

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
            return selectPossibleEntry(1);
        } else if (event.key() == InputConstants.KEY_UP)
        {
            return selectPossibleEntry(-1);
        } else {
            return super.keyPressed(event);
        }
    }

    private boolean selectPossibleEntry(int incBy)
    {
        if (entriesMatching.isEmpty())
        {
            return false;
        }

        entryIndex = Math.clamp(entryIndex + incBy, 0, entriesMatching.size() - 1);
        selectedEntry = entriesMatching.entrySet().stream().toList().get(entryIndex).getValue();
        searchBox.setSuggestion(" " + selectedEntry.getMarker());

        return true;
    }

    private void reset()
    {
        selectedEntry = null;
        entriesMatching.clear();
        searchBox.setSuggestion("");
        entryIndex = 0;
        hints = Set.of();
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
                entriesMatching.put(allEntry.getMarker(), allEntry);
            }
        }

        hints = entriesMatching.entrySet();
        selectPossibleEntry(entryIndex);
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
                i++;
                int renderColour = ColorHelper.shiftColourToOpacity(stringAbstractEntryEntry.getValue().highlightColor, 210);
                String displayableKey = stringAbstractEntryEntry.getKey();

                if (stringAbstractEntryEntry.getValue() == selectedEntry)
                {
                    renderColour = ColorHelper.WHITE;
                    displayableKey = "→ " + displayableKey;
                }

                graphics.text(minecraft.font, displayableKey, xForHint, baseHintY + (minecraft.font.lineHeight * i), renderColour);
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
