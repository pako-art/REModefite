package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Return if player is spectating entity, or if local player
public class ViewEntityBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        Minecraft client = Minecraft.getInstance();
        Entity spectatedEntity = client.getCameraEntity(); // get entity player is spectating in spectator mode
        return spectatedEntity != null ? entity == spectatedEntity : entity == client.player; // return true if player is spectating entity or is local client player
    }
}
