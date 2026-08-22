package timmychips.modefiteitemdefinitions.property.resolver;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.PackType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds a vanilla-only fallback ResourceProvider, for retrying a shader compile without any active resourcepack's shaders/core interfering.
 */
public class VanillaShaderFactory {
    private static final Map<String, ResourceProvider> CACHE = new ConcurrentHashMap<>();

    public static void clearCache() {
        CACHE.clear();
    }

    public static ResourceProvider get(String namespace) {
        return CACHE.computeIfAbsent(namespace, VanillaShaderFactory::build);
    }

    private static ResourceProvider build(String namespace) {
        Pack vanilla = Minecraft.getInstance().getResourcePackRepository().getProfile("vanilla");
        FallbackResourceManager manager = new FallbackResourceManager(PackType.CLIENT_RESOURCES, namespace);
        manager.addPack(vanilla.open());
        return manager;
    }
}
