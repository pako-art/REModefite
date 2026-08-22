package timmychips.modefiteitemdefinitions.property.resolver;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.registry.RangePropertyRegistry;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

public class RangeDispatchValueResolver {
    public static float evaluate(
            ResourceLocation property, Float scale,
            ItemStack stack, LivingEntity entity,
            RangeDispatchDefinition.Definition def) {

        return RangePropertyRegistry.resolve(property, stack, entity, def) * scale;
    }
}
