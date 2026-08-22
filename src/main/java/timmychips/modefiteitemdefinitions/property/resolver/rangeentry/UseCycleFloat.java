package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import net.minecraft.world.entity.LivingEntity;
import timmychips.modefiteitemdefinitions.compat.PunchyCompat;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

// Return item use left modulo use period
public class UseCycleFloat implements RangePropertyHandler {
    @Override
    public float getValue(ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition def) {
        float period = def.usePeriod();

        if (entity == null || PunchyCompat.deferUseProperties()) return 0F;
        if (entity.getUseItem() == stack) return (float) entity.getUseItemRemainingTicks() % period;

        return 0F;
    }
}
