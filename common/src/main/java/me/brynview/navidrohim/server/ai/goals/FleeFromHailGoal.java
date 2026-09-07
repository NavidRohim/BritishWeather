package me.brynview.navidrohim.server.ai.goals;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FleeFromHailGoal extends FleeSunGoal
{
    public FleeFromHailGoal(PathfinderMob mob, double speedModifier)
    {
        super(mob, speedModifier);
    }

    private boolean isHailing()
    {
        return CommonClass.getWeatherManager().getState().getWeatherCondition().isHail();
    }

    @Override
    public boolean canUse()
    {
        // If been hit by hail, activate goal
        if (mob.level().canSeeSky(mob.blockPosition()) && isHailing())
        {
            return this.setWantedPos();
        }
        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !mob.getNavigation().isDone() || !mob.level().canSeeSky(mob.blockPosition());
    }

    @Override
    protected @Nullable Vec3 getHidePos()
    {
        RandomSource random = this.mob.getRandom();
        BlockPos pos = this.mob.blockPosition();

        for (int i = 0; i < 10; i++) {
            BlockPos randomPos = pos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.mob.level().canSeeSky(randomPos) && mob.level().getBlockState(randomPos).isAir()) {
                return Vec3.atBottomCenterOf(randomPos);
            }
        }

        return null;
    }
}
