package me.brynview.navidrohim.client.hud.compass.entry.impl.renderers;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class CharEntryRenderer extends EntryRenderer<AbstractEntry>
{
    private final FormattedCharSequence charSeq;
    private final int charWidth;

    public CharEntryRenderer(AbstractEntry owner)
    {
        super(owner);
        char charToRender = owner.getMarker().charAt(0);
        this.charSeq = GeneralUtils.getFormattedChar(charToRender);
        this.charWidth = mc.font.width(charSeq);
    }

    @Override
    public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, int x, int yRow)
    {
        int renderX = compass.util.centerWidthAroundX(charWidth, x);
        int renderY = compass.util.getYRowOnCompass(yRow) - 1;

        boolean shadow = true;
        if (shouldTakePriority() && BritishWeather.getConfig().shouldShowPinBackground())
        {
            shadow = false;
            guiGraphicsExtractor.fill(renderX - 2, renderY - 2, renderX + charWidth + 2, renderY + mc.font.lineHeight, owner.highlightBgColour);
            guiGraphicsExtractor.outline(renderX - 3, renderY - 3, charWidth + 5, mc.font.lineHeight + 4, renderColourForState);
        }

        guiGraphicsExtractor.text(mc.font, charSeq, renderX, renderY, renderColourForState, shadow);
    }
}
