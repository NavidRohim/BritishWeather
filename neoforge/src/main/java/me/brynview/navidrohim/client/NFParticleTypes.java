package me.brynview.navidrohim.client;

import me.brynview.navidrohim.Constants;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NFParticleTypes
{
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Constants.MOD_ID);

    public static final Supplier<SimpleParticleType> HAIL = PARTICLE_TYPES.register(
            "fallen_hail_1",
            () -> new SimpleParticleType(false));
}
