package timmychips.modefiteitemdefinitions.property.resolver.selectcase;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

/**
 * Return custom model data int component value on item as string
 * <p>(Closest to custom_model_data predicate, pre-1.21.4)
 */
public class CustomModelDataCase implements SelectPropertyHandler {
    @Override
    public String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        CustomModelData custom_model_data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (custom_model_data == null) return null;
        return String.valueOf(custom_model_data.value());
    }
}
