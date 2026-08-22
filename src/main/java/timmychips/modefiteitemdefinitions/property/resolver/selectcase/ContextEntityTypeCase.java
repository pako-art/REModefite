package timmychips.modefiteitemdefinitions.property.resolver.selectcase;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

/**
 * Returns entity as string
 */
public class ContextEntityTypeCase implements SelectPropertyHandler {
    @Override
    public String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        if (entity == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
}
