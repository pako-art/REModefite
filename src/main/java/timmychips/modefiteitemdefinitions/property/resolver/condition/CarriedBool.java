package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Returns if cursor is holding item
public class CarriedBool implements ConditionPropertyHandler {
    // Checks if current stack matches the cursor's stack
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        if (entity instanceof LocalPlayer clientPlayer) {
            return clientPlayer.containerMenu.getCarried() == stack;
        }

        return false;
    }
}
