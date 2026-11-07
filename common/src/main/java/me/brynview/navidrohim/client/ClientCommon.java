package me.brynview.navidrohim.client;

import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup.JMWaypointGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup.MapEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup.VehicleEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.DeathPin;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.TimedPin;
import me.brynview.navidrohim.platform.Services;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ClientCommon
{
    private static String currentServerName = "";
    private static ClientWeatherManager WEATHER_MANAGER;
    public static Compass compass;

    public static final SoundEvent SCROLL = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "scroll"));

    public static void init()
    {
        WEATHER_MANAGER = new ClientWeatherManager();
        compass = Compass.init();

        EntryManager.addPinCodec(PinEntry.PIN_ID, PinEntry.CODEC);
        EntryManager.addPinCodec(TimedPin.PIN_ID, TimedPin.CODEC);
        EntryManager.addPinCodec(DeathPin.PIN_ID, DeathPin.CODEC);
    }

    public static void initNativeEntryGroups()
    {
        EntryManager.addEntryGroup(new MapEntryGroup(Level.OVERWORLD));
        EntryManager.addEntryGroup(new VehicleEntryGroup(null));

        if (Services.PLATFORM.isModLoaded(Constants.JM_MOD_ID))
        {
            EntryManager.addEntryGroup(new JMWaypointGroup(null));
        }
    }

    public static ClientWeatherManager getWeatherManager()
    {
        return WEATHER_MANAGER;
    }

    public static void tickClient(@NotNull LocalPlayer player)
    {
        Compass.getInstance().tick(player);
    }

    /*
    Call then when the client level changes (transferring dimension, loading into a world or server)
     */
    public static void joinServerEvent()
    {
        ClientCommon.WEATHER_MANAGER.reset();
        Compass.prepareAndLoad();
    }

    public static void leaveServerEvent()
    {
        EntryManager.savePins();
        Compass.cleanupAndReset();
    }

}
