package me.brynview.navidrohim.mixin.server;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.util.WeatherUtil;
import me.brynview.navidrohim.server.ai.goals.FleeHailGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Is there an event for when an animal / any entity is spawned?
// Then I can add the goal there instead of using mixins.
@Mixin(Animal.class)
public class AnimalAiMixin
{

    /*
    Inject the flee hail behaviour in the Animal constructor. Seems like the best way.

    Though, this way of injecting the goal feels very bodgy. But I am very happy with the FleeFromHailGoal
    Feels bodgy because of the goal prio (priority) being 999.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectFleeGoal(CallbackInfo ci)
    {
        Mob mob = (Mob) (Object) this;
        // speedModifier set to 1.5 so it appears they hurry up and actually flee for their lives.
        mob.getGoalSelector().addGoal(999, new FleeHailGoal((PathfinderMob) mob, 1.5));
    }

    /*
    getWalkTargetValue determines how much an entity "likes" a block chosen to pathfind.

    For an example, pigs will search for grass and somewhere light I believe. They will be happy to move there.
    But, hostile mobs search for dark areas where sunlight doesn't shine (they will burn)

    Hijack this behaviour for normal Animals (only animals. Mobs will get hit by hail, take damage and not flee)
    So they find cover (darker light levels, not exposed to the sky)
     */
    @Inject(method = "getWalkTargetValue", at = @At("RETURN"), cancellable = true)
    private void getHailWalkTargetValue(BlockPos pos, LevelReader level, CallbackInfoReturnable<Float> cir)
    {
        // Ignore and use vanilla logic if hail doesn't deal damage
        if (!BritishWeather.getConfig().hailShouldDealDamage())
        {
            return;
        }

        if (WeatherUtil.isHailing() && !level.canSeeSky(pos))
        {
            // When hailing, and the block chosen is adequate, the return value will be 1000 (Basically: "Go to immediately")
            cir.setReturnValue(1000F);
        } else if (WeatherUtil.isHailing() && level.canSeeSky(pos))
        {
            // If exposed to the sky (hail will hit) find another block
            cir.setReturnValue(-1000F);
        }
    }
}
