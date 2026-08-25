package timmychips.modefiteitemdefinitions.bakedmodels;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A model drawn by the vanilla special-item renderer rather than from quads.
 *
 * <p>Wraps the {@code base} model of a {@code minecraft:special} definition and
 * carries the proxy {@link ItemStack} that
 * {@link net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer#renderByItem}
 * should be handed. Everything that is not the geometry — transforms,
 * overrides, particle icon — is delegated to the base, so an inventory slot or
 * a dropped item still looks right.
 *
 * <p>{@link #isCustomRenderer()} returns true, which is how vanilla is told to
 * route this through the block-entity renderer instead of drawing quads.
 * {@code HeldItemMixin} intercepts that call to substitute {@link #proxyStack()}
 * for the real stack: without the substitution the renderer would dispatch on
 * whatever item is actually held, and a definition that draws a shield on a
 * stick would draw a stick.
 */
public class SpecialItemModel implements BakedModel {

    private final BakedModel base;
    private final ItemStack proxyStack;

    public SpecialItemModel(BakedModel base, ItemStack proxyStack) {
        this.base = base;
        this.proxyStack = proxyStack;
    }

    /** The stack the vanilla renderer should dispatch on. */
    public ItemStack proxyStack() {
        return proxyStack;
    }

    /** The flat model behind it, used wherever the special renderer is not. */
    public BakedModel base() {
        return base;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return base.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return base.usesBlockLight();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return base.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return base.getOverrides();
    }
}
