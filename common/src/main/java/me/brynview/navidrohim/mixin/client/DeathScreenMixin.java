package me.brynview.navidrohim.mixin.client;

import me.brynview.navidrohim.client.gui.compass.HudCompass;
import me.brynview.navidrohim.client.gui.compass.providers.builtin.DeathpointTimedProvider;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin
{
    @Inject(method = "<init>", at = @At("TAIL"))
    private void handlePlayerDeath(Component causeOfDeath, boolean hardcore, LocalPlayer player, CallbackInfo ci)
    {
        HudCompass.addProvider(new DeathpointTimedProvider(player));
    }
}
