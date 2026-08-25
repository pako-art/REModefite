package timmychips.modefiteitemdefinitions.property.type.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

// Entry point to item model definition types
public sealed interface ItemModelDefinition
        permits CompositeModelDefinition, ConditionDefinition, EmptyModelDefinition, SpecialModelDefinition, ModelDefinition, RangeDispatchDefinition.Definition, SelectDefinition.Definition {

    /**
     * Every subtype must return its own codec.
     */
    MapCodec<? extends ItemModelDefinition> getCodec();

    /**
     *
     * @return The actual type declared in the JSON type field
     */
    ResourceLocation type();

    /**
     *
     * @return The type that is expected from the definition object (minecraft:composite, minecraft:condition, etc.)
     */
    ResourceLocation expectedType();
}
