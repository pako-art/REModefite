package timmychips.modefiteitemdefinitions.property.type.codec;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record ModelDefinition(ResourceLocation type, ResourceLocation model) implements ItemModelDefinition {
    public static final MapCodec<ModelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(ModelDefinition::type),
            ResourceLocation.CODEC.fieldOf("model").forGetter(ModelDefinition::model)
    ).apply(instance, ModelDefinition::new));

    public static final ResourceLocation TYPE = ResourceLocation.parse("minecraft:model");

    @Override
    public MapCodec<? extends ItemModelDefinition> getCodec() {
        return CODEC;
    }

    @Override
    public ResourceLocation expectedType() {
        return TYPE;
    }
}
