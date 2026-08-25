package timmychips.modefiteitemdefinitions.bakedmodels;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A model with an affine transform applied to it.
 *
 * <p>26.x added a {@code transformation} field on a model definition, and uses
 * it where earlier versions used a hardcoded renderer: in 26.2 a bed is no
 * longer {@code special}, it is a composite of two ordinary block models, one
 * of them rotated into place by this field. All 51 uses in that version carry
 * the same four keys, which is exactly {@link Transformation}'s own shape, so
 * its {@code CODEC} reads them without any parsing of ours:
 *
 * <pre>{@code
 * "transformation": { "left_rotation":  [0,-0,0,1],
 *                     "right_rotation": [0, 0,0,1],
 *                     "scale":       [0.667,-0.667,-0.667],
 *                     "translation": [0.5, 0, 0.5] }
 * }</pre>
 *
 * <p>The transform is applied to the pose rather than baked into the quads.
 * That keeps the wrapped model shared and untouched — several definitions can
 * transform the same model differently — and costs one matrix multiply per
 * draw instead of rebuilding geometry.
 *
 * <p>Rotations arrive as quaternions, which can express any orientation. That
 * is the same thing Blockbench's multi-axis element rotation needs and vanilla
 * has never read, so this is the piece that makes supporting it possible.
 */
public class TransformedItemModel implements BakedModel {

    private final BakedModel wrapped;
    private final Transformation transformation;

    public TransformedItemModel(BakedModel wrapped, Transformation transformation) {
        this.wrapped = wrapped;
        this.transformation = transformation;
    }

    public BakedModel wrapped() {
        return wrapped;
    }

    public Transformation transformation() {
        return transformation;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return wrapped.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return wrapped.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return wrapped.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return wrapped.getOverrides();
    }
}
