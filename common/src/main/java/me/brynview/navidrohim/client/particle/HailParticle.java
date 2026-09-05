package me.brynview.navidrohim.client.particle;

import me.brynview.navidrohim.client.ClientCommon;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HailParticle extends SimpleAnimatedParticle
{

    protected HailParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, RandomSource random)
    {
        super(level, x, y, z, sprites, 1);

        int hail = ClientCommon.getWeatherManager().getWeather().getHailLevel();
        if (hail != 1 && random.nextInt(0, hail + 1) == 0)
        {
            this.remove();
        }
        this.setParticleSpeed((double) random.nextInt(2) / 5, yd + (0.25 + (-0.05 * hail)), (double) random.nextInt(2) / 5);
        this.setLifetime(60);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {

        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(@NonNull SimpleParticleType options, @NonNull ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, @NonNull RandomSource random)
        {
            return new HailParticle(level, x, y, z, this.sprites, random);
        }
    }
}
