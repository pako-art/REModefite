package timmychips.modefiteitemdefinitions.property.resolver.condition;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.resolver.ResolveRecursive;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

import java.util.Set;

// Returns if item has specified component (and/or if it should ignore the default component value)
public class HasComponentBool implements ConditionPropertyHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED_MODELS = ResolveRecursive.WARNED_MODELS;

    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        String component = definition.component(); // Specified component string from items definition
        boolean ignore_default = definition.ignore_default(); // If it should ignore the default item's component value

        if (component == null) return false;

        // Parse string to ResourceLocation id
        ResourceLocation componentId = ResourceLocation.tryParse(component);
        if (componentId == null) {
            if (ResolveRecursive.warnOnce("minecraft:has_component", stack)) LOGGER.warn("Invalid component predicate ID '{}'", component);
            return false;
        }

        // Get component type from ResourceLocation
        DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(componentId);
        if (componentType == null) {
            if (ResolveRecursive.warnOnce("minecraft:has_component", stack)) LOGGER.warn("Unknown component predicate componentType '{}'", componentId);
            return false;
        }

        if (stack.has(componentType)) { // stack has component

            if (!ignore_default) return true;               // if ignore_default is false
            else return hasChanged(stack, componentType);   // if it's true
        }

        return false;
    }

    // Boolean if item component has had component changes
    private static Boolean hasChanged(ItemStack stack, DataComponentType<?> componentType) {
        DataComponentPatch changes = stack.getComponentsPatch();
        return changes.entrySet().stream()                                  // changes.entrySet returns map<DataComponentType, Optional<?>>
                .anyMatch(entry -> entry.getKey().equals(componentType));   // stream and do anyMatch to check the key (DataComponentType) matches to our componentType var
    }
}
