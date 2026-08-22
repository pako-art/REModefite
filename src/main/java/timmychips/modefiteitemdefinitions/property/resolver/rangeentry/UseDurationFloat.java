package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

public class UseDurationFloat implements RangePropertyHandler {
    @Override
    public float getValue(ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition definition) {
        boolean use_remaining = Boolean.TRUE.equals(definition.useRemaining());

        if (entity == null) return 0F;
        else if (entity.getUseItem() != stack) return 0F;
        else return use_remaining ? entity.getUseItemRemainingTicks() : getTicksUsed(stack, entity);
    }

    public static int getTicksUsed(ItemStack stack, LivingEntity user) {
        return stack.getUseDuration(user) - user.getUseItemRemainingTicks();
    }
}
