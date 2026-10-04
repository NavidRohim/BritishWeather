package me.brynview.navidrohim.mixin.client;

import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.builtin.DeathEntry;
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

    /*
    There seems to be no event listener on Fabric for a LocalPlayer's death, so do it here.
    Shouldn't cause issues as I don't believe this code is changed often
     */
    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void handlePlayerDeath(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir)
    {
        Compass.addEntry(new DeathEntry(player.position(), player.level().dimension().identifier().toString()));
    }
}
