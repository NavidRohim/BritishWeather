package me.brynview.navidrohim.client.screen.elements;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.GeneralUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public abstract class AbstractJMWSButton extends Button
{
    private static final Identifier BUTTON_BG = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "button_bg");
    protected boolean isHeld = false;
    private boolean isEnabled = true;

    protected AbstractJMWSButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (isEnabled)
        {
            isHeld = true;
            super.onClick(event, doubleClick);
        }
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent event) {
        isHeld = false;
        super.onRelease(event);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {

        renderBackground(guiGraphicsExtractor);
        renderBorder(guiGraphicsExtractor);

        if (isHeld)
        {
            GeneralUtils.renderFillForElement(guiGraphicsExtractor, this, 0x7F404040);
        } else if (this.isHovered())
        {
            GeneralUtils.renderFillForElement(guiGraphicsExtractor, this, 0x40909090);
        }

        if (!isEnabled)
        {
            GeneralUtils.renderFillForElement(guiGraphicsExtractor, this, 0x40929292);
        }
    }

    public void renderBorder(@NonNull GuiGraphicsExtractor graphics)
    {
        graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.BORDER_COLOUR);
    }

    public void renderBackground(@NonNull GuiGraphicsExtractor graphics) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_BG, this.getX(), this.getY(), width, height);
    }
}
