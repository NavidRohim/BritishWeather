package me.brynview.navidrohim.client.hud.compass.entry;

import me.brynview.navidrohim.client.hud.compass.entry.time.GameTime;
import me.brynview.navidrohim.client.hud.compass.entry.time.RealTime;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/*
TimeMethod basically counts time for a TimedEntry and tells it when to expire.
The entry doesn't just count itself as some entries might count time past while offline (RealTime class) or only count time past in-game (GameTime).
To simplify this, the TimeMethod interface was made if in the future I add other ways of counting. Which sounds kinda stupid.
 */
public interface TimeKeeper
{
    default void tick() {}

    boolean isExpired();

    default long timeLeft()
    {
        return 0L;
    }

    /*
    Encode the TimeMethod so it persists. Usually encodes the current time, and what time it will end.
     */
    default void encode(FriendlyByteBuf friendlyByteBuf)
    {
    }

    TimeMethods getKey();

    enum TimeMethods
    {
        REALTIME("real time", RealTime::new, RealTime::decode),
        GAMETIME("game time", GameTime::new, GameTime::decode);

        private final String displayString;
        private final BiFunction<TimeUnit, Integer, TimeKeeper> supplier;
        private final Function<FriendlyByteBuf, TimeKeeper> bufferDecoder;

        TimeMethods(String displayName, BiFunction<TimeUnit, Integer, TimeKeeper> timeMethodSupplier, Function<FriendlyByteBuf, TimeKeeper> fromBuffer)
        {
            this.displayString = displayName;
            this.supplier = timeMethodSupplier;
            this.bufferDecoder = fromBuffer;
        }

        public String getDisplayName()
        {
            return displayString;
        }

        public TimeKeeper createTimeMethod(TimeUnit timeUnit, int time)
        {
            return supplier.apply(timeUnit, time);
        }

        public TimeKeeper getFromBuffer(FriendlyByteBuf buffer)
        {
            return bufferDecoder.apply(buffer);
        }

        public Tooltip getDescription()
        {
            return Tooltip.create(Component.translatable("br.compass.time_methods.%s.tooltip".formatted(this.name().toLowerCase())));
        }

        public static TimeKeeper getTimeKeeperFromBuffer(FriendlyByteBuf buffer)
        {
            String savedTimeMethod = buffer.readUtf();
            TimeKeeper.TimeMethods tm = TimeKeeper.TimeMethods.valueOf(savedTimeMethod);
            return tm.getFromBuffer(buffer);
        }
    }
}
