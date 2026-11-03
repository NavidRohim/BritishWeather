package me.brynview.navidrohim.client.screen;

import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class SearchPin extends Screen
{
    private static final String UNKNOWN = " ?";

    private @Nullable AbstractEntry entry;

    public SearchPin()
    {
        super(CommonComponents.EMPTY);
    }

    @Override
    protected void init()
    {
        EditBox pinNameBox = new EditBox(minecraft.font, CommonComponents.EMPTY);
        pinNameBox.setResponder(str -> this.typedString(str, pinNameBox));
        pinNameBox.setRectangle(150, 20, (width / 2) - 75, (height / 2) - 10);

        this.addRenderableWidget(pinNameBox);
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

    private void typedString(String string, EditBox pinNameBox)
    {
        pinNameBox.setSuggestion(UNKNOWN);
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

        String hint = " " + String.join(", ", entryHashMap.keySet());
        pinNameBox.setSuggestion(hint);

        Optional<Map.Entry<String, AbstractEntry>> possibleEntry = entryHashMap.entrySet().stream().findFirst();
        possibleEntry.ifPresent(stringAbstractEntryEntry -> entry = stringAbstractEntryEntry.getValue());
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
    public boolean isPauseScreen()
    {
        return false;
    }
}
