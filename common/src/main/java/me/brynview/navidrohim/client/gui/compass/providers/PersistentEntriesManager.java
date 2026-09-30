package me.brynview.navidrohim.client.gui.compass.providers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.gui.compass.HudCompass;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.PersistentEntryConstructor;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
}
