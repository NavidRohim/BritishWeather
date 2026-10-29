package me.brynview.navidrohim.client.screen.elements;

import me.brynview.navidrohim.client.screen.elements.abs.AbstractIntegerEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ColourEditBox extends AbstractIntegerEditBox
{
    private final int defaultValue;
    private final int outlineColour;

    public ColourEditBox(Font font, Component narration, int defaultValue, String hint, int outlineColour)
    {
        super(font, narration);
        this.setMaxLength(3);
        this.setHint(Component.literal(hint));
        this.setValue(String.valueOf(defaultValue));
        this.setWidth(50);

        this.defaultValue = defaultValue;
        this.outlineColour = outlineColour;
    }

    @Override
    public int getIntValue()
    {
        int v = super.getIntValue();
        return Math.min(v, this.defaultValue);
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics)
    {
        super.extractWidgetRenderState(graphics);
        graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.outlineColour);
    }
}
