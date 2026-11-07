package me.brynview.navidrohim.client.hud.compass.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.brynview.navidrohim.client.hud.compass.EntryManager;
import me.brynview.navidrohim.client.hud.compass.entry.impl.time.GameTime;
import me.brynview.navidrohim.client.hud.compass.entry.impl.time.RealTime;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.NotImplementedException;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/*
TimeMethod basically counts time for a TimedEntry and tells it when to expire.
The entry doesn't just count itself as some entries might count time past while offline (RealTime class) or only count time past in-game (GameTime).
To simplify this, the TimeMethod interface was made if in the future I add other ways of counting. Which sounds kinda stupid.
 */
public abstract class TimeKeeper
{
    public static final Codec<TimeKeeper> CODEC = Codec.STRING.dispatch(
                    "type",
                    TimeKeeper::getKey,
                    id -> {
                        MapCodec<? extends TimeKeeper> codec = EntryManager.TIMEKEEPER_CODECS.get(id);
                        if (codec == null)
                        {
                            throw new RuntimeException("Unknown time keeper: " + id);
                        }

                        return codec;
                    }
            );

    public long timeLeft()
    {
        return 0L;
    }

    public void tick() {}

    public boolean isExpired()
    {
        throw new NotImplementedException();
    }

    /*
    Encode the TimeMethod so it persists. Usually encodes the current time, and what time it will end.
     */

    public String getKey()
    {
        throw new NotImplementedException();
    }

    public enum TimeMethods
    {
        GAMETIME("game time", GameTime::new),
        REALTIME("real time", RealTime::new);

        private final String displayString;
        private final BiFunction<TimeUnit, Integer, TimeKeeper> supplier;

        TimeMethods(String displayName, BiFunction<TimeUnit, Integer, TimeKeeper> supplier)
        {
            this.displayString = displayName;
            this.supplier = supplier;
        }

        public String getDisplayName()
        {
            return displayString;
        }

        public Tooltip getDescription()
        {
            return Tooltip.create(Component.translatable("br.compass.time_methods.%s.tooltip".formatted(this.name().toLowerCase())));
        }

        public TimeKeeper createTimeMethod(TimeUnit timeUnit, int time)
        {
            return supplier.apply(timeUnit, time);
        }

    }
}
