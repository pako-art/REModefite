package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Check if specified keybinding is held down
public class KeybindDownBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        KeyMapping keybind = definition.keybind();
        if (keybind == null) return false;
        return keybind.isDown();
    }
}
