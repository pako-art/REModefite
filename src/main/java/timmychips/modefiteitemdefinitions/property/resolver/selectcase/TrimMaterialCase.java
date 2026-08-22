package timmychips.modefiteitemdefinitions.property.resolver.selectcase;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

/**
 * Return the material id of the armor trim as string
 */
public class TrimMaterialCase implements SelectPropertyHandler {
    @Override
    public String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        ArmorTrim armorTrim = stack.get(DataComponents.TRIM);
        if (armorTrim == null) return null;
        return armorTrim.getMaterial().getIdAsString();
    }
}
