package timmychips.modefiteitemdefinitions.objects;

import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;

/**
 * The display context the hand renderer is currently drawing in, if any.
 *
 * <p>Read by the {@code ItemRenderer.getModel} hook, which has no context of
 * its own and would otherwise answer every request as GUI. Set only around
 * {@code ItemInHandRenderer.renderItem}; null everywhere else, which correctly
 * means "not rendering a hand right now".
 *
 * <p>A plain static field, not a ThreadLocal: this is written and read on the
 * render thread only, and it sits on a path that runs per item per frame.
 */
public final class HandDisplayContext {

    @Nullable
    private static ItemDisplayContext current;

    private HandDisplayContext() {}

    public static void set(ItemDisplayContext context) {
        current = context;
    }

    public static void clear() {
        current = null;
    }

    @Nullable
    public static ItemDisplayContext get() {
        return current;
    }
}
