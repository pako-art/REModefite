package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

public class BundleFullnessFloat implements RangePropertyHandler {
    @Override
    public float getValue(ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition def) {
        return BundleItem.getFullnessDisplay(stack);
    }
}
