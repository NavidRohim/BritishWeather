package me.brynview.navidrohim.client.screen.elements;

import me.brynview.navidrohim.client.screen.elements.abs.AbstractJMWSButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

public class StyledCheckbox extends AbstractJMWSButton
{

    public boolean isChecked = false;
    protected OnCheckboxPress onPress;
    private final Tooltip checkedTooltip;
    private final Tooltip uncheckedTooltip;

    private static final MutableComponent CHECKMARK = Component.literal("✔");

    protected StyledCheckbox(int x, int y, int width, int height, Component message, OnCheckboxPress onPress, Button.CreateNarration createNarration, Tooltip checkedTooltip, Tooltip uncheckedTooltip) {
        super(x, y, width, height, message, (_) -> {}, createNarration);
        this.onPress = onPress;
        this.checkedTooltip = checkedTooltip;
        this.uncheckedTooltip = uncheckedTooltip;
    }

    public void check()
    {
        isChecked = true;
        this.setTooltip(this.checkedTooltip);
    }

    public void uncheck()
    {
        isChecked = false;
        this.setTooltip(this.uncheckedTooltip);
    }

    public void setChecked(boolean checked)
    {
        this.isChecked = checked;
        this.setTooltip(isChecked ? this.checkedTooltip : this.uncheckedTooltip);
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        super.onPress(input);

        if (isChecked)
        {
            this.uncheck();
        } else {
            this.check();
        }

        this.onPress.onPress(this);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor)
    {
        super.extractContents(guiGraphicsExtractor);
        if (isChecked)
        {
            guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, CHECKMARK, this.getX() + (width / 2), this.getY() + (height / 2) - 3, 0xFFFFFFFF);
        }

        guiGraphicsExtractor.text(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width + 3, this.getY() + 2, 0xFFFFFFFF);
    }

    public static StyledCheckbox buildCheckbox(Component label, OnCheckboxPress onPress, Tooltip checkedTooltip, Tooltip uncheckedTooltip, boolean defaultState) {
        StyledCheckbox box = new StyledCheckbox(0, 0, 15, 15, label, onPress, Button.DEFAULT_NARRATION, checkedTooltip, uncheckedTooltip);

        if (defaultState)
        {
            box.check();
            box.setTooltip(checkedTooltip);
        } else {
            box.setTooltip(uncheckedTooltip);
        }

        return box;
    }

    public interface OnCheckboxPress
    {
        void onPress(StyledCheckbox checkboxPressed);
    }
}
