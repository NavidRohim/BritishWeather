package me.brynview.navidrohim.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import me.brynview.navidrohim.Constants;
import me.brynview.navidrohim.client.hud.compass.Compass;
import me.brynview.navidrohim.util.ColorHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

public class KeyHandler
{
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "keybinds"));
    private static final String KEY_BASE = "br.keybind.";
    private static final int SPACING = 4;
    private static final int HOTBAR_HEIGHT_WITH_SPACING = 20;

    private static String getBindingKey(String bindingBaseName)
    {
        return KEY_BASE + bindingBaseName;
    }

    public static final class KeybindWrapper
    {
        public final KeyMapping keybind;
        public final Component description;
        private final Consumer<Compass> action;
        private final boolean showInTable;
        private boolean isHeld = false;

        public KeybindWrapper(String name, int key, Consumer<Compass> action, boolean showInTable)
        {
            String bindingKey = getBindingKey(name);
            this.keybind = new KeyMapping(bindingKey, key, CATEGORY);
            this.description = Component.translatable(bindingKey);
            this.action = action;
            this.showInTable = showInTable;
        }

        public Component getFormattedDescriptionString()
        {
            return Component.literal("%s + \"%s\" = %s".formatted(ENTRY_HUD_KEY.getTranslatedKeyMessage().getString(), this.keybind.getTranslatedKeyMessage().getString(), this.description.getString()));
        }
    }

    public static final KeyMapping ZOOM_MODIFIER = new KeyMapping(
            getBindingKey("zoom_modifier"),
            InputConstants.KEY_RALT,
            CATEGORY
    );

    public static final KeyMapping ENTRY_HUD_KEY = new KeyMapping(
            getBindingKey("showPins"), // The translation key for the key mapping.
            InputConstants.KEY_LALT, // The keycode of the key.
            CATEGORY // The category of the mapping.
    );

    public static final List<KeybindWrapper> KEYBIND_WRAPPERS = List.of(
            new KeybindWrapper("delete_pin", InputConstants.KEY_BACKSPACE, Compass::delete, true),
            new KeybindWrapper("create_pin", InputConstants.KEY_N, Compass::newPin, true),
            new KeybindWrapper("find_pin", InputConstants.KEY_X, Compass::find, true),
            new KeybindWrapper("edit_pin", InputConstants.KEY_B, Compass::editPin, true),
            new KeybindWrapper("track_pin", InputConstants.KEY_MINUS, Compass::track, true),
            new KeybindWrapper("debug", InputConstants.KEY_EQUALS, Compass::teleport, false)
    );

    public static void tick()
    {
        if (ENTRY_HUD_KEY.isDown())
        {
            Compass compass = Compass.getInstance();
            for (KeybindWrapper keybindWrapper : KEYBIND_WRAPPERS)
            {
                if (keybindWrapper.keybind.isDown() && !keybindWrapper.isHeld)
                {
                    keybindWrapper.action.accept(compass);
                    keybindWrapper.isHeld = true;
                } else if (!keybindWrapper.keybind.isDown() && keybindWrapper.isHeld)
                {
                    keybindWrapper.isHeld = false;
                }
            }
        }
    }

    public static void extractKeyHelper(GuiGraphicsExtractor guiGraphicsExtractor, Compass.CompassRenderContext context)
    {
        int i = 0;
        for (KeybindWrapper keybindWrapper : KEYBIND_WRAPPERS)
        {
            if (keybindWrapper.showInTable)
            {
                i++;
                int y = guiGraphicsExtractor.guiHeight() - (HOTBAR_HEIGHT_WITH_SPACING + (SPACING + 9 * i)); // 25 is hotbar height (22px) plus 3px for spacing. 9 is lineHeight
                guiGraphicsExtractor.text(context.font, keybindWrapper.getFormattedDescriptionString(), HOTBAR_HEIGHT_WITH_SPACING, y, ColorHelper.WHITE);
            }
        }
    }
}
