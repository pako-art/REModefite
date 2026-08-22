package timmychips.modefiteitemdefinitions.property.resolver.selectcase;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.helper.EntityVariantHelper;
import timmychips.modefiteitemdefinitions.property.resolver.ResolveRecursive;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;
import java.util.*;
import static timmychips.modefiteitemdefinitions.property.helper.EntityVariantHelper.castEntityVariantComponents;

/**
 * Returns a string for specified component's value
 * <p>{@code component:} ID of the component type
 */
public class ComponentCase implements SelectPropertyHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED_MODELS = ResolveRecursive.WARNED_MODELS;

    @Override
    public String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        String component = definition.component(); // Retrieve specified component to check for
        if (component == null) return null;

        ResourceLocation componentId = ResourceLocation.tryParse(component); // Parse string to id
        if (componentId == null) {
            String key = stack.getItem().toString() + "|" + "minecraft:component";
            if (WARNED_MODELS.add(key)) LOGGER.warn("Invalid component predicate ID '{}'", component);
            return null;
        }

        DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(componentId); // Retrieve component type from id
        if (componentType == null) {

            // For entity variants; ignore warned models
            if (EntityVariantHelper.isEntityVariant(ResourceLocation.tryParse(component))) {
                // Test if it can get entity variant from item stack
                String entityVariant = castEntityVariantComponents(stack, component);
                if (entityVariant != null) return castEntityVariantComponents(stack, component);
            }
            else {
                String key = stack.getItem().toString() + "|" + "minecraft:component";
                if (WARNED_MODELS.add(key)) LOGGER.warn("Unknown component predicate componentType: '{}'", componentId);
                return null;
            }
        }

        String str;

        Object componentValue = stack.get(componentType);
        if (componentValue instanceof Component textValue) {
            str = textValue.getString(); // Get the string without the surrounding literal from Component component types, and with string as is
            return str; // Return just the string for text
        }
        else str = String.valueOf(componentValue).toLowerCase(); // Convert non-text values to lower case string

        return String.valueOf(ResourceLocation.tryParse(str)); // Return component non-text value as string in identifier format
    }

    // Reads a component's enchantment map, for matching against a select case's map "when"
    public static Optional<Map<String, Integer>> getComponentEntries(ItemStack stack, String component) {
        ResourceLocation componentId = ResourceLocation.tryParse(component);
        if (componentId == null) return Optional.empty();

        DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(componentId);
        if (componentType == null) return Optional.empty();

        Object value = stack.get(componentType);
        if (!(value instanceof ItemEnchantments enchantments)) return Optional.empty();

        Map<String, Integer> entries = new HashMap<>();
        for (Holder<net.minecraft.world.item.enchantment.Enchantment> entry : enchantments.keySet()) {
            ResourceLocation id = entry.unwrapKey().map(ResourceKey::location).orElse(null);
            if (id != null) entries.put(id.toString(), enchantments.getLevel(entry));
        }
        return Optional.of(entries);
    }

    public static boolean matchesAll(Map<String, Integer> want, Map<String, Integer> actual) {
        for (Map.Entry<String, Integer> entry : want.entrySet()) {
            if (!entry.getValue().equals(actual.get(entry.getKey()))) return false;
        }
        return true;
    }
}
