package timmychips.modefiteitemdefinitions.property.resolver;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.registry.SelectPropertyRegistry;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

public class SelectValueResolver {

    public static String evaluate(
            ResourceLocation property,
            ItemDisplayContext renderMode,
            SelectDefinition.Definition def,
            ItemStack stack,
            LivingEntity entity) {

        // TODO case "minecraft:local_time" class
        return SelectPropertyRegistry.resolve(property, stack, entity, renderMode, def);
    }
}
