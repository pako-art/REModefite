package timmychips.modefiteitemdefinitions.bakedmodels;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import timmychips.modefiteitemdefinitions.property.resolver.ResolveRecursive;
import timmychips.modefiteitemdefinitions.property.type.codec.ItemModelDefinition;

import java.util.List;
import java.util.function.Supplier;

public class CompositeItemModel implements BakedModel {

    private final List<ItemModelDefinition> modelParts;
    private final ItemDisplayContext renderMode;
    private final ItemStack stack;
    private final LivingEntity entity;

    public CompositeItemModel(List<ItemModelDefinition> modelDefinitions, ItemDisplayContext renderMode, ItemStack stack, LivingEntity entity) {
        this.modelParts = modelDefinitions;
        this.renderMode = renderMode;
        this.stack = stack;
        this.entity = entity;
    }

    // Return list of models from this instance
    public List<BakedModel> getModels() {
        return modelParts.stream()
                .map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .toList();
    }

    // Get quads for each model part
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return modelParts.stream()
                .map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .flatMap(baked -> baked.getQuads(state, face, random).stream())
                .toList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return modelParts.stream()
                .map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .anyMatch(BakedModel::useAmbientOcclusion);
    }

    @Override
    public boolean isGui3d() {
        return modelParts.stream().map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .anyMatch(BakedModel::isGui3d);
    }

    @Override
    public boolean usesBlockLight() {
        return modelParts.stream().map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .anyMatch(BakedModel::usesBlockLight);
    }

    @Override
    public boolean isCustomRenderer() {
        return modelParts.stream().map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .anyMatch(BakedModel::isCustomRenderer);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return modelParts.stream()
                .map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .findFirst() // pick the first resolved model
                .map(BakedModel::getParticleIcon)
                .orElse(Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon());
    }

    @Override
    public ItemTransforms getTransformation() {
        List<BakedModel> children = modelParts.stream()
                .map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .toList();

        for (BakedModel part : children) {
            return part.getTransforms();
        }
        return ItemTransforms.NONE;
    }

    // Get item overrides; doesn't matter too much though since we're replacing the item override system
    @Override
    public ItemOverrides getOverrides() {
        return modelParts.stream()
                .map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .findFirst()
                .map(BakedModel::getOverrides)
                .orElse(ItemOverrides.EMPTY);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false; // False to trigger FabricBakedModel rendering
    }

    // Emit item quads for each model part
    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        modelParts.stream()
                .map(part -> ResolveRecursive.resolve(part, renderMode, stack, entity)
                        .orElse(ResolveRecursive.getMissingModel()))
                .forEach(baked -> baked.emitItemQuads(stack, randomSupplier, context));
    }
}
