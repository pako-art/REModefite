package timmychips.modefiteitemdefinitions.mixin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Decides which mixins to apply, so one of them can stand down for Punchy.
 *
 * <p>{@code HeldItemSwapMixin} uses {@code @ModifyArg} on the
 * {@code renderPlayerArm} and {@code applyItemArmTransform} calls inside
 * {@code ItemInHandRenderer.renderArmWithItem}. Punchy hooks that same method:
 * it cancels the vanilla arms and installs its own transform baseline. With
 * both active the held item renders at the wrong size and position - verified
 * by isolation, and the official NeoForge build has it too.
 *
 * <p>Punchy already owns hand rendering end to end, so the honest resolution is
 * to yield rather than to fight for ordering. Mixin priority cannot settle this
 * anyway: it only orders mixins that share a target class, and the rest of this
 * mod patches {@code ItemRenderer}, which Punchy does not touch.
 *
 * <p>What is given up is narrow: the {@code hand_animation_on_swap} field in an
 * item definition stops being honoured while Punchy is installed. Without
 * Punchy everything applies as before.
 */
public class ModefiteMixinPlugin implements IMixinConfigPlugin {

    private static final String HELD_ITEM_SWAP = "timmychips.modefiteitemdefinitions.mixin.client.HeldItemSwapMixin";

    private boolean punchyPresent;

    @Override
    public void onLoad(String mixinPackage) {
        punchyPresent = FMLLoader.getLoadingModList().getModFileById("punchy") != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (HELD_ITEM_SWAP.equals(mixinClassName) && punchyPresent) {
            return false;
        }
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
