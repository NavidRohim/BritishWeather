package me.brynview.navidrohim.client.screen;

import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class SearchPin extends Screen
{
    private static final Component PIN_SEARCH_NARRATION = Component.translatable("br.search_screen.pin_search.narration");
    private static final String UNKNOWN = " ?";

    private EditBox searchBox;
    private @Nullable AbstractEntry entry;
    private String hint = "";

    public SearchPin()
    {
        super(Component.literal("Search"));
    }

    @Override
    protected void init()
    {
        LinearLayout master = LinearLayout.horizontal().spacing(3);

        searchBox = new EditBox(minecraft.font, PIN_SEARCH_NARRATION);

        searchBox.setResponder(this::typedString);

        Button doneButton = Button.builder(Component.literal("E"), (_) -> this.closeWithResult()).bounds(0, 0, 20, 20).build();

        master.addChild(doneButton);
        master.addChild(searchBox);

        master.arrangeElements();
        FrameLayout.centerInRectangle(master, getRectangle());
        master.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (event.keycode() == 13) // enter
        {
            closeWithResult();
            return true;
        }
        return super.keyPressed(event);
    }

    private void typedString(String string)
    {
        entry = null;

        if (string.isEmpty())
        {
            return;
        }

        HashMap<String, AbstractEntry> entryHashMap = new HashMap<>();

        for (AbstractEntry allEntry : EntryManager.getAllEntries())
        {
            if (allEntry.getMarker().toLowerCase().contains(string.toLowerCase()))
            {
                entryHashMap.put(allEntry.getMarker(), allEntry);
            }
        }

        hint = String.join(", ", entryHashMap.keySet());

        Optional<Map.Entry<String, AbstractEntry>> possibleEntry = entryHashMap.entrySet().stream().findFirst();
        possibleEntry.ifPresent(stringAbstractEntryEntry ->
        {
            entry = stringAbstractEntryEntry.getValue();
            searchBox.setSuggestion(" " + stringAbstractEntryEntry.getKey());
        });

    }

    private void closeWithResult()
    {
        if (entry != null)
        {
            minecraft.player.setYRot(entry.getRenderer().angleFromHeading);
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
        graphics.centeredText(minecraft.font, hint, searchBox.getX() + searchBox.getWidth() / 2, searchBox.getY() + searchBox.getHeight() + 5, -8355712);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
