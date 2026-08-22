package timmychips.modefiteitemdefinitions.property.registry;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.resolver.rangeentry.*;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

import java.util.HashMap;
import java.util.Map;

public class RangePropertyRegistry {
    private static final Map<ResourceLocation, RangePropertyHandler> HANDLERS = new HashMap<>();

    // Register each property
    public static void init() {
        register(ResourceLocation.parse("minecraft:bundle/fullness"), new BundleFullnessFloat());
        register(ResourceLocation.parse("minecraft:compass"), new CompassFloat());
        register(ResourceLocation.parse("minecraft:cooldown"), new CooldownFloat());
        register(ResourceLocation.parse("minecraft:count"), new CountFloat());
        register(ResourceLocation.parse("minecraft:crossbow/pull"), new CrossbowPullFloat());
        register(ResourceLocation.parse("minecraft:damage"), new DamageFloat());
        register(ResourceLocation.parse("minecraft:time"), new ClockTimeFloat());
        register(ResourceLocation.parse("minecraft:use_cycle"), new UseCycleFloat());
        register(ResourceLocation.parse("minecraft:use_duration"), new UseDurationFloat());
        register(ResourceLocation.parse("minecraft:custom_model_data"), new CustomModelDataFloat());
    }

    private static void register(ResourceLocation id, RangePropertyHandler handler) {
        HANDLERS.put(id, handler);
    }

    public static float resolve(ResourceLocation id, ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition def) {
        RangePropertyHandler handler = HANDLERS.get(id);
        if (handler == null) return 0f;
        return handler.getValue(stack, entity, def);
    }
}
