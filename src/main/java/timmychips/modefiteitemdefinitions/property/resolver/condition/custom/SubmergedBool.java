package timmychips.modefiteitemdefinitions.property.resolver.condition.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import timmychips.modefiteitemdefinitions.ClientInitializer;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

// Returns if player is in matching "fluid" field in items model definition
// "fluid" json field - optional; is id minecraft:water by default
public class SubmergedBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        ResourceLocation fluid = definition.submergedFluid(); // is minecraft:water by default
        return submergedInFluidCheck(entity, stack, fluid);
    }

    private static boolean submergedInFluidCheck(LivingEntity livingEntity, ItemStack stack, ResourceLocation fluidIdToMatch) {

        Entity holder = livingEntity == null ? stack.getHolder() : livingEntity; // Get the stack holding entity (ItemEntity) if livingEntity is null

        if (holder != null) {
            Vec3 eyePos;
            if (holder instanceof LivingEntity) eyePos = holder.getEyePosition();
            else eyePos = holder.getPos();

            BlockPos fluidBlock = BlockPos.containing(eyePos);
            FluidState fluidState = holder.level().getFluidState(fluidBlock); // Get fluidState entity is submerged in

            if (!fluidState.isEmpty()) {
                Fluid fluid = fluidState.getFluid();
                Fluid targetToMatch = BuiltInRegistries.FLUID.get(fluidIdToMatch); // Get identifier to match as Fluid object

                return fluid.is(targetToMatch); // Matches specified fluid from json file
            }
        }
        return false;
    }
}
