package me.brynview.navidrohim.mixin.server;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.Util;
import me.brynview.navidrohim.server.ai.goals.FleeFromHailGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Is there an event for when an animal / any entity is spawned?
// Then I can add the goal there instead of using mixins.
@Mixin(Animal.class)
public class AnimalAiMixin
{

    @Inject(method = "<init>", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci)
    {
        Mob mob = (Mob) (Object) this;
        mob.getGoalSelector().addGoal(999, new FleeFromHailGoal((PathfinderMob) mob, 1.5));
    }

    @Inject(method = "getWalkTargetValue", at = @At("RETURN"), cancellable = true)
    private void getWalkTargetValue(BlockPos pos, LevelReader level, CallbackInfoReturnable<Float> cir)
    {
        if (Util.isHailing() && !level.canSeeSky(pos))
        {
            cir.setReturnValue(1000F);
        } else if (Util.isHailing() && level.canSeeSky(pos))
        {
            cir.setReturnValue(-1000F);
        }
    }
}
