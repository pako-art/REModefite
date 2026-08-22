package timmychips.modefiteitemdefinitions.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import timmychips.modefiteitemdefinitions.property.type.ItemModelTypes;
import timmychips.modefiteitemdefinitions.property.type.codec.ItemModelRootDefinition;

/**
 * Lets the JSON boolean, "hand_animation_on_swap" disable the equipment animation when swapping items
 */
@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemSwapMixin {

    ///  Not accurate to vanilla, simply disables any swapping animation for item models (both for main hand and offhand)
    @Unique
    private float doModelHandSwap(float equipProgress) {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            ItemStack heldItem = !clientPlayer.getMainHandItem().isEmpty() ? clientPlayer.getMainHandItem() : clientPlayer.getOffhandItem();
            ResourceLocation heldId = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
            ItemModelRootDefinition def = ItemModelTypes.Registry.getRoot(heldId); // Get items model definition for item model

            if (def != null && !def.handAnimationSwap()) return 0F; // Disable hand animation swap if current held item model has hand swap set to false
        }
        return equipProgress; // Normal hand animation swap
    }

    /// Accurate to vanilla, disables the main hand to offhand animation swap
    @Unique
    private float doModelOffHandSwap(float equipProgress) {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            ItemStack offhandItem = clientPlayer.getOffhandItem();
            ResourceLocation offhandId = BuiltInRegistries.ITEM.getKey(offhandItem.getItem());
            ItemModelRootDefinition def = ItemModelTypes.Registry.getRoot(offhandId); // Get items model definition for item model

            if (def != null && !def.handAnimationSwap()) return 0F; // Disable offhand animation swap if current held item model has hand swap set to false
        }
        return equipProgress; // Normal hand animation swap
    }

    @ModifyArg(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemInHandRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;IFFLnet/minecraft/util/HumanoidArm;)V"
            ),
            index = 3 // equipProgress parameter index
    )
    private float modefite$disableEmptyHandEquipProgress(float equipProgress) {
        return doModelOffHandSwap(equipProgress);
    }

    @ModifyArg(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemInHandRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/util/HumanoidArm;F)V"
            ),
            index = 2 // equipProgress parameter index
    )
    private float modefite$disableItemEquipProgress(float equipProgress) {
        return doModelOffHandSwap(equipProgress);
    }
}
