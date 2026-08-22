package timmychips.modefiteitemdefinitions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import timmychips.modefiteitemdefinitions.property.registry.ConditionPropertyRegistry;
import timmychips.modefiteitemdefinitions.property.registry.RangePropertyRegistry;
import timmychips.modefiteitemdefinitions.property.registry.SelectPropertyRegistry;
import timmychips.modefiteitemdefinitions.property.type.codec.ItemModelDefinition;
import timmychips.modefiteitemdefinitions.property.type.codec.ItemModelRootDefinition;
import timmychips.modefiteitemdefinitions.property.type.ItemModelTypes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

/**
 * NeoForge entrypoint, replacing the Fabric {@code ClientModInitializer}.
 *
 * <p>Fabric drove everything from one {@code ModelLoadingPlugin} callback:
 * parse the item definitions, then hand the model ids to
 * {@code pluginContext.addModels}. NeoForge splits that across two events on
 * the mod bus, and the split matters - the ids have to be declared during
 * {@link ModelEvent.RegisterAdditional} or the models are never baked, while
 * the baked instances only exist later, at
 * {@link ModelEvent.BakingCompleted}.
 *
 * <p>The second half is what replaces {@code FabricBakedModelManager}, which
 * has no NeoForge equivalent: standalone models are not reachable through the
 * vanilla {@code ModelManager}, so the lookup is captured once at bake time and
 * handed to {@link timmychips.modefiteitemdefinitions.property.resolver.ResolveRecursive}.
 */
@Mod(value = ClientInitializer.MOD_ID, dist = Dist.CLIENT)
public class ClientInitializer {

    public static final String MOD_ID = "modefite";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static Collection<ResourceLocation> modelIds = List.of();

    // Extra optional fields in Items Model root
    private static final String HAND_ANIMATION_SWAP = "hand_animation_on_swap";
    private static final String OVERSIZED_IN_GUI = "oversized_in_gui"; // Not currently used/functioning
    private static final String SWAP_ANIMATION_SCALE = "swap_animation_scale"; // Not currently used/functioning

    private static void registerResources(String folderName, ResourceManager manager) {
        for (ResourceLocation id : manager.listResources(folderName, path -> path.getPath().endsWith(".json")).keySet()) {
            try (InputStream stream = manager.getResource(id).get().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));

                JsonObject root = json.getAsJsonObject();

                // Optional fields
                boolean handAnimationOnSwap = !root.has(HAND_ANIMATION_SWAP) || root.get(HAND_ANIMATION_SWAP).getAsBoolean(); // True if not specified
                boolean oversizedInGui = root.has(OVERSIZED_IN_GUI) && root.get(OVERSIZED_IN_GUI).getAsBoolean();
                float swapAnimationScale = root.has(SWAP_ANIMATION_SCALE) ? root.get(SWAP_ANIMATION_SCALE).getAsFloat() : 1F; // 1.0 if not specified

                JsonElement modelElement = root.get("model");

                if (modelElement != null && modelElement.isJsonObject()) {
                    ItemModelTypes.CODEC.decode(JsonOps.INSTANCE, modelElement)
                            .resultOrPartial(error -> LOGGER.warn("Failed to decode model definition for {}: {}", id, error))
                            .ifPresent(pair -> {
                                // Clean up path to match item ID (remove "items/" and ".json")
                                String cleanPath = id.getPath().substring((folderName + "/").length(), id.getPath().length() - ".json".length());
                                ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), cleanPath);

                                ItemModelDefinition definition = pair.getFirst();
                                ItemModelRootDefinition rootDef = new ItemModelRootDefinition(definition, handAnimationOnSwap, oversizedInGui, swapAnimationScale);

                                ItemModelTypes.Registry.putRoot(itemId, rootDef); // Register item and root definition
//                                LOGGER.info("Successfully decoded item model definition for: {}", itemId);
                            });
                } else {
                    LOGGER.warn("No 'model' field found in item JSON for {}", id);
                }

            } catch (Exception e) {
                LOGGER.warn("Failed to parse item definition for {}", id, e);
            }
        }
    }

    public ClientInitializer(IEventBus modBus) {
        modBus.addListener(ClientInitializer::onRegisterAdditional);
        modBus.addListener(ClientInitializer::onBakingCompleted);
    }

    /**
     * Parses every item definition and declares the models they reference.
     *
     * <p>All the per-reload resetting the Fabric plugin did happens here, for
     * the same reason: this fires on every resource reload, so stale state from
     * the previous pack set has to go first.
     */
    private static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();

        timmychips.modefiteitemdefinitions.property.resolver.ArmorTextureRedirect.clearCache();
        timmychips.modefiteitemdefinitions.property.resolver.VanillaShaderFactory.clearCache();
        ItemModelTypes.Registry.clear();
        RangePropertyRegistry.init();
        ConditionPropertyRegistry.init();
        SelectPropertyRegistry.init();

        registerResources("items", manager);
        // Where modded properties belong: modefite_items_override
        registerResources(MOD_ID + "_items_override", manager);

        modelIds = ItemModelTypes.Registry.getAllModelDependencies();
        LOGGER.info("{}: loading {} models", MOD_ID.toUpperCase(), modelIds.size());

        for (ResourceLocation id : modelIds) {
            event.register(new ModelResourceLocation(id, "standalone"));
        }
    }

    /** Captures the baked instances for the ids declared above. */
    private static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        timmychips.modefiteitemdefinitions.property.resolver.ResolveRecursive.setModelLookup(
                id -> event.getModels().get(new ModelResourceLocation(id, "standalone")));
    }
}
