package me.brynview.navidrohim.client.gui.compass;

import com.google.common.collect.Sets;
import me.brynview.navidrohim.BritishWeather;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.ClientCommon;
import me.brynview.navidrohim.client.gui.compass.providers.builtin.MapObjectEntryGroup;
import me.brynview.navidrohim.client.gui.compass.providers.builtin.PinEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.PersistentEntriesManager;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.Singleton;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.CompassEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entry.DefaultEntry;
import me.brynview.navidrohim.client.gui.compass.providers.iapi.entrygroup.DefaultEntryGroup;
import me.brynview.navidrohim.util.ColorHelper;
import me.brynview.navidrohim.util.FontHelper;
import me.brynview.navidrohim.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HudCompass
{
    private final static class ProviderRegistry
    {

        private static final HashMap<String, DefaultEntry> PROVIDERS = new HashMap<>();
        private static final Set<DefaultEntryGroup> PROVIDER_GROUPS = Sets.newHashSet();

        static
        {
            PROVIDER_GROUPS.add(new MapObjectEntryGroup());
        }

        private static void tick(@NotNull LocalPlayer player)
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
            Constants.LOG.info(id);
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

    // Colours for compass elements
    private static final int CENTER_COLOR = ColorHelper.decode("#FF0000").getRGB(); // Red
    private static final int COMPASS_BG_COLOR = ColorHelper.rgb(255, 255, 255, 190);
    private static final int COMPASS_BG_SHADOW_COLOUR = ColorHelper.rgb(0, 0, 0, 100);
    private static final int WHITE = ColorHelper.decode("#FFFFFF").getRGB();

    // Colours for entity markers
    private static final int PLAYER = ColorHelper.rgb(125, 255, 125, 255);
    private static final int NEUTRAL = ColorHelper.rgb(255, 255, 255, 255);
    private static final int HOSTILE = ColorHelper.rgb(255, 125, 125, 255);
    private static final int ALLY = ColorHelper.rgb(125, 125, 255, 255);

    // Text used for compass decorations
    private static final String COMPASS_HEADING = "|";
    private static final String ENTITY_LABEL = "⏺";

    // Math constants used for rendering
    private static final double EXPAND_INFLATE_WITH_FOV_CONST = 0.119; // value at lowest fov (30): 3.477 blocks value at highest (110): 13.07
    private static final int SCALE_MAX = 255; // Max value of what the entity distance scale should be (entity further away = lower, closer = higher)
    private static final int DETECTION_DISTANCE = 40; // How far to check in front of the player for entities
    private static final int MAX_ALLOWED_ENTITIES_ON_COMPASS = 10; // Max entities allowed on compass

    // Non-final general variables
    private static boolean didRenderProviders = false;

    public static void render(GuiGraphicsExtractor guiGraphics, Minecraft mc, int scaledWidth, float partialTicks) {
        final LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        // uncomment this if something weird happens in pause menu
        //final float partialTicks = mc.isPaused() ? 0 : _partialTicks;
        final int x = MathHelper.getCompassX(scaledWidth, mc.font.width(COMPASS_HEADING));

        final int yMiddle = (BritishWeather.getConfig().getCompassY() + mc.font.lineHeight - 1) / 2;
        final int yMiddleForText = yMiddle - mc.font.lineHeight / 2;

        final int compassScaledWidth = (int) (((float) BritishWeather.getConfig().getCompassSize() / 100f) * (float) scaledWidth); // All these float are dumb
        final int compassScaledWidthHalf = compassScaledWidth / 2;

        final float yaw = (!player.isPassenger() ?
                Mth.lerp(partialTicks, player.yRotO, player.getYRot())
                : player.getYRot()) % 360;
        final int yawi = (int) yaw;

        // Compass background
        drawBackground(guiGraphics, mc, compassScaledWidthHalf, x, yMiddle);

        if (!ClientCommon.PROVIDER.isDown())
        {
            if (didRenderProviders)
            {
                ProviderRegistry.stopTickAll(player);
                HudCompass.addEntry(new PinEntry(player.position()));

                PersistentEntriesManager.save();
            }

            // Render N/E/S/W
            int cardinalDirectionY = yMiddleForText - (mc.font.lineHeight + 3);
            drawCardinal(mc, guiGraphics, yaw, 0, x - 3, cardinalDirectionY, compassScaledWidth, "S");
            drawCardinal(mc, guiGraphics, yaw, 90, x - 3, cardinalDirectionY, compassScaledWidth, "W");
            drawCardinal(mc, guiGraphics, yaw, 180, x - 3, cardinalDirectionY, compassScaledWidth, "N");
            drawCardinal(mc, guiGraphics, yaw, 270, x - 3, cardinalDirectionY, compassScaledWidth, "E");

            // Draw all living entities
            drawEntities(mc, player, guiGraphics, partialTicks, yaw, x, yMiddleForText, compassScaledWidthHalf);

            if (BritishWeather.getConfig().shouldRenderHeading())
            {
                String friendlyDeg = getActualDegreesFromYaw(yawi);
                FontHelper.draw(mc, guiGraphics, friendlyDeg, x - (mc.font.width(friendlyDeg) / 2), getTextLocationY(mc, yMiddleForText), CENTER_COLOR, true, FontHelper.TextType.LABEL);
            }

            didRenderProviders = false;
        } else {
            drawProviders(guiGraphics, mc, player, yawi, compassScaledWidth, x, yMiddleForText, compassScaledWidthHalf);
            didRenderProviders = true;
        }

        // Draw compass heading
        FontHelper.draw(mc, guiGraphics, COMPASS_HEADING, x, yMiddleForText, CENTER_COLOR, false, FontHelper.TextType.NONE);
    }

    public static void addEntry(DefaultEntry provider)
    {
        ProviderRegistry.addEntry(provider);
    }

    public static void tick(@NotNull LocalPlayer player)
    {
        HudCompass.ProviderRegistry.tick(player);
    }

    public static Collection<DefaultEntry> getEntries()
    {
        return HudCompass.ProviderRegistry.getEntries();
    }

    private static void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, Minecraft mc, int compassScaledWidthHalf, int x, int y)
    {
        // Draw 2 lines. One for normal visible line and one for shadow (is there a way to combine this?)
        int farLeftX = x - compassScaledWidthHalf;
        int farRightX = x + compassScaledWidthHalf;
        int aY = y - 1;

        guiGraphicsExtractor.horizontalLine(farLeftX + 1 , farRightX, aY, COMPASS_BG_COLOR);
        guiGraphicsExtractor.horizontalLine(farLeftX + 2, farRightX, aY + 1, COMPASS_BG_SHADOW_COLOUR);

        FontHelper.draw(mc, guiGraphicsExtractor, "<", farLeftX, 1 + aY - mc.font.lineHeight / 2, COMPASS_BG_COLOR, false, FontHelper.TextType.NONE);
        FontHelper.draw(mc, guiGraphicsExtractor, ">", farRightX - 2, 1 + aY - mc.font.lineHeight / 2, COMPASS_BG_COLOR, false, FontHelper.TextType.NONE);
    }

    private static void drawEntities(Minecraft mc, LocalPlayer player, GuiGraphicsExtractor guiGraphics, float partialTicks, float yaw, int compassX, int y, int compassScaledWidth)
    {
        // Where player is looking
        Vec3 look = player.getViewVector(partialTicks).scale(DETECTION_DISTANCE);

        // Get bounding box 40m infront of where player is looking
        int fov = mc.options.fov().get();

        AABB normalBB = player.getBoundingBox();
        AABB inFrontOfPlayer = normalBB
                .expandTowards(look.x, 0, look.z)
                .inflate(EXPAND_INFLATE_WITH_FOV_CONST * fov); // inflate so entities don't get cut off from compass

        // For every LivingEntity (includes all living things but not items or arrows) except current player. Does include armour stands
        mc.level.getEntitiesOfClass(LivingEntity.class, inFrontOfPlayer, (en) -> (en != player.asLivingEntity() && !en.getBoundingBox().intersects(normalBB))).stream().limit(MAX_ALLOWED_ENTITIES_ON_COMPASS).forEach(entity ->
                {

                    double angleFromEntity = MathHelper.angleFromPos(entity.position(), player.position());
                    int distanceFromEntity = MathHelper.getDistance(player.position(), entity.position());
                    int iconScale = Math.abs(SCALE_MAX - (SCALE_MAX / DETECTION_DISTANCE * distanceFromEntity));
                    int livingEntity = getColourForEntity(entity, player, iconScale);

                    // Entity x offset on screen
                    int ex = MathHelper.getCompassScreenX(yaw, (float) angleFromEntity, compassX, compassScaledWidth);
                    if (ex != Integer.MAX_VALUE)
                    {
                        FontHelper.draw(mc, guiGraphics, ENTITY_LABEL, ex, y, livingEntity, true, FontHelper.TextType.NONE);
                    }
                }
        );
    }

    private static void drawCardinal(Minecraft mc, GuiGraphicsExtractor guiGraphics, float yaw, float angle, int x, int y, int compassScaledWidth, String text) {
        int dx = MathHelper.getCompassScreenX(yaw, angle, x, compassScaledWidth);
        if (dx != Integer.MAX_VALUE)
        {
            FontHelper.draw(mc, guiGraphics, text, dx, y, WHITE, FontHelper.TextType.NONE);
        }
    }

    private static void drawProvider(
            GuiGraphicsExtractor guiGraphicsExtractor,
            Minecraft mc,
            DefaultEntry entry,
            LocalPlayer player,
            int yaw,
            int compassScaledWidth,
            int compassWindowX,
            int compassWindowY,
            int compassScaledWidthHalf
    )
    {
        if (!entry.shouldRender())
        {
            return;
        }

        if (!didRenderProviders)
        {
            entry.startTick(player);
        }

        Vec3 playerPos = player.position();
        Vec3 entryPos = entry.getPosition();

        double angleFromPosition = MathHelper.angleFromPos(entryPos, playerPos);
        int compassX = MathHelper.getCompassScreenX(yaw, (float) angleFromPosition, compassWindowX, compassScaledWidth, false);

        FontHelper.draw(mc, guiGraphicsExtractor, entry.getMarker(), compassX - (entry.getMarkerWidthHalf()), compassWindowY, entry.getColour(), FontHelper.TextType.NONE);

        if (entry.shouldShowDistance() && !MathHelper.isXOutOfBounds(compassX, compassScaledWidthHalf, compassWindowX))
        {
            int distance = MathHelper.getDistance(entryPos, playerPos);

            String suffix = "m ";
            double heightDiff = playerPos.y - entryPos.y;
            if (distance <= 200 && heightDiff >= 3)
            {
                suffix = "↓";
            } else if (distance <= 200 && heightDiff <= -3)
            {
                suffix = "↑";
            }

            String distanceFromObjective = MathHelper.getDistance(playerPos, entryPos) + suffix;
            FontHelper.draw(mc, guiGraphicsExtractor, distanceFromObjective, compassX - (mc.font.width(distanceFromObjective) / 2) + 2, getTextLocationY(mc, compassWindowY), entry.getColour(), FontHelper.TextType.LABEL);
        }
    }

    private static void drawProviders(
            GuiGraphicsExtractor guiGraphicsExtractor,
            Minecraft mc,
            LocalPlayer player,
            int yaw,
            int compassScaledWidth,
            int compassWindowX,
            int compassWindowY,
            int compassScaledWidthHalf
    )
    {
        for (DefaultEntry entry : ProviderRegistry.getEntries())
        {
            drawProvider(guiGraphicsExtractor, mc, entry, player, yaw, compassScaledWidth, compassWindowX, compassWindowY, compassScaledWidthHalf);
        }

        for (DefaultEntryGroup group : ProviderRegistry.PROVIDER_GROUPS)
        {
            if (!didRenderProviders)
            {
                group.startTick(player);
            }

            group.getEntries().forEach(groupEntry -> {
                drawProvider(guiGraphicsExtractor, mc, groupEntry, player, yaw, compassScaledWidth, compassWindowX, compassWindowY, compassScaledWidthHalf);
            });
        }
    }

    private static String getActualDegreesFromYaw(int yawi)
    {
        return (Mth.wrapDegrees(yawi) + 180) % 360 + "°";
    }

    private static int getTextLocationY(Minecraft mc, int compassWindowY)
    {
        return compassWindowY + mc.font.lineHeight + 3;
    }

    private static int shiftColourToOpacity(int colour, int opacity)
    {
        return (colour & 0x00FFFFFF) | (opacity << 24);
    }

    private static int getColourForEntity(LivingEntity entity, @Nullable LocalPlayer owner, int opacity)
    {
        switch (entity)
        {
            case Player _ ->
            {
                return shiftColourToOpacity(PLAYER, opacity);
            }
            case Monster _ ->
            {
                return shiftColourToOpacity(HOSTILE, opacity);
            }
            case TamableAnimal tamableAnimal ->
            {
                if (tamableAnimal.getOwnerReference() != null && tamableAnimal.getOwnerReference().getUUID().equals(owner.getUUID()))
                {
                    return shiftColourToOpacity(ALLY, opacity);
                }
            }
            default ->
            {
            }
        }
        return shiftColourToOpacity(NEUTRAL, opacity);
    }
}
