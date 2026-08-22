package timmychips.modefiteitemdefinitions.property.registry;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.ClientInitializer;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.resolver.condition.*;
import timmychips.modefiteitemdefinitions.property.resolver.condition.custom.HoveredItemBool;
import timmychips.modefiteitemdefinitions.property.resolver.condition.custom.SubmergedBool;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

import java.util.HashMap;
import java.util.Map;

public class ConditionPropertyRegistry {
    private static final Map<ResourceLocation, ConditionPropertyHandler> HANDLERS = new HashMap<>();
    private static final String MOD_ID = ClientInitializer.MOD_ID;

    public static void init() {
        register(ResourceLocation.parse("minecraft:broken"), new BrokenBool());
        register(ResourceLocation.parse("minecraft:carried"), new CarriedBool());
        register(ResourceLocation.parse("minecraft:component"), new ComponentBool());
        register(ResourceLocation.parse("minecraft:custom_model_data"), new CustomModelDataBool());
        register(ResourceLocation.parse("minecraft:damaged"), new DamagedBool());
        register(ResourceLocation.parse("minecraft:extended_view"), new ExtendedViewBool());
        register(ResourceLocation.parse("minecraft:fishing_rod/cast"), new FishingRodCastBool());
        register(ResourceLocation.parse("minecraft:has_component"), new HasComponentBool());
        register(ResourceLocation.parse("minecraft:keybind_down"), new KeybindDownBool());
        register(ResourceLocation.parse("minecraft:selected"), new SelectedBool());
        register(ResourceLocation.parse("minecraft:using_item"), new UsingItemBool());
        register(ResourceLocation.parse("minecraft:view_entity"), new ViewEntityBool());

        // Custom, modded Properties
        register(ResourceLocation.parse(MOD_ID,"hovered_item"), new HoveredItemBool());
        register(ResourceLocation.parse(MOD_ID,"submerged"), new SubmergedBool());
    }

    private static void register(ResourceLocation id, ConditionPropertyHandler handler) {
        HANDLERS.put(id, handler);
    }

    public static boolean resolve(ResourceLocation id, ItemStack stack, LivingEntity entity, ConditionDefinition def) {
        ConditionPropertyHandler handler = HANDLERS.get(id);
        if (handler == null) return false;
        return handler.getValue(stack, entity, def);
    }
}
