package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

public class CooldownFloat implements RangePropertyHandler {
    @Override
    public float getValue(ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition definition) {
        float cooldown = 0F;
        if (entity instanceof Player player) {
            cooldown = player.getCooldowns().getCooldownPercent(stack.getItem(), 0F);
        }
        return cooldown;
    }
}
