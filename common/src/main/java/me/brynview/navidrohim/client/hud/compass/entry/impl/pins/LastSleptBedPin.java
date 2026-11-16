package me.brynview.navidrohim.client.hud.compass.entry.impl.pins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.TimeKeeper;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.TrackedJourney;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.BlitRenderer;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.CharEntryRenderer;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.client.hud.compass.entry.impl.time.GameTime;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.TimeUnit;

public class LastSleptBedPin extends TimedPin
{
    private static final class BedEntryRenderer extends EntryRenderer<LastSleptBedPin>
    {
        private static final Identifier bed = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "item/bed");
        private static final int bedSquare = 10;
        private static final int bedSquareHalf = bedSquare / 2;

        public BedEntryRenderer(LastSleptBedPin owner)
        {
            super(owner);
        }

        @Override
        public void drawMarker(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext, int x, float yRow)
        {
            int y = compassRenderContext.getYOnRowCentered(0, bedSquare);
            guiGraphicsExtractor.blitSprite(GuiPipeline.OPAQUE_TEXTURED_BACKGROUND, bed, screenX - bedSquareHalf, y, bedSquare, bedSquare);
        }

        @Override
        public void drawDecoration(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext)
        {

        }
    }

    public static final String PIN_ID = "last_slept_bed";

    public static final Codec<TimedPin> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("marker").forGetter(PinEntry::getMarker),
                    Codec.STRING.fieldOf("level").forGetter(PinEntry::getLevel),
                    Vec3.CODEC.fieldOf("position").forGetter(PinEntry::getPosition),
                    Codec.INT.fieldOf("colour").forGetter(PinEntry::getHighlightColor),
                    EntrySettings.CODEC.fieldOf("settings").forGetter(PinEntry::getSettings),
                    TrackedJourney.CODEC.optionalFieldOf("tracked", TrackedJourney.none()).forGetter(PinEntry::getJourney),
                    TimeKeeper.CODEC.fieldOf("timeKeeper").forGetter(TimedPin::getTimeMethod)
            ).apply(instance, LastSleptBedPin::new)
    );

    public LastSleptBedPin(String level, Vec3 position)
    {
        super(
                "last bed location. If your somehow seeing this in-game, my code is fucked.",
                level,
                position,
                ColorHelper.RED,
                EntrySettings.defaults().setShowDistance(true),
                TrackedJourney.empty(),
                new GameTime(TimeUnit.MINUTES, 30)
        );
        this.renderer = new BedEntryRenderer(this);
    }

    public LastSleptBedPin(String marker, String level, Vec3 position, Integer colour, EntrySettings settings, TrackedJourney trackedJourney, TimeKeeper timeKeeper)
    {
        super(marker, level, position, colour, settings, trackedJourney, timeKeeper);
        this.renderer = new BedEntryRenderer(this);
    }

    @Override
    public boolean canBeEdited()
    {
        return false;
    }

    @Override
    public String getType()
    {
        return PIN_ID;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    @Override
    public Tag encode()
    {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }
}
