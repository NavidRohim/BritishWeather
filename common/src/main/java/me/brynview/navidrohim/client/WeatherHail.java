package me.brynview.navidrohim.client;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.server.WeatherManager;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;
import java.util.OptionalDouble;

public class WeatherHail
{
    public static void tickWeatherHail(ClientLevel level)
    {
        /*
        Minecraft mc = Minecraft.getInstance();
        // I believe ClientLevel::tickWeatherEffects() handles rendering of rain effects
        if (ClientCommon.SERVER_WEATHER_STATE.getWeatherCondition() == WeatherManager.WeatherCondition.HAIL)
        {
            //Vec3 pos = Minecraft.getInstance().player.getPosition(1);
            //level.addParticle(ModParticles.HAIL, pos.x, pos.y, pos.z, 0, 0, 0);
        }*/
        //tickWeatherHailTest(level);
    }
    /*
    public static void tickWeatherHailTest(ClientLevel level)
    {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture rainTexture = textureManager.getTexture(RAIN_LOCATION);
        AbstractTexture snowTexture = textureManager.getTexture(SNOW_LOCATION);
        RenderTarget weatherRenderTarget = OutputTarget.WEATHER_TARGET.getRenderTarget();
        GpuTextureView colorTexture = weatherRenderTarget.getColorTextureView();
        GpuTextureView depthTexture = weatherRenderTarget.getDepthTextureView();
        RenderPipeline renderPipeline = Minecraft.getInstance().gameRenderer.gameRenderState().useShaderTransparency()
                ? RenderPipelines.WEATHER_DEPTH_WRITE
                : RenderPipelines.WEATHER_NO_DEPTH_WRITE;

        GpuBuffer vertexBuffer;
        GpuBuffer indexBuffer;
        IndexType indexType;
        try (ByteBufferBuilder builder = ByteBufferBuilder.exactlySized(columnCount * DefaultVertexFormat.PARTICLE.getVertexSize() * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(builder, PrimitiveTopology.QUADS, DefaultVertexFormat.PARTICLE);
            this.renderInstances(bufferBuilder, renderState.rainColumns, cameraPos, 1.0F, renderState.radius, renderState.intensity);
            this.renderInstances(bufferBuilder, renderState.snowColumns, cameraPos, 0.8F, renderState.radius, renderState.intensity);

            try (MeshData mesh = bufferBuilder.buildOrThrow()) {
                vertexBuffer = this.uploadVertexBuffer(mesh.vertexBuffer());
                RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(mesh.drawState().primitiveTopology());
                indexBuffer = autoIndices.getBuffer(mesh.drawState().indexCount());
                indexType = autoIndices.type();
            }
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Weather Effect", colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty())) {
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.bindTexture(
                    "Sampler2", Minecraft.getInstance().gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
            );
            renderPass.setIndexBuffer(indexBuffer, indexType);
            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            this.renderWeather(renderPass, rainTexture, 0, renderState.rainColumns.size());
            this.renderWeather(renderPass, snowTexture, renderState.rainColumns.size(), renderState.snowColumns.size());
        }
    }*/
}
