package me.brynview.navidrohim.server.ai.goals;

import me.brynview.navidrohim.CommonClass;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FleeFromHailGoal extends FleeSunGoal
{
    public boolean isInProgress = false;

    public FleeFromHailGoal(PathfinderMob mob, double speedModifier)
    {
        super(mob, speedModifier);
    }

    @Override
    public boolean canUse()
    {
        if (mob.level().canSeeSky(mob.blockPosition()) && Util.isHailing())
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

        for (int i = 0; i < 10; i++)
        {
            BlockPos randomPos = pos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.mob.level().canSeeSky(randomPos) && this.mob.getWalkTargetValue(randomPos) == 1000F)
            {
                return Vec3.atBottomCenterOf(randomPos);
            }
        }

        return null;
    }

    @Override
    public void start()
    {
        super.start();
        this.isInProgress = true;
    }

    @Override
    public void stop()
    {
        super.stop();
        this.isInProgress = false;
    }
}
