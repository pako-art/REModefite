package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

// Return the pull use-time of the crossbow
public class CrossbowPullFloat implements RangePropertyHandler {
    @Override
    public float getValue(ItemStack stack, LivingEntity user, RangeDispatchDefinition.Definition definition) {
        if (user == null) return 0.0F;
        else if (CrossbowItem.isCharged(stack)) return 0.0F;
        else {
            int pull_time = CrossbowItem.getChargeDuration(stack, user);
            return (float) UseDurationFloat.getTicksUsed(stack, user) / pull_time;
//            return (float) (stack.getMaxUseTime(user) - user.getUseItemRemainingTicks()) / pull_time;
        }
//        return CrossbowItem.isCharged(stack) ? 0.0F : (float)(stack.getMaxUseTime(user) - user.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(stack, user);
    }
}
