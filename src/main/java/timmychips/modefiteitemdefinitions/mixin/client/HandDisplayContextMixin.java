package timmychips.modefiteitemdefinitions.mixin.client;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import timmychips.modefiteitemdefinitions.objects.HandDisplayContext;

/**
 * Records which display context the hand is currently rendering in.
 *
 * <p>{@code ItemRenderer.getModel} carries no display context in its signature,
 * so the mixin on it resolved every request as {@code GUI}. That is wrong
 * anywhere but the inventory, and the mod papered over it by intercepting
 * {@code renderStatic} afterwards and re-rendering with the right context.
 *
 * <p>That patch-up only holds while {@code renderStatic} is on the path. Punchy
 * cancels the vanilla arm rendering and drives the hand itself, so the
 * correction never runs and the GUI answer is what gets drawn. Weskerson's
 * lantern shows it plainly: its definition selects {@code item/lantern} for
 * gui/ground/fixed and {@code item/lantern_hand} for everything else, so asking
 * in GUI context returns the wrong branch - and if that branch's model is not
 * installed, no branch at all, leaving a vanilla lantern in hand.
 *
 * <p>{@code ItemInHandRenderer.renderItem} does receive the context, and both
 * the vanilla path and Punchy go through it. Recording it here lets getModel
 * answer for the context actually being drawn.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class HandDisplayContextMixin {

    @Inject(
            method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void modefite$rememberContext(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.item.ItemStack stack,
                                          ItemDisplayContext context, boolean leftHanded,
                                          com.mojang.blaze3d.vertex.PoseStack poseStack,
                                          net.minecraft.client.renderer.MultiBufferSource buffer, int light,
                                          CallbackInfo ci) {
        HandDisplayContext.set(context);
    }

    @Inject(
            method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void modefite$forgetContext(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.item.ItemStack stack,
                                        ItemDisplayContext context, boolean leftHanded,
                                        com.mojang.blaze3d.vertex.PoseStack poseStack,
                                        net.minecraft.client.renderer.MultiBufferSource buffer, int light,
                                        CallbackInfo ci) {
        HandDisplayContext.clear();
    }
}
