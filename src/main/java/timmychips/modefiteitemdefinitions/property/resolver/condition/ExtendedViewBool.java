package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Returns if player is holding shift down (i.e. showing extended details in the UI)
public class ExtendedViewBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        return Screen.hasShiftDown();
    }
}
