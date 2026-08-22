package timmychips.modefiteitemdefinitions.mixin.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import timmychips.modefiteitemdefinitions.property.resolver.VanillaShaderFactory;

import java.io.IOException;

/**
 * Keeps a single incompatible core shader (e.g. from a pack built for 1.21.4+'s shader format) from aborting the whole shader reload.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererShaderResilienceMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Redirect(method = "reloadShaders(Lnet/minecraft/server/packs/resources/ResourceProvider;)V",
            at = @At(value = "NEW", target = "Lnet/minecraft/client/renderer/ShaderInstance;"))
    private ShaderInstance modefite$safeConstructShaderProgram(ResourceProvider factory, String name, VertexFormat format) throws IOException {
        try {
            return new ShaderInstance(factory, name, format);
        } catch (IOException e) {
            LOGGER.warn("MODEFITE: shader '{}' failed to compile from active resourcepacks, falling back to vanilla ({})", name, e.getMessage());
            return new ShaderInstance(VanillaShaderFactory.get("minecraft"), name, format);
        }
    }
}
