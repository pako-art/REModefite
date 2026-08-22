package timmychips.modefiteitemdefinitions.mixin.client;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.MatrixUtil;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import timmychips.modefiteitemdefinitions.bakedmodels.CompositeItemModel;
import timmychips.modefiteitemdefinitions.property.type.ItemModelTypes;

import java.util.List;
import java.util.Optional;

import static timmychips.modefiteitemdefinitions.property.resolver.ItemModelResolver.resolveModel;

// Mixin injects into target ItemRenderer vanilla class
@Mixin(ItemRenderer.class)
public abstract class HeldItemMixin {

    @Unique
    private static final ThreadLocal<Boolean> RENDERING_LIVING_ENTITY = ThreadLocal.withInitial(() -> false);

    // Gets custom model for GUI model mode so the item model changes for the GUI
    @Inject(method = "getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/Level;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At("HEAD"),
            cancellable = true)
    private void modefite$overrideGUIModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        BakedModel gui_model = getCustomModel(stack, entity, ItemDisplayContext.GUI);
        if (gui_model != null) {
            cir.setReturnValue(gui_model);
        }
    }

    @Shadow
    private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer vertices) {/*dummy body*/}

    @Shadow
    private final BlockEntityWithoutLevelRenderer builtinModelItemRenderer = this.builtinModelItemRenderer;

    @Shadow
    private static boolean usesDynamicDisplay(ItemStack stack) {
        return stack.is(ItemTags.COMPASSES) || stack.is(Items.CLOCK);
    }

    @Shadow
    public static VertexConsumer getDirectItemGlintConsumer(MultiBufferSource provider, RenderType layer, boolean solid, boolean glint) {
        return glint
                ? VertexMultiConsumer.create(provider.getBuffer(solid ? RenderType.glint() : RenderType.entityGlintDirect()), provider.getBuffer(layer))
                : provider.getBuffer(layer);
    }

    /**
     * Performs item renderer methods for each baked model if baked model is a composite item model
     * <p> Code is mostly from vanilla target method with the major difference being it performs the method for each modelPart of the composite item model
     */
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ItemDisplayContext;ZLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void modefite$renderCompositeOrItemEntityModel(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            ///  Composite model types
            if (model instanceof CompositeItemModel compositeModel) {
                // Retrieve list of baked models from CompositeItemModel object
                List<BakedModel> models = compositeModel.getModels();

                if (models != null) {
                    for (BakedModel modelPart : models) {
                        // Code is from vanilla method with minor tweaks
                        matrices.pushPose();

                        boolean bl = renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED;
                        modelPart.getTransforms().getTransform(renderMode).apply(leftHanded, matrices);
                        matrices.translate(-0.5F, -0.5F, -0.5F);

                        if (!model.isCustomRenderer() || bl) {
                            RenderType renderLayer = ItemBlockRenderTypes.getRenderType(stack, true);
                            VertexConsumer vertexConsumer;

                            vertexConsumer = getDirectItemGlintConsumer(vertexConsumers, renderLayer, true, stack.hasFoil());

                            PoseStack.Pose entry = matrices.last().copy();
                            if (renderMode == ItemDisplayContext.GUI) {
                                entry.pose().scale(0.5F);
                            } else if (renderMode.firstPerson()) {
                                entry.pose().scale(0.75F);
                            }

                            this.renderBakedItemModel(modelPart, stack, light, overlay, matrices, vertexConsumer);
                        }
                        else {
                            this.builtinModelItemRenderer.renderByItem(stack, renderMode, matrices, vertexConsumers, light, overlay);
                        }
                        matrices.popPose(); // Pop matrix to prevent render errors for next element in list
                    }
                }
                ci.cancel(); // Cancel rest of method for composite item models
            }

            else {
                ///  For Ground or null LivingEntity types (such as ItemEntities)
                if (RENDERING_LIVING_ENTITY.get()) return;

                if (renderMode == ItemDisplayContext.GROUND) {
                    // For null LivingEntities (e.g. renders the ground render mode for items thrown onto ground)
                    BakedModel customModel = getCustomModel(stack, null, renderMode);
                    if (customModel != null && customModel != model) {
                        ItemRenderer self = (ItemRenderer) (Object) this;
                        self.render(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, customModel);
                        ci.cancel();
                    }
                }
            }
        }
    }

    // Replaces entity item render with our custom model
    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ItemDisplayContext;ZLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;Lnet/minecraft/world/Level;III)V",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void modefite$interceptRender(LivingEntity entity, ItemStack item, ItemDisplayContext renderMode, boolean leftHanded,
                                        PoseStack matrices, MultiBufferSource vertexConsumers, Level world,
                                        int light, int overlay, int seed, CallbackInfo ci) {

        if (RENDERING_LIVING_ENTITY.get()) {
            // Already in recursive rendering call — skip this render method call
            return;
        }

        RENDERING_LIVING_ENTITY.set(true); // Set if it's rendering model for a valid LivingEntity

        try {
            BakedModel model = getCustomModel(item, entity, renderMode);

            if (model != null) {
                ItemRenderer self = (ItemRenderer)(Object) this;
                // manually call vanilla rendering method with overridden model
                self.render(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
                ci.cancel(); // skip original call
            }
        } finally {
            RENDERING_LIVING_ENTITY.set(false); // Set boolean back to false if LivingEntity is null or finished rendering in this method call
        }
    }

    /*
    @Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ItemDisplayContext;ZLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;Lnet/minecraft/world/Level;III)V",
            at = @At("HEAD")
    )
    private void modefite$markEntityRenderStart(LivingEntity entity, ItemStack stack, ItemDisplayContext mode, boolean leftHanded,
                                                 PoseStack matrices, MultiBufferSource vertexConsumers, Level world,
                                                 int light, int overlay, int seed, CallbackInfo ci) {
        RENDERING_ENTITY.set(true); // this works?
    }

     */

    @Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ItemDisplayContext;ZLnet/minecraft/client/util/math/PoseStack;Lnet/minecraft/client/render/MultiBufferSource;Lnet/minecraft/world/Level;III)V",
            at = @At("RETURN")
    )
    private void modefite$markEntityRenderEnd(LivingEntity entity, ItemStack stack, ItemDisplayContext mode, boolean leftHanded,
                                               PoseStack matrices, MultiBufferSource vertexConsumers, Level world,
                                               int light, int overlay, int seed, CallbackInfo ci) {
        RENDERING_LIVING_ENTITY.set(false);
    }


    // Get custom model from BakedModelManger's getModel from id (which is needed since we loaded the models with ModelLoadingPlugin)
    @Unique
    private static BakedModel getCustomModel(ItemStack stack, LivingEntity entity, ItemDisplayContext mode) {

        ModelManager missingModelManager = Minecraft.getInstance().getModelManager();

        // If item's items model definition has an invalid model type, returns missing item model
        for (ResourceLocation id : ItemModelTypes.Registry.INVALID_MODEL_TYPES) {
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id)) { // Checks if INVALID_TYPES Set contains item id
                return missingModelManager.getMissingModel(); // Item renders as Missing Model
            }
        }

        if (mode == null) mode = ItemDisplayContext.GUI;

        Optional<BakedModel> maybeModel = resolveModel(BuiltInRegistries.ITEM.getKey(stack.getItem()), mode, stack, entity);
        if (maybeModel != null && maybeModel.isPresent()) {
            return maybeModel.get();
        }
        return null;
    }
}

