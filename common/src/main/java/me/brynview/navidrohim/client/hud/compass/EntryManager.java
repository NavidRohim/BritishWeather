package me.brynview.navidrohim.client.hud.compass;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup.JMWaypointGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup.MapEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup.VehicleEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.entry.DefaultEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.DefaultEntryGroup;
import me.brynview.navidrohim.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/*
Manages persistent entries and the constructors that deserialises them.
 */
public class EntryManager
{

    // All constructors
    private static final Map<String, PersistentEntryConstructor> CONSTRUCTORS = new HashMap<>();
    private static final String PATH = "data/%s/".formatted(Constants.MOD_ID);

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
            nbtPath = Path.of(PATH + "%s.nbt".formatted(mc.getSingleplayerServer().getWorldData().getLevelName().replace(" ", "")));
        } else {
            nbtPath = Path.of(PATH + "%s.nbt".formatted(mc.getCurrentServer().ip));
        }

        return nbtPath;
    }

    /*
    Loads the current worlds / servers entry cache.
     */
    public static void load()
    {
        try
        {
            CompoundTag nbtR = NbtIo.read(getCurrentPath());
            if (nbtR == null) // Will be null if the file doesn't exist
            {
                return;
            }

            // set consists of keys which are the identifiers for each entry in the set.
            // Values are the `FriendlyByteBuf`s which holds the data of each entry.
            // Each entry has an entry in the map.
            for (Map.Entry<String, Tag> stringTagEntry : nbtR.entrySet())
            {
                if (stringTagEntry.getValue().asCompound().isPresent())
                {
                    CompoundTag compoundTagByteBuffers = stringTagEntry.getValue().asCompound().get();
                    Optional<byte[]> customDataByteBuf = compoundTagByteBuffers.get("custom").asByteArray();
                    Optional<byte[]> nativeByteBuf = compoundTagByteBuffers.get("native").asByteArray();

                    if (nativeByteBuf.isEmpty() || customDataByteBuf.isEmpty())
                    {
                        Constants.LOG.error("Could not deserialize entry: {} one of the required buffers is missing.", stringTagEntry);
                        continue;
                    }

                    ByteBuf byteBuf = Unpooled.copiedBuffer(nativeByteBuf.get()); // Normal netty ByteBuf
                    FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf); // Get FriendlyByteBuf as it plays better with MC. Can write strings and vectors

                    ByteBuf byteBufCustom = Unpooled.copiedBuffer(customDataByteBuf.get()); // Normal netty ByteBuf
                    FriendlyByteBuf customFriendlyByteBuf = new FriendlyByteBuf(byteBufCustom); // Get FriendlyByteBuf as it plays better with MC. Can write strings and vectors

                    // Read values for reconstructing.
                    String marker = friendlyByteBuf.readUtf();
                    int colour = friendlyByteBuf.readInt();
                    String type = friendlyByteBuf.readUtf(); // Type of entry. This is the name of the entry class. Used as a key to find the correct constructor
                    Vec3 pos = new Vec3(friendlyByteBuf.readVector3f());
                    String level = friendlyByteBuf.readUtf(); // What dimension this entry belongs to (was made in)

                    if (CONSTRUCTORS.containsKey(type)) // Check if the entry has a valid deserializer constructor
                    {
                        Compass.addEntry(CONSTRUCTORS.get(type).constructEntryWithExtraData(marker, colour, pos, level, customFriendlyByteBuf));
                    } else {
                        Constants.LOG.error("Marker present in persistence cache does not have a registered constructor! This is perhaps due to a version mismatch. {}", type);
                    }
                }
            }


        } catch (NullPointerException exc)
        {
            Constants.LOG.error("Corrupted pin. Error: {}", exc.getMessage());
        } catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    /*
    Saves the current entries (ONLY entries, not group entries) to file NBT for current world / server.
     */
    public static void save()
    {
        // Current entries
        Collection<DefaultEntry> defaultEntries = Compass.getEntries();

        CompoundTag master = new CompoundTag();

        for (DefaultEntry entry : defaultEntries)
        {
            if (!entry.isPersistent()) // Persistence check
            {
                continue;
            }

            if (!CONSTRUCTORS.containsKey(entry.getClass().getSimpleName())) // Check if entry has deserialization constructor
            {
                Constants.LOG.error("Entry marked as persistent does not have a registered constructor! Ignoring, but please take a look at class {}", entry.getClass());
                continue;
            }
            CompoundTag entryTag = new CompoundTag();

            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

            // Write metadata used to reconstruct
            buffer.writeUtf(entry.getMarker());
            buffer.writeInt(entry.getHighlightColor());
            buffer.writeUtf(entry.getClass().getSimpleName());
            buffer.writeVector3f(entry.getPosition().toVector3f());
            buffer.writeUtf(entry.getLevel());

            FriendlyByteBuf customDataBuffer = new FriendlyByteBuf(Unpooled.buffer());

            // Call the entries serialise method so the entry can encode anything else it wants
            entry.serialise(customDataBuffer);

            entryTag.putByteArray("native", buffer.array());
            entryTag.putByteArray("custom", customDataBuffer.array());

            master.put(entry.getId(), entryTag);
        }

        try
        {
            // Write
            NbtIo.write(master, getCurrentPath());
        } catch (IOException e)
        {
            // Bad, again
            e.printStackTrace();
        }
    }

    public static void registerPersistentConstructor(Class<?> classy, PersistentEntryConstructor constructor)
    {
        CONSTRUCTORS.put(classy.getSimpleName(), constructor);
    }

    /*
    Holds all entries and group entries made.
     */
    public final static class ProviderRegistry
    {

        // Internal map of all entries and group entries. This is messy and should be changed somehow, but I don't know to what.
        private static final HashMap<String, DefaultEntry> PROVIDERS = new HashMap<>();
        private static final HashMap<String, HashMap<String, DefaultEntry>> PROVIDERS_DIMENSION_SORTED = new HashMap<>();

        private static final Set<DefaultEntryGroup<?>> PROVIDER_GROUPS = Sets.newHashSet(); // Only one type of group entry can appear at once
        private static final HashMap<String, Set<DefaultEntryGroup<DefaultEntry>>> PROVIDER_GROUPS_DIMENSION_SORTED = new HashMap<>();

        static
        {
            ProviderRegistry.addEntryGroup(new MapEntryGroup(Level.OVERWORLD));
            ProviderRegistry.addEntryGroup(new VehicleEntryGroup(null));

            if (Services.PLATFORM.isModLoaded(Constants.JM_MOD_ID))
            {
                ProviderRegistry.addEntryGroup(new JMWaypointGroup(null));
            }
        }

        /*
        Tick all entries and group entries. Responsible for checking expiration as well
         */
        public static void tick(@NotNull LocalPlayer player)
        {
            // check providers
            PROVIDERS.values().removeIf(DefaultEntry::hasExpired);
            PROVIDERS_DIMENSION_SORTED.forEach((_, innerMap) -> innerMap.values().removeIf(DefaultEntry::hasExpired));

            PROVIDERS.values().forEach((p) -> p.tick(player));

            // check provider groups
            PROVIDER_GROUPS.removeIf(DefaultEntryGroup::hasExpired);
            PROVIDER_GROUPS_DIMENSION_SORTED.forEach((_, innerSet) -> innerSet.removeIf(DefaultEntryGroup::hasExpired));

            PROVIDER_GROUPS.forEach((pg) -> pg.tick(player));
        }

        public static Collection<DefaultEntry> getAllEntries()
        {
            return PROVIDERS.values();
        }

        public static Collection<DefaultEntry> getEntriesInDimension(ResourceKey<Level> key)
        {
            return PROVIDERS_DIMENSION_SORTED.computeIfAbsent(key.identifier().toString(), _ -> new HashMap<>()).values();
        }

        public static Set<DefaultEntryGroup<DefaultEntry>> getEntryGroupsInDimension(ResourceKey<Level> key)
        {
            return PROVIDER_GROUPS_DIMENSION_SORTED.computeIfAbsent(key.identifier().toString(), _ -> new HashSet<>());
        }

        public static Set<DefaultEntryGroup<DefaultEntry>> getEntryGroupsInAll()
        {
            return PROVIDER_GROUPS_DIMENSION_SORTED.computeIfAbsent("all",  _ -> new HashSet<>());
        }

        public static void addEntry(DefaultEntry entry)
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

        public static void removeEntry(DefaultEntry highlightedEntry)
        {
            PROVIDERS.remove(highlightedEntry.getId());
            PROVIDERS_DIMENSION_SORTED.get(highlightedEntry.getLevel()).remove(highlightedEntry.getId());
        }

        /*
        Adds an entry group
         */
        public static void addEntryGroup(DefaultEntryGroup group)
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
    }

    /*
        Callback interface for when a persistent entry is reconstructed from NBT file.
         */
    public static interface PersistentEntryConstructor
    {
        /*
        This should be used in lambda, and return an instance of whatever entry is persistent. See BritishWeather class for more info.
         */
        DefaultEntry constructEntryWithExtraData(String marker, int colour, Vec3 pos, String level, FriendlyByteBuf extraData);
    }
}
