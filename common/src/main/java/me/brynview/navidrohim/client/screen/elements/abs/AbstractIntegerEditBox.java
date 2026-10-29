package me.brynview.navidrohim.client.screen.elements.abs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

public class AbstractIntegerEditBox extends EditBox
{
    public AbstractIntegerEditBox(Font font, Component narration)
    {
        super(font, narration);
    }

    @Override
    public boolean charTyped(CharacterEvent event)
    {
        if (StringUtils.isNumeric(event.codepointAsString()))
        {
            super.charTyped(event);
        }
        return false;
    }


    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (!event.isPaste() || (event.isPaste() && StringUtils.isNumeric(Minecraft.getInstance().keyboardHandler.getClipboard())))
        {
            return super.keyPressed(event);
        }
        return false;
    }

    public int getIntValue()
    {
        String value = getValue();
        if (value.isEmpty())
        {
            return 0;
        }
        return Integer.parseInt(this.getValue());
    }

    public void setIntValue(int value)
    {
        this.setValue(String.valueOf(value));
    }
}
