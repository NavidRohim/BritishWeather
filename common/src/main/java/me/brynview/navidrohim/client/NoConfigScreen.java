package me.brynview.navidrohim.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class NoConfigScreen extends Screen
{

    private Component errorComponent;
    private Screen parentScreen;

    protected NoConfigScreen(Minecraft minecraft, Font font, Screen parent)
    {
        super(minecraft, font, Component.empty());
        this.parentScreen = parent;
        this.errorComponent = Component.translatable("br.config.no_config");
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(font, errorComponent, width / 2, height / 2, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }

    @Override
    public void onClose()
    {
        minecraft.setScreenAndShow(parentScreen);
    }

    public static Screen fromScreen(Screen screen)
    {
        Minecraft mc = Minecraft.getInstance();
        return new NoConfigScreen(mc, mc.font, screen);
    }
}