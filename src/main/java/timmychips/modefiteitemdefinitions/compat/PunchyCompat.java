package timmychips.modefiteitemdefinitions.compat;

import net.neoforged.fml.loading.FMLLoader;

/**
 * Hands the use-item animation over to Punchy when it is installed.
 *
 * <p>Punchy animates the hand itself: a pack like Just 3D Potions ships a
 * keyframed {@code potion.animation.json} plus a run of {@code drink_N} frame
 * models, and Punchy steps through them. Item definitions that branch on
 * {@code minecraft:using_item}, {@code minecraft:use_duration} or
 * {@code minecraft:use_cycle} - Weskerson's potion definition does all three -
 * try to drive the same model from this side. With both active they overwrite
 * each other every frame and the drinking animation visibly breaks.
 *
 * <p>Punchy already wrote the arbitration for this. Its jar carries
 * {@code ModefiteUseDurationMixin}, targeting {@code UseDurationFloat.getValue},
 * with strings like {@code use_bow} and {@code start_mesh_animation}. It never
 * runs: {@code punchy.mixins.modefite.json} is declared neither in Punchy's
 * neoforge.mods.toml nor anywhere across its 399 classes. Their intent is
 * clear, so this implements it from our side rather than waiting on a fix.
 *
 * <p>While Punchy is present the three use-based properties report their
 * neutral value, so a definition sees an item that is simply not being used and
 * keeps its idle model. Everything else about a definition still applies -
 * display context, components, damage, and the rest are untouched, and without
 * Punchy nothing changes at all.
 */
public final class PunchyCompat {

    /**
     * Resolved once at class load. The mod list does not change afterwards, and
     * this is read from resolvers that run per item per frame.
     */
    private static final boolean PUNCHY_PRESENT =
            FMLLoader.getLoadingModList().getModFileById("punchy") != null;

    private PunchyCompat() {}

    /** Whether Punchy owns the use-item animation, leaving these properties inert. */
    public static boolean deferUseProperties() {
        return PUNCHY_PRESENT;
    }
}
