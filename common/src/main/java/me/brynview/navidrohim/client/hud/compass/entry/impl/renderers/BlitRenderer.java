package me.brynview.navidrohim.client.hud.compass.entry.impl.renderers;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.resources.Identifier;

public class BlitRenderer extends EntryRenderer<AbstractEntry>
{
    private Identifier blitTexture;
    private int blitWidth;
    private int blitHeight;

    private boolean isSprite = true;
    private int renderColour = ColorHelper.WHITE;

    public BlitRenderer(AbstractEntry owner, int blitWidth, int blitHeight)
    {
        super(owner);
        this.blitTexture = Identifier.parse(owner.getMarker());
        this.blitWidth = blitWidth;
        this.blitHeight = blitHeight;
    }

    public BlitRenderer(AbstractEntry owner, int blitWidth, int blitHeight, int colour)
    {
        super(owner);
        this.blitTexture = Identifier.parse(owner.getMarker());
        this.blitWidth = blitWidth;
        this.blitHeight = blitHeight;
        this.isSprite = false;
        this.renderColour = colour;
    }

    @Override
    public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext, int x, float yRow)
    {
        int renderX = MathHelper.centerWidthAroundX(blitWidth, x);
        int renderY = compassRenderContext.getYOnRowCentered(yRow, blitHeight) - 1;

        if (isSprite)
        {
            guiGraphicsExtractor.blitSprite(GuiPipeline.OPAQUE_TEXTURED_BACKGROUND, blitTexture, renderX, renderY, blitWidth, blitHeight);
        } else {
            guiGraphicsExtractor.blit(GuiPipeline.OPAQUE_TEXTURED_BACKGROUND, blitTexture, renderX, renderY, 0, 0, blitWidth, blitHeight, blitWidth, blitHeight, renderColour);
        }
    }
}
