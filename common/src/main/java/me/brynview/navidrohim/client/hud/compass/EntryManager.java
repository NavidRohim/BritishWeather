package me.brynview.navidrohim.client.hud.compass;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.pins.PinEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.time.GameTime;
import me.brynview.navidrohim.client.hud.compass.entry.impl.time.RealTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/*
Manages persistent entries and the constructors that deserialises them.
 */
public class EntryManager
{

    public static final Map<String, MapCodec<? extends TimeKeeper>> TIMEKEEPER_CODECS = Map.of(
            "gametime", GameTime.CODEC,
            "realtime", RealTime.CODEC
    );
    public static final Map<String, Codec<? extends PinEntry>> PIN_CONSTRUCTORS = new HashMap<>();

    private static final String PATH = "data/%s/".formatted(Constants.MOD_ID);
    // Internal map of all entries and group entries. This is messy and should be changed somehow, but I don't know to what.
    private static final HashMap<String, AbstractEntry> PROVIDERS = new HashMap<>();
    private static final HashMap<String, HashMap<String, AbstractEntry>> PROVIDERS_DIMENSION_SORTED = new HashMap<>();
    private static final Set<AbstractEntryGroup<?>> PROVIDER_GROUPS = Sets.newHashSet(); // Only one type of group entry can appear at once
    private static final HashMap<String, Set<AbstractEntryGroup<? extends AbstractEntry>>> PROVIDER_GROUPS_DIMENSION_SORTED = new HashMap<>();


    /*
    Get the current NBT entry cache. Will be the name of the world without spaces if singleplayer, IP of the server if multiplayer.
     */
    public static Path getCurrentPath()
    {
        Minecraft mc = Minecraft.getInstance();
        File path = new File(PATH);

        if (!path.isDirectory())
        {
            boolean success = path.mkdirs();
            if (!success)
            {
                throw new RuntimeException("Could not create data path for persistent pins.");
            }
        }

        Path nbtPath;
        if (mc.hasSingleplayerServer())
        {
            nbtPath = Path.of(mc.getSingleplayerServer().getWorldPath(LevelResource.DATA) + "/%s/pins.nbt".formatted(Constants.MOD_ID));
        } else {
            nbtPath = Path.of(PATH + "%s.nbt".formatted(mc.getCurrentServer().ip));
        }

        return nbtPath;
    }

    /*
    Loads the current worlds / servers entry cache.
     */
    public static void loadPins()
    {
        try
        {
            CompoundTag nbtR = NbtIo.read(getCurrentPath());
            if (nbtR == null) // Will be null if the file doesn't exist
            {
                return;
            }

            String trackedPinId = nbtR.getStringOr("tracked", "none");

            for (Map.Entry<String, Tag> stringTagEntry : nbtR.entrySet())
            {
                String codecName = stringTagEntry.getKey().split("\\.")[0];
                @Nullable Codec<? extends PinEntry> codecForPin = EntryManager.PIN_CONSTRUCTORS.get(codecName);

                if (codecForPin != null)
                {
                    PinEntry decodedPin = codecForPin.parse(NbtOps.INSTANCE, stringTagEntry.getValue()).getOrThrow();
                    Compass.addEntry(decodedPin);

                    if (trackedPinId.equals(decodedPin.getId()) && decodedPin.isTracked())
                    {
                        Compass.getInstance().setTrackedPin(decodedPin);
                    } else if (!trackedPinId.equals(decodedPin.getId()) && decodedPin.isTracked())
                    {
                        // Maybe don't reset but set trackedJourney.tracked to `false` and leave other information
                        Constants.LOG.warn("Pin \"{}\" is marked as tracked, but NBT `tracked` value is not pointing to this pin. Resetting pin tracking information.", decodedPin.getId());
                        decodedPin.trackedJourney.reset();
                    }
                }
            }
        } catch (NullPointerException | IllegalStateException exc)
        {
            Constants.LOG.error("Corrupted pin. Error: {}", exc.getMessage());
        } catch (IOException exc)
        {
            Constants.LOG.error("Could not read pins for {}. Error: {}", getCurrentPath(), exc.getMessage());
        }
    }

    /*
    Saves the current entries (ONLY entries, not group entries) to file NBT for current world / server.
     */
    public static void savePins()
    {
        // Current entries

        Collection<? extends AbstractEntry> defaultEntries = EntryManager.getAllEntries();

        CompoundTag master = new CompoundTag();
        Compass compass = Compass.getInstance();

        if (compass.getTrackedPin() != null)
        {
            master.putString("tracked", compass.getTrackedPin().getId());
        } else {
            master.putString("tracked", "none");
        }

        for (AbstractEntry entry : defaultEntries)
        {
            if (entry instanceof PinEntry pin && pin.getSettings().isPersistent())
            {
                master.put(pin.getId(), pin.encode());
            }
        }

        try
        {
            // Write
            NbtIo.write(master, getCurrentPath());
        } catch (IOException e)
        {
            Constants.LOG.error("Could not save pins to persistent storage. Error: {}", e.getMessage());
        }
    }

    /*
        Tick all entries and group entries. Responsible for checking expiration as well
         */
    public static void tick(@NotNull LocalPlayer player)
    {
        // check providers
        PROVIDERS.values().removeIf(AbstractEntry::hasExpired);
        PROVIDERS_DIMENSION_SORTED.forEach((_, innerMap) -> innerMap.values().removeIf(AbstractEntry::hasExpired));

        PROVIDERS.values().forEach((p) -> p.tick(player));

        // check provider groups
        PROVIDER_GROUPS.removeIf(AbstractEntryGroup::hasExpired);
        PROVIDER_GROUPS_DIMENSION_SORTED.forEach((_, innerSet) -> innerSet.removeIf(AbstractEntryGroup::hasExpired));

        PROVIDER_GROUPS.forEach((pg) -> pg.tick(player));
    }

    public static Collection<? extends AbstractEntry> getAllEntries()
    {
        return PROVIDERS.values();
    }

    public static Collection<? extends AbstractEntry> getEntriesInDimension(ResourceKey<Level> key)
    {
        return PROVIDERS_DIMENSION_SORTED.computeIfAbsent(key.identifier().toString(), _ -> new HashMap<>()).values();
    }

    public static Set<AbstractEntryGroup<? extends AbstractEntry>> getEntryGroupsInDimension(ResourceKey<Level> key)
    {
        return PROVIDER_GROUPS_DIMENSION_SORTED.computeIfAbsent(key.identifier().toString(), _ -> new HashSet<>());
    }

    public static Set<AbstractEntryGroup<? extends AbstractEntry>> getEntryGroupsInAll()
    {
        return PROVIDER_GROUPS_DIMENSION_SORTED.computeIfAbsent("all",  _ -> new HashSet<>());
    }

    public static void addEntry(AbstractEntry entry)
    {
        String id = entry.getId(); // Check if the entry is a singleton and will not just override an existing singleton.
        if (entry.isSingleton())
        {
            if (!PROVIDERS.containsKey(id)) // Check if singleton exists, if not, put.
            {
                PROVIDERS_DIMENSION_SORTED.computeIfAbsent(entry.getLevel(), _ -> new HashMap<>()).put(id, entry);
                PROVIDERS.put(id, entry);
            }
        } else
        {
            PROVIDERS_DIMENSION_SORTED.computeIfAbsent(entry.getLevel(), _ -> new HashMap<>()).put(id, entry);
            PROVIDERS.put(id, entry);
        }
    }

    public static void removeEntry(AbstractEntry highlightedEntry)
    {
        PROVIDERS.remove(highlightedEntry.getId());
        PROVIDERS_DIMENSION_SORTED.get(highlightedEntry.getLevel()).remove(highlightedEntry.getId());
    }

    /*
        Adds an entry group
         */
    public static void addEntryGroup(AbstractEntryGroup<?> group)
    {
        PROVIDER_GROUPS.add(group);
        PROVIDER_GROUPS_DIMENSION_SORTED.computeIfAbsent(group.getLevel(), _ -> new HashSet<>()).add(group);
    }

    /*
        Sends endTick to all entries and entry groups. endTick is sent when the entry HUD disappears.
        Cleanup can be done in these end ticks.
         */
    public static void stopTickAll(@NotNull LocalPlayer player)
    {
        PROVIDERS.values().forEach((p) -> p.endTick(player));
        PROVIDER_GROUPS.forEach((pg) -> pg.endTick(player));
    }

    public static void clear()
    {
        PROVIDERS.clear();
        PROVIDERS_DIMENSION_SORTED.forEach((_, subMap) -> subMap.clear());

        PROVIDER_GROUPS.removeIf((g) -> !g.isBuiltIn());
        PROVIDER_GROUPS_DIMENSION_SORTED.forEach((_, subSet) -> subSet.removeIf((g) -> !g.isBuiltIn()));
    }

    public static void addPinCodec(String key, Codec<? extends PinEntry> codec)
    {
        PIN_CONSTRUCTORS.put(key, codec);
    }
}
