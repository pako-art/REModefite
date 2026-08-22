package timmychips.modefiteitemdefinitions.property.resolver.selectcase;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

/**
 * Returns dimension id entity is in as string
 */
public class ContextDimensionCase implements SelectPropertyHandler {
    @Override
    public String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        if (entity == null) return null;
        ClientLevel clientWorld = Minecraft.getInstance().level;
        return clientWorld != null ? clientWorld.dimension().location().toString() : null;
    }
}
