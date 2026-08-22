package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.world.entity.LivingEntity;
import timmychips.modefiteitemdefinitions.compat.PunchyCompat;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Return if entity is using an interactable item
public class UsingItemBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        // Punchy drives the use animation when installed; see PunchyCompat.
        if (entity == null || PunchyCompat.deferUseProperties()) return false;
        return entity.isUsingItem() && entity.getUseItem() == stack;
    }
}
