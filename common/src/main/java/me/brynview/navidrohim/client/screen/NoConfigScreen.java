package me.brynview.navidrohim.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class NoConfigScreen extends Screen
{
    private static final Component title = Component.literal("No config");
    private static final Component errorComponent = Component.translatable("br.no_config_screen.no_config");
    private final Screen parentScreen;

    protected NoConfigScreen(Minecraft minecraft, Font font, Screen parent)
    {
        super(minecraft, font, title);
        this.parentScreen = parent;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics)
    {
        super.extractRenderState(graphics);
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