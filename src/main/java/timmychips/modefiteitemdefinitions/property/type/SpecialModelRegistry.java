package timmychips.modefiteitemdefinitions.property.type;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import timmychips.modefiteitemdefinitions.ClientInitializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Sub-type table for the {@code minecraft:special} model type.
 *
 * <p>Vanilla renders these through {@link net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer},
 * and that renderer dispatches on the <em>item</em> it is handed: {@code is(Items.SHIELD)},
 * {@code instanceof BlockItem} then the block type, and so on. So a sub-type
 * does not need its own rendering code — it needs to produce the vanilla
 * ItemStack that makes the existing renderer take the right branch. Colour,
 * banner patterns and skull kind all come out of that stack's item and
 * components, exactly as they do for a real held shield or bed.
 *
 * <p>The table is open on purpose. Mojang added {@code copper_golem_statue} and
 * {@code player_head} in 1.21.9 and none since, but it is also moving the other
 * way: in 26.2 beds stopped being {@code special} and became a
 * {@code composite} of two ordinary models. Registration keeps new sub-types
 * from touching the resolver, and lets an unknown one fail softly instead of
 * throwing.
 */
public final class SpecialModelRegistry {

    /** Builds the proxy stack a sub-type renders as, or null if it cannot be represented. */
    @FunctionalInterface
    public interface Factory {
        @Nullable ItemStack proxyFor(JsonObject fields);
    }

    private static final Map<String, Factory> FACTORIES = new HashMap<>();

    private SpecialModelRegistry() {}

    public static void register(String type, Factory factory) {
        FACTORIES.put(type, factory);
    }

    /**
     * The stack to hand {@code renderByItem}, or null when this sub-type has no
     * representation on this version.
     */
    @Nullable
    public static ItemStack proxyFor(String type, JsonObject fields) {
        Factory f = FACTORIES.get(type);
        if (f == null) {
            ClientInitializer.LOGGER.warn("Unknown special model type '{}'. Falling back to its base model.", type);
            return null;
        }
        return f.proxyFor(fields);
    }

    static {
        // Fieldless sub-types. The item alone is enough for the vanilla renderer.
        register("shield",        fields -> new ItemStack(Items.SHIELD));
        register("trident",       fields -> new ItemStack(Items.TRIDENT));
        register("conduit",       fields -> new ItemStack(Items.CONDUIT));
        register("decorated_pot", fields -> new ItemStack(Items.DECORATED_POT));

        // Sub-types carrying a field. Each names a vanilla item whose own
        // components already tell the renderer what to draw, so the field is
        // only ever used to pick that item.
        register("banner",      fields -> byId(str(fields, "color"), "_banner"));
        register("bed",         fields -> byId(str(fields, "texture"), "_bed"));
        register("shulker_box", fields -> byId(shulkerColour(str(fields, "texture")), "_shulker_box"));
        register("chest",       fields -> chestFor(str(fields, "texture")));
        register("head",        fields -> byId(str(fields, "kind"), "_head", "_skull"));
        register("player_head", fields -> new ItemStack(Items.PLAYER_HEAD));

        // 1.21.9 content. The copper golem does not exist in 1.21.1, so there is
        // no entity model to borrow and no honest way to draw this. Returning
        // null falls back to the definition's base model rather than pretending.
        register("copper_golem_statue", fields -> null);
    }

    /** A field's value with any namespace stripped; null when absent. */
    @Nullable
    private static String str(JsonObject fields, String key) {
        return fields.has(key) && fields.get(key).isJsonPrimitive()
                ? fields.get(key).getAsString().replace("minecraft:", "")
                : null;
    }

    /**
     * Resolves {@code <value><suffix>} against the item registry, trying each
     * suffix in turn.
     *
     * <p>Heads need two: vanilla names them {@code zombie_head} and
     * {@code creeper_head} but {@code skeleton_skull} and {@code wither_skeleton_skull}.
     */
    @Nullable
    private static ItemStack byId(@Nullable String value, String... suffixes) {
        if (value == null) return null;
        for (String suffix : suffixes) {
            var item = BuiltInRegistries.ITEM.getOptional(
                    ResourceLocation.withDefaultNamespace(value + suffix)).orElse(null);
            if (item != null && item != Items.AIR) return new ItemStack(item);
        }
        ClientInitializer.LOGGER.warn("No item for special model value '{}'. Falling back to the base model.", value);
        return null;
    }

    /** {@code shulker_yellow} names the texture, not the item; the item is {@code yellow_shulker_box}. */
    @Nullable
    private static String shulkerColour(@Nullable String texture) {
        if (texture == null) return null;
        return texture.startsWith("shulker_") ? texture.substring("shulker_".length()) : texture;
    }

    /**
     * Chest textures do not map onto item ids the way the others do:
     * {@code normal}, {@code trapped} and {@code ender} are variants of one
     * block, while 1.21.9's {@code copper} chests have no 1.21.1 counterpart.
     */
    @Nullable
    private static ItemStack chestFor(@Nullable String texture) {
        if (texture == null) return new ItemStack(Items.CHEST);
        return switch (texture) {
            case "trapped" -> new ItemStack(Items.TRAPPED_CHEST);
            case "ender"   -> new ItemStack(Items.ENDER_CHEST);
            case "normal"  -> new ItemStack(Items.CHEST);
            default -> {
                ClientInitializer.LOGGER.warn("Chest texture '{}' has no 1.21.1 equivalent; drawing a normal chest.", texture);
                yield new ItemStack(Items.CHEST);
            }
        };
    }
}
