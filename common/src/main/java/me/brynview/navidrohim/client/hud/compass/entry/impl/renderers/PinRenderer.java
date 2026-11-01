package me.brynview.navidrohim.client.hud.compass.entry.impl.renderers;

import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.PinEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.particle.EndRodParticle;

public class PinRenderer extends EntryRenderer<PinEntry>
{
    public PinRenderer(PinEntry owner)
    {
        super(owner);
    }

    @Override
    protected boolean drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, int colour, int x, int y, int markerWidth)
    {
        if (owner.shouldShowBackground())
        {
            return super.drawBackground(guiGraphicsExtractor, colour, x, y, markerWidth);
        }
        return true;
    }
}
