package me.brynview.navidrohim.client.hud.compass.entry.iapi;

import me.brynview.navidrohim.client.hud.compass.entry.iapi.entry.DefaultEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/*
Callback interface for when a persistent entry is reconstructed from NBT file.
 */
public interface PersistentEntryConstructor
{
    /*
    This should be used in lambda, and return an instance of whatever entry is persistent. See BritishWeather class for more info.
     */
    DefaultEntry constructEntryWithExtraData(String marker, int colour, Vec3 pos, String level, FriendlyByteBuf extraData);
}
