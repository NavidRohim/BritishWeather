package me.brynview.navidrohim.client.hud.compass.entry.impl.entrygroup;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntry;
import me.brynview.navidrohim.client.hud.compass.entry.entry.AbstractEntryGroup;
import me.brynview.navidrohim.client.hud.compass.entry.impl.EntrySettings;
import me.brynview.navidrohim.client.hud.compass.entry.impl.renderers.EntryRenderer;
import me.brynview.navidrohim.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JMWaypointGroup extends AbstractEntryGroup<JMWaypointGroup.WaypointEntry>
{
    public static final class WaypointEntry extends AbstractEntry
    {
        private final Waypoint waypoint;

        public final class WaypointEntryRenderer extends EntryRenderer<WaypointEntry>
        {
            public WaypointEntryRenderer(WaypointEntry owner)
            {
                super(owner);
            }

            @Override
            public void drawDecoration(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext compassRenderContext)
            {
                guiGraphicsExtractor.blit(
                        GuiPipeline.OPAQUE_TEXTURED_BACKGROUND,
                        waypoint.getIconIdentifier(),
                        compassRenderContext.centerWidthAroundX(12, screenX), compassRenderContext.getYRowOnCompass(screenYRow) - 2,
                        0, 0,
                        12, 12,
                        12, 12,
                        waypoint.getColor()
                );
            }
        }

        public static final EntrySettings settings = new EntrySettings()
                .setShowBackground(false)
                .setShowDistance(true)
                .setShadow(true)
                .setPersistent(false);

        public WaypointEntry(Waypoint waypoint, String level)
        {
            super(new Vec3(waypoint.getX(), waypoint.getY(), waypoint.getZ()), level, waypoint.getName(), waypoint.getColor(), settings);
            this.waypoint = waypoint;
            this.renderer = new WaypointEntryRenderer(this);
        }

        @Override
        public boolean isBuiltIn()
        {
            return true;
        }
    }

    private final IClientAPI api;

    public JMWaypointGroup(@Nullable ResourceKey<Level> level)
    {
        super(level);
        this.api = Services.PLATFORM.getJourneyMapIntegration();
    }

    @Override
    public void startTick(@NotNull LocalPlayer player, Compass.CompassRenderContext context)
    {
        for (Waypoint waypoint : this.api.getAllWaypoints(player.level().dimension()))
        {
            this.addEntry(new WaypointEntry(waypoint, getLevel()));
        }
    }

    @Override
    public void endTick(@NotNull LocalPlayer player)
    {
        super.endTick(player);
        this.clearEntries();
    }

    @Override
    public boolean isBuiltIn()
    {
        return true;
    }
}
