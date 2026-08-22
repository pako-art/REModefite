package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Return if item is selected in player's hand
public class SelectedBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        if (entity instanceof Player player) {
            InteractionHand hand = player.getUsedItemHand();
            return hand != null && player.getItemInHand(hand) == stack;
        }
        return false;
    }
}
