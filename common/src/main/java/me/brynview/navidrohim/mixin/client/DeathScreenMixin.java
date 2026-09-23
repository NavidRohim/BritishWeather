package me.brynview.navidrohim.mixin.client;

import me.brynview.navidrohim.client.gui.compass.HudCompass;
import me.brynview.navidrohim.client.gui.compass.providers.builtin.DeathEntry;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeathScreen.class)
public class DeathScreenMixin
{
    @Shadow
    @Final
    private LocalPlayer player;

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void handlePlayerDeath(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir)
    {
        HudCompass.addProvider(new DeathEntry(player.position()));
    }
}
