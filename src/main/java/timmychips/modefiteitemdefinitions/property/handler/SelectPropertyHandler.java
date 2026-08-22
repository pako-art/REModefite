package timmychips.modefiteitemdefinitions.property.handler;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

@FunctionalInterface
public interface SelectPropertyHandler {
    String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition);
}
