package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Returns if player has a valid fishhook entity associated to them
public class FishingRodCastBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        if (entity instanceof Player player) {
            return player.fishing != null;
        }
        return false;
    }
}
