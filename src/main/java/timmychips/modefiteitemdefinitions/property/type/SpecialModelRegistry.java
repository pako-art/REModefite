package timmychips.modefiteitemdefinitions.property.type;

import com.google.gson.JsonObject;
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

        // 1.21.9 content. The copper golem does not exist in 1.21.1, so there is
        // no entity model to borrow and no honest way to draw this. Returning
        // null falls back to the definition's base model rather than pretending.
        register("copper_golem_statue", fields -> null);
    }
}
