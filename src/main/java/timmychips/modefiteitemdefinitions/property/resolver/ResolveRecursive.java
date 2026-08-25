package timmychips.modefiteitemdefinitions.property.resolver;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import timmychips.modefiteitemdefinitions.bakedmodels.CompositeItemModel;
import timmychips.modefiteitemdefinitions.bakedmodels.EmptyItemModel;
import timmychips.modefiteitemdefinitions.bakedmodels.SpecialItemModel;
import timmychips.modefiteitemdefinitions.property.type.SpecialModelRegistry;
import timmychips.modefiteitemdefinitions.property.resolver.selectcase.ComponentCase;
import timmychips.modefiteitemdefinitions.property.type.codec.*;

import java.util.Map;
import java.util.function.Function;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ResolveRecursive {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Compared by identity instead of building a String on every resolve. */
    private static final ResourceLocation COMPONENT_PROPERTY = ResourceLocation.withDefaultNamespace("component");
    public static final Set<String> WARNED_MODELS = ConcurrentHashMap.newKeySet();

    /** Per-category sets of items already warned about. */
    private static final Map<String, Set<net.minecraft.world.item.Item>> WARNED_ITEMS = new ConcurrentHashMap<>();

    /**
     * Whether this is the first warning of {@code category} for this item.
     *
     * <p>The call sites built "item|category" by concatenation before testing
     * the set, so each allocated two strings plus an Item.toString() per item
     * per frame - for a message logged once per session. Keying on the Item
     * instance costs nothing after the first call, and the lambda passed to
     * computeIfAbsent captures nothing, so it is not allocated either.
     */
    public static boolean warnOnce(String category, ItemStack stack) {
        return WARNED_ITEMS.computeIfAbsent(category, k -> ConcurrentHashMap.newKeySet()).add(stack.getItem());
    }

    /** EmptyItemModel holds no state; allocating one per resolve was pure garbage. */
    private static final Optional<BakedModel> EMPTY_MODEL = Optional.of(new EmptyItemModel());

    /**
     * Lookup for the standalone models this mod registers.
     *
     * <p>Replaces {@code FabricBakedModelManager.getModel(Identifier)}, which
     * has no NeoForge counterpart: models added through
     * {@code ModelEvent.RegisterAdditional} are not reachable from the vanilla
     * {@code ModelManager}. {@code ClientInitializer} installs this at
     * {@code ModelEvent.BakingCompleted} and replaces it on every reload.
     *
     * <p>Volatile because the resource-reload worker writes it and the render
     * thread reads it.
     */
    private static volatile Function<ResourceLocation, BakedModel> modelLookup = id -> null;

    public static void setModelLookup(Function<ResourceLocation, BakedModel> lookup) {
        modelLookup = lookup;
    }

    /**
     * The exact instance ModelBakery substitutes for a model it could not load.
     *
     * <p>Identity against {@code ModelManager.getMissingModel()} is not enough:
     * a standalone id that fails to bake can come back as a different instance,
     * and then the check silently passes and a magenta cube is drawn in hand.
     * Captured from the same map the lookup reads, so it is always the right one.
     */
    private static volatile BakedModel bakedMissingModel;

    public static void setBakedMissingModel(BakedModel model) {
        bakedMissingModel = model;
    }

    /**
     * Whether a lookup produced no usable model.
     *
     * <p>Identity only, deliberately. An earlier version also treated any model
     * whose particle icon was the missingno texture as missing, which caught
     * four models that had loaded perfectly well and merely referenced a texture
     * the atlas could not resolve - copper_ingot_hand, golden_ingot_hand,
     * iron_ingot_hand and heart_of_the_sea_hand all rendered vanilla instead of
     * their 3D shape. A model with a missing texture is still a model; only a
     * model that never baked is not.
     *
     * <p>The cost of narrowing it back is that a model which failed to bake and
     * came back as an instance other than these two is not recognised, and
     * renders as the magenta cube - the same as both upstream builds do. That
     * is worse for one item and better for four.
     */
    private static boolean isMissing(BakedModel model) {
        return model == null || model == bakedMissingModel || model == getMissingModel();
    }

    /** Fetch missing model safely */
    public static BakedModel getMissingModel() {
        return Minecraft.getInstance().getModelManager().getMissingModel();
    }

    /**
     * Resolves a definition recursively into a baked model.
     */
    public static Optional<BakedModel> resolve(ItemModelDefinition def, ItemDisplayContext renderMode, ItemStack stack, LivingEntity entity) {
        if (def == null) return Optional.empty();
        if (renderMode == null) renderMode = ItemDisplayContext.GUI;

        switch (def) {
            case ModelDefinition model -> {
                BakedModel bakedModel = modelLookup.apply(model.model());
                // A registered id whose file is absent does not come back null:
                // ModelBakery bakes the missing model under that id, so the lookup
                // succeeds and upstream renders a full-size magenta cube in hand.
                // Report nothing instead, which leaves vanilla to draw the item.
                if (isMissing(bakedModel)) {
                    if (WARNED_MODELS.add("missing|" + model.model())) {
                        LOGGER.warn("Item definition points at a model that failed to load: {}. Falling back to the vanilla item model.", model.model());
                    }
                    return Optional.empty();
                }
                return Optional.of(bakedModel);
            }
            case SpecialModelDefinition special -> {
                BakedModel base = modelLookup.apply(special.base());
                if (isMissing(base)) {
                    return Optional.empty();
                }
                ItemStack proxy = SpecialModelRegistry.proxyFor(special.subType(), special.fields());
                // No proxy means this sub-type has no representation here -
                // copper_golem_statue, for one. The base model is the honest answer.
                return Optional.of(proxy == null ? base : new SpecialItemModel(base, proxy));
            }
            case EmptyModelDefinition emptyModelDefinition -> {
                return EMPTY_MODEL;  // stateless, so one instance serves every call
            }
            case CompositeModelDefinition composite -> {
                if (composite.models().isEmpty()) return missingFallbackModel(stack, null, composite.type());
                return Optional.of(new CompositeItemModel(composite.models(), renderMode, stack, entity)); // Returns combined item models
            }
            case SelectDefinition.Definition select -> {
                // Component map predicates (e.g. stored_enchantments) match against a map, not a single value
                if (COMPONENT_PROPERTY.equals(select.property()) && select.component() != null) {
                    Optional<Map<String, Integer>> componentEntries = ComponentCase.getComponentEntries(stack, select.component());
                    if (componentEntries.isPresent()) {
                        for (SelectDefinition.Case<String> c : select.cases()) {
                            if (c.isComponentMap() && c.componentMaps().stream().anyMatch(m -> ComponentCase.matchesAll(m, componentEntries.get()))) {
                                return resolve(c.model(), renderMode, stack, entity);
                            }
                        }
                        return select.fallback() != null
                                ? resolve(select.fallback(), renderMode, stack, entity)
                                : missingFallbackModel(stack, select.property(), select.type());
                    }
                }

                String propertyValue = SelectValueResolver.evaluate(select.property(), renderMode, select, stack, entity);

                if (propertyValue != null) {
                    for (SelectDefinition.Case<String> c : select.cases()) {
                        if (c.valueSet().contains(propertyValue)) {
                            return resolve(c.model(), renderMode, stack, entity);
                        }
                    }
                }

                return select.fallback() != null
                        ? resolve(select.fallback(), renderMode, stack, entity)
                        : missingFallbackModel(stack, select.property(), select.type());
            }
            case ConditionDefinition cond -> {
                boolean result = ConditionValueResolver.evaluate(cond.property(), stack, entity, cond);
                return result
                        ? resolve(cond.on_true(), renderMode, stack, entity)
                        : resolve(cond.on_false(), renderMode, stack, entity);
            }
            case RangeDispatchDefinition.Definition range -> {
                float value = RangeDispatchValueResolver.evaluate(range.property(), range.scale(), stack, entity, range);

                // entries() arrives sorted highest-threshold-first from the codec,
                // so the first match is the right one. No stream, no sort, no
                // lambda allocation on a path that runs per item per frame.
                for (RangeDispatchDefinition.ThresholdEntry entry : range.entries()) {
                    if (value >= entry.threshold()) {
                        return resolve(entry.model(), renderMode, stack, entity);
                    }
                }
                return range.fallback() != null
                        ? resolve(range.fallback(), renderMode, stack, entity)
                        : missingFallbackModel(stack, range.property(), range.type());
            }
            default -> {
            }
        }

        return Optional.empty();
    }

    /** Warn once and return missing model if no match found */
    private static Optional<BakedModel> missingFallbackModel(ItemStack stack, ResourceLocation property, @Nullable ResourceLocation type) {
        /// For properties of condition, select, range_dispatch types
        if (property != null) {
            if (warnOnce(property.toString(), stack)) {
                LOGGER.warn("No matching model found for property '{}', item: '{}'", property, stack.getItem());
            }
        }

        ///  For composite model type
        if (type != null && type.getPath().equals("composite")) {
            if (warnOnce(type.toString(), stack)) {
                LOGGER.warn("Composite model has no valid models defined '{}', item: '{}'", type, stack.getItem());
            }
        }
        return Optional.of(getMissingModel());
    }

//    private static Optional<BakedModel> nullCompositeModel(ItemStack stack, )
}
