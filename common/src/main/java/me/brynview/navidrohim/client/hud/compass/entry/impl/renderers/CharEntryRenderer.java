package me.brynview.navidrohim.client.hud.compass.entry.impl.renderers;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.client.hud.compass.entry.entry.DefaultEntry;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class CharEntryRenderer extends EntryRenderer
{
    private final FormattedCharSequence charSeq;
    private final int charWidth;

    public CharEntryRenderer(DefaultEntry owner)
    {
        super(owner);
        char charToRender = owner.getMarker().charAt(0);
        this.charSeq = GeneralUtils.getFormattedChar(charToRender);
        this.charWidth = mc.font.width(charSeq);
    }

    @Override
    protected void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, int colour)
    {
        int renderX = compass.util.centerWidthAroundX(charWidth, screenX);
        int renderY = compass.util.getYRowOnCompass(screenYRow - 1) - 1;

        boolean shadow = true;
        if (renderState == RenderState.FOCUSED && BritishWeather.getConfig().shouldShowPinBackground())
        {
            shadow = false;
            guiGraphicsExtractor.fill(renderX - 2, renderY - 2, renderX + charWidth + 2, renderY + mc.font.lineHeight, owner.highlightBgColour);
            guiGraphicsExtractor.outline(renderX - 3, renderY - 3, charWidth + 5, mc.font.lineHeight + 4, colour);
        }

        guiGraphicsExtractor.text(mc.font, charSeq, renderX, renderY, colour, shadow);
    }
}
