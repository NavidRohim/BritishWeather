package me.brynview.navidrohim.server;

import me.brynview.navidrohim.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class DamageSources
{
    public static final ResourceKey<DamageType> HAIL_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "hail_damage"));
}
