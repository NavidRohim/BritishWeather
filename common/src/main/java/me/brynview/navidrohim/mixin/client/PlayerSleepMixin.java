package me.brynview.navidrohim.mixin.client;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PlayerSleepMixin
{

    @Inject(method = "stopSleeping", at = @At(value = "HEAD"))
    private void onSyncedDataUpdated(CallbackInfo ci)
    {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.level().isClientSide() && livingEntity instanceof LocalPlayer player && player == Minecraft.getInstance().player && player.isSleepingLongEnough())
        {
            Compass.wakeEvent(livingEntity);
        }
    }
}
