package me.brynview.navidrohim.server.ai.goals;

import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.util.WeatherUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FleeHailGoal extends FleeSunGoal
{
    public FleeHailGoal(PathfinderMob mob, double speedModifier)
    {
        super(mob, speedModifier);
    }

    @Override
    public boolean canUse()
    {
        // Only activate goal if hail damage is enabled and entity isn't sheltered
        if (BritishWeather.getConfig().hailShouldDealDamage() && mob.level().canSeeSky(mob.blockPosition()) && WeatherUtil.isHailing())
        {
            return this.setWantedPos();
        }
        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        // Stop fleeing if entity is sheltered
        return !mob.getNavigation().isDone() || !mob.level().canSeeSky(mob.blockPosition());
    }

    /*
    Code basically identical to the FleeSunGoal. Some things changed to fit with the AnimalAiMixin
     */
    @Override
    protected @Nullable Vec3 getHidePos()
    {
        RandomSource random = this.mob.getRandom();
        for (int i = 0; i < 10; i++)
        {
            // Get random block within 10 blocks
            BlockPos randomPos = this.mob.blockPosition().offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);

            // Check if block is suitable to flee to if it's hailing (cannot see sky and the walk target value is correct)
            if (!this.mob.level().canSeeSky(randomPos) && this.mob.getWalkTargetValue(randomPos) == 1000F)
            {
                return Vec3.atBottomCenterOf(randomPos);
            }
        }

        return null;
    }
}
