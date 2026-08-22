package timmychips.modefiteitemdefinitions.property.type.codec;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.type.ItemModelTypes;

import java.util.List;

public record CompositeModelDefinition(ResourceLocation type, List<ItemModelDefinition> models) implements ItemModelDefinition {
    public static final MapCodec<CompositeModelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(CompositeModelDefinition::type),
            ItemModelTypes.CODEC.listOf().fieldOf("models").forGetter(CompositeModelDefinition::models)
    ).apply(instance, CompositeModelDefinition::new));

    public static final ResourceLocation TYPE = ResourceLocation.parse("minecraft:composite");

    @Override
    public MapCodec<? extends ItemModelDefinition> getCodec() {
        return CODEC;
    }

    @Override
    public ResourceLocation expectedType() {
        return TYPE;
    }
}
