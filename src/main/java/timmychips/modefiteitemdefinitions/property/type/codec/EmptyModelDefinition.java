package timmychips.modefiteitemdefinitions.property.type.codec;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record EmptyModelDefinition(ResourceLocation type) implements ItemModelDefinition {
    public static final MapCodec<EmptyModelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(EmptyModelDefinition::type)
    ).apply(instance, EmptyModelDefinition::new));

    public static final ResourceLocation TYPE = ResourceLocation.parse("minecraft:empty");

    @Override
    public MapCodec<? extends ItemModelDefinition> getCodec() {
        return CODEC;
    }

    @Override
    public ResourceLocation expectedType() {
        return TYPE;
    }
}
