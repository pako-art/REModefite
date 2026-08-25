package timmychips.modefiteitemdefinitions.property.tint;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The tint list of the model currently being drawn, if it declared one.
 *
 * <p>{@code ItemColors.getColor} is handed a stack and a tint index and nothing
 * else — it has no idea which item definition produced the model, and vanilla
 * has no reason to give it one. So the list is parked here for the duration of
 * the render call and read from the mixin on that method.
 *
 * <p>A plain static field rather than a ThreadLocal: written and read on the
 * render thread only, on a path that runs per item per frame. Cleared in a
 * finally block so an exception mid-render cannot leave a stale list tinting
 * the next item.
 */
public final class ActiveTints {

    @Nullable
    private static List<TintSource> current;

    private ActiveTints() {}

    public static void set(@Nullable List<TintSource> tints) {
        current = (tints == null || tints.isEmpty()) ? null : tints;
    }

    public static void clear() {
        current = null;
    }

    /** The source for this tint index, or null to let vanilla answer. */
    @Nullable
    public static TintSource forIndex(int tintIndex) {
        List<TintSource> tints = current;
        if (tints == null || tintIndex < 0 || tintIndex >= tints.size()) return null;
        return tints.get(tintIndex);
    }
}
