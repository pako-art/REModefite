package timmychips.modefiteitemdefinitions.property.tint;

import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.GrassColor;
import timmychips.modefiteitemdefinitions.ClientInitializer;

import java.util.HashMap;
import java.util.Map;

/**
 * A colour for one tint index of a model.
 *
 * <p>The {@code tints} array on a {@code minecraft:model} definition replaces
 * what 1.21.1 does through {@code ItemColors}: a hardcoded per-item colour
 * function. Each entry answers for one tint index, in order, and each pulls its
 * colour from somewhere different — a literal, a component on the stack, or a
 * biome computation.
 *
 * <p>Until now the array was dropped silently, because the codec ignores fields
 * it does not model. A pack tinting leather armour or a potion got no colour at
 * all, which shows up as grey rather than as an error.
 *
 * <p>The six types have not changed between 1.21.4 and 1.21.9, and all six have
 * a 1.21.1 equivalent, so this is a complete implementation rather than a
 * partial one. Registration is open for the same reason as the special
 * sub-types: a seventh should not need to touch this file's callers.
 */
@FunctionalInterface
public interface TintSource {

    /** ARGB for this tint index, given the stack being drawn. */
    int colour(ItemStack stack);

    /** Builds a source from its JSON, or null if the type is unknown. */
    @FunctionalInterface
    interface Factory {
        TintSource create(JsonObject json);
    }

    Map<String, Factory> FACTORIES = new HashMap<>();

    static void register(String type, Factory factory) {
        FACTORIES.put(type, factory);
    }

    static TintSource fromJson(JsonObject json) {
        String type = json.has("type")
                ? json.get("type").getAsString().replace("minecraft:", "")
                : "";
        Factory f = FACTORIES.get(type);
        if (f == null) {
            ClientInitializer.LOGGER.warn("Unknown tint source '{}'; that layer will be left untinted.", type);
            return stack -> -1;
        }
        return f.create(json);
    }

    private static int intField(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static float floatField(JsonObject json, String key, float fallback) {
        return json.has(key) ? json.get(key).getAsFloat() : fallback;
    }

    static void init() {
        // A literal ARGB. No stack involved.
        register("constant", json -> {
            int value = intField(json, "value", -1);
            return stack -> value;
        });

        // The dyed_color component, as leather armour uses.
        register("dye", json -> {
            int fallback = intField(json, "default", -1);
            return stack -> DyedItemColor.getOrDefault(stack, fallback);
        });

        // Biome grass colour. The two parameters are baked into the definition
        // rather than read from the world, so this is constant per definition -
        // vanilla does the same.
        register("grass", json -> {
            float temperature = floatField(json, "temperature", 0.5F);
            float downfall = floatField(json, "downfall", 1.0F);
            int colour = GrassColor.get(temperature, downfall);
            return stack -> colour;
        });

        // Potion colour, including custom colours set on the component.
        register("potion", json -> {
            int fallback = intField(json, "default", -1);
            return stack -> {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                return contents == null ? fallback : contents.getColor();
            };
        });

        // First explosion's first colour, which is what vanilla shows on the star.
        register("firework", json -> {
            int fallback = intField(json, "default", -1);
            return stack -> {
                FireworkExplosion explosion = stack.get(DataComponents.FIREWORK_EXPLOSION);
                if (explosion == null) return fallback;
                var colours = explosion.colors();
                return colours.isEmpty() ? fallback : colours.getInt(0) | 0xFF000000;
            };
        });

        register("map_color", json -> {
            int fallback = intField(json, "default", -1);
            return stack -> {
                MapItemColor colour = stack.get(DataComponents.MAP_COLOR);
                return colour == null ? fallback : colour.rgb() | 0xFF000000;
            };
        });
    }
}
