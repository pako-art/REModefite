package timmychips.modefiteitemdefinitions.mixin.client;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import timmychips.modefiteitemdefinitions.property.tint.ActiveTints;
import timmychips.modefiteitemdefinitions.property.tint.TintSource;

/**
 * Lets a definition's {@code tints} array decide the colour of a tint index.
 *
 * <p>1.21.1 colours items through {@code ItemColors}: a function registered per
 * item, in code. The item definition system replaces that with data, and this
 * is where the two meet — when a definition declared tints, its source for the
 * requested index answers instead of the registered function.
 *
 * <p>Only takes over for indices the definition actually covers. Anything else
 * falls through to vanilla, so an item that has both a registered colour
 * function and a partial tint list keeps working.
 */
@Mixin(ItemColors.class)
public abstract class ItemColorsTintMixin {

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private void modefite$applyDefinitionTint(ItemStack stack, int tintIndex, CallbackInfoReturnable<Integer> cir) {
        TintSource source = ActiveTints.forIndex(tintIndex);
        if (source != null) {
            cir.setReturnValue(source.colour(stack));
        }
    }
}
