package timmychips.modefiteitemdefinitions.property.registry;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.handler.SelectPropertyHandler;
import timmychips.modefiteitemdefinitions.property.resolver.selectcase.*;
import timmychips.modefiteitemdefinitions.property.type.codec.SelectDefinition;

import java.util.HashMap;
import java.util.Map;

public class SelectPropertyRegistry {
    private static final Map<ResourceLocation, SelectPropertyHandler> HANDLERS = new HashMap<>();

    public static void init() {
        register(ResourceLocation.parse("minecraft:block_state"), new BlockStateCase());
        register(ResourceLocation.parse("minecraft:charge_type"), new ChargeTypeCase());
        register(ResourceLocation.parse("minecraft:component"), new ComponentCase());
        register(ResourceLocation.parse("minecraft:context_dimension"), new ContextDimensionCase());
        register(ResourceLocation.parse("minecraft:context_entity_type"), new ContextEntityTypeCase());
        register(ResourceLocation.parse("minecraft:custom_model_data"), new CustomModelDataCase());
        register(ResourceLocation.parse("minecraft:display_context"), new DisplayContextCase());
        register(ResourceLocation.parse("minecraft:main_hand"), new MainHandCase());
        register(ResourceLocation.parse("minecraft:trim_material"), new TrimMaterialCase());
    }

    private static void register(ResourceLocation id, SelectPropertyHandler handler) {
        HANDLERS.put(id, handler);
    }

    public static String resolve(ResourceLocation id, ItemStack stack, LivingEntity entity, ItemDisplayContext mode, SelectDefinition.Definition definition) {
        SelectPropertyHandler handler = HANDLERS.get(id);
        if (handler == null) return null;
        return handler.getValue(stack, entity, mode, definition);
    }
}
