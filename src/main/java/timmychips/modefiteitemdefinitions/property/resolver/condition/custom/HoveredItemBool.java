package timmychips.modefiteitemdefinitions.property.resolver.condition.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.helper.MouseHelper;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Custom Property
// Returns if item is hovered over in the inventory
public class HoveredItemBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        return MouseHelper.isHoveredOverStack(stack, Minecraft.getInstance());
    }
}
