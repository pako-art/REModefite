package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.resolver.ResolveRecursive;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

import java.util.Set;

public class CustomModelDataFloat implements RangePropertyHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED_MODELS = ResolveRecursive.WARNED_MODELS;

    @Override
    public float getValue(ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition definition) {
        // Warning that custom_model_data is only an integer in versions below 1.21.4
        if (ResolveRecursive.warnOnce("minecraft:custom_model_data", stack)) LOGGER.warn("Unable to read 'custom_model_data' for type: 'minecraft:range_dispatch' since component is an integer in this version. Defaulting to use integer values.");

        CustomModelData custom_model_data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return custom_model_data != null ? (float) custom_model_data.value() : 0F;
    }
}
