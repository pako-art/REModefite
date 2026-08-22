package timmychips.modefiteitemdefinitions.mixin.client;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import timmychips.modefiteitemdefinitions.property.resolver.ArmorTextureRedirect;

/**
 * Redirects legacy worn-armor texture lookups to a pack's 1.21.2+ equipment texture when present.
 */
@Mixin(ArmorMaterial.Layer.class)
public abstract class ArmorTextureRedirectMixin {
    @Inject(method = "texture", at = @At("RETURN"), cancellable = true)
    private void modefite$redirectToNewEquipmentTexture(boolean secondLayer, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation legacy = cir.getReturnValue();
        ResourceLocation redirected = ArmorTextureRedirect.redirect(legacy);
        if (redirected != legacy) cir.setReturnValue(redirected);
    }
}
