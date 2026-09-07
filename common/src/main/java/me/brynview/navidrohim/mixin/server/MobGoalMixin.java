package me.brynview.navidrohim.mixin.server;

import me.brynview.navidrohim.server.ai.goals.FleeFromHailGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.Pig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.include.com.google.gson.annotations.Expose;

// Is there an event for when an animal / any entity is spawned?
// Then I can add the goal there instead of using mixins.
@Mixin(Animal.class)
public class MobGoalMixin
{
    @Inject(method = "<init>", at = @At("TAIL"))
    private void registerGoals(CallbackInfo ci)
    {
        Mob mob = (Mob) (Object) this;
        mob.getGoalSelector().addGoal(999, new FleeFromHailGoal((PathfinderMob) mob, 1.5));
    }
}
