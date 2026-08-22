package timmychips.modefiteitemdefinitions.property.resolver.selectcase;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

/**
 * Return main hand of player
 * <p>Values: left or right
 */
public class MainHandCase implements SelectPropertyHandler {
    @Override
    public String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        HumanoidArm mainArm = Minecraft.getInstance().options.mainHand().get();
        if (entity != null) mainArm = entity.getMainArm();

        return mainArm.toString().toLowerCase();
    }
}
