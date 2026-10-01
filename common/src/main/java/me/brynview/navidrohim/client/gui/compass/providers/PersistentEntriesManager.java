package me.brynview.navidrohim.client.gui.compass.providers;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.gui.compass.HudCompass;
import me.brynview.navidrohim.client.gui.compass.providers.builtin.entrygroup.MapObjectiveEntryGroup;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.PersistentEntryConstructor;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.CompassEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entrygroup.DefaultEntryGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PersistentEntriesManager
{

    private static final Map<String, PersistentEntryConstructor> CONSTRUCTORS = new HashMap<>();

    public static Path getCurrentPath()
    {
        Minecraft mc = Minecraft.getInstance();

        Path nbtPath;
        if (mc.hasSingleplayerServer())
        {
            nbtPath = Path.of("%s.nbt".formatted(mc.getSingleplayerServer().getWorldData().getLevelName().replace(" ", "")));
        } else {
            nbtPath = Path.of("%s.nbt".formatted(mc.getCurrentServer().ip));
        }

        return nbtPath;
    }

    public static void load()
    {
        try
        {
            CompoundTag nbtR = NbtIo.read(getCurrentPath());
            if (nbtR == null)
            {
                return;
            }

            for (Map.Entry<String, Tag> stringTagEntry : nbtR.entrySet())
            {
                if (stringTagEntry.getValue().asByteArray().isPresent())
                {
                    ByteBuf byteBuf = Unpooled.copiedBuffer(stringTagEntry.getValue().asByteArray().get());
                    FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);

                    String marker = friendlyByteBuf.readUtf();
                    String type = friendlyByteBuf.readUtf();
                    Vec3 pos = new Vec3(friendlyByteBuf.readVector3f());
                    String level = friendlyByteBuf.readUtf();

                    if (CONSTRUCTORS.containsKey(type))
                    {
                        HudCompass.addEntry(CONSTRUCTORS.get(type).constructEntryWithExtraData(marker, pos, level, friendlyByteBuf));
                    } else {
                        Constants.LOG.error("Marker present in persistence cache does not have a registered constructor! This is perhaps due to a version mismatch. {}", type);
                    }
                }
            }

        } catch (IOException err)
        {
            err.printStackTrace();
        }
    }

    public static void save()
    {
        Collection<DefaultEntry> defaultEntries = HudCompass.getEntries();

        if (defaultEntries.isEmpty())
        {
            return;
        }

        CompoundTag master = new CompoundTag();

        for (DefaultEntry entry : defaultEntries)
        {
            if (!entry.isPersistent())
            {
                continue;
            }

            if (!CONSTRUCTORS.containsKey(entry.getClass().getSimpleName()))
            {
                Constants.LOG.error("Entry marked as persistent does not have a registered constructor! Ignoring, but please take a look at class {}", entry.getClass());
                continue;
            }

            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

            buffer.writeUtf(entry.getMarker());
            buffer.writeUtf(entry.getClass().getSimpleName());
            buffer.writeVector3f(entry.getPosition().toVector3f());
            buffer.writeUtf(entry.getLevel());

            entry.serialise(buffer);

            master.putByteArray(entry.getId(), buffer.array());
        }

        try
        {
            NbtIo.write(master, getCurrentPath());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void registerPersistentConstructor(Class<?> classy, PersistentEntryConstructor constructor)
    {
        CONSTRUCTORS.put(classy.getSimpleName(), constructor);
    }

    public final static class ProviderRegistry
    {

        private static final HashMap<String, DefaultEntry> PROVIDERS = new HashMap<>();
        public static final Set<DefaultEntryGroup> PROVIDER_GROUPS = Sets.newHashSet();

        static
        {
            ProviderRegistry.addEntryGroup(new MapObjectiveEntryGroup());
        }

        public static void tick(@NotNull LocalPlayer player)
        {
            PROVIDERS.values().removeIf(CompassEntry::hasExpired);
            PROVIDERS.values().forEach((p) -> p.tick(player));

            PROVIDER_GROUPS.removeIf(DefaultEntryGroup::hasExpired);
            PROVIDER_GROUPS.forEach((pg) -> pg.tick(player));
        }

        public static Collection<DefaultEntry> getEntries()
        {
            return PROVIDERS.values();
        }

        public static void addEntry(DefaultEntry entry)
        {
            String id = entry.getId();
            if (entry instanceof Singleton && !((Singleton) entry).isAbsolute())
            {
                if (!PROVIDERS.containsKey(id))
                {
                    PROVIDERS.put(id, entry);
                }
            } else
            {
                PROVIDERS.put(id, entry);
            }
        }

        public static void addEntryGroup(DefaultEntryGroup group)
        {
            PROVIDER_GROUPS.add(group);
        }

        public static void stopTickAll(@NotNull LocalPlayer player)
        {
            PROVIDERS.values().forEach((p) -> p.endTick(player));
            PROVIDER_GROUPS.forEach((pg) -> pg.endTick(player));
        }
    }
}
