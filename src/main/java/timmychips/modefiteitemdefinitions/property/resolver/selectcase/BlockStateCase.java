package timmychips.modefiteitemdefinitions.property.resolver.selectcase;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

// Returns Block State String specified
public class BlockStateCase implements SelectPropertyHandler {
    @Override
    public String getValue(ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        String block_state_property = definition.blockStateProperty(); // Get specified block state from items model definition

        BlockItemStateProperties block_state = stack.get(DataComponents.BLOCK_STATE);
        if (block_state == null) return null;
        return block_state.properties().get(block_state_property); // retrieves value from string
    }
}
