package timmychips.modefiteitemdefinitions.property.type.codec;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.tint.TintSource;

import java.util.List;

/**
 * A plain model reference, optionally with per-tint-index colours.
 *
 * <p>The {@code tints} array was previously dropped: the codec ignores fields
 * it does not declare, so a pack tinting leather armour or a potion simply got
 * no colour. Sources are built once here at parse time rather than per frame.
 */
public record ModelDefinition(ResourceLocation type, ResourceLocation model, List<TintSource> tints)
        implements ItemModelDefinition {

    /** Tint entries are type-tagged and each carries its own fields, so they arrive raw. */
    private static final Codec<JsonObject> RAW_OBJECT = Codec.PASSTHROUGH.xmap(
            d -> d.convert(JsonOps.INSTANCE).getValue().getAsJsonObject(),
            j -> new com.mojang.serialization.Dynamic<>(JsonOps.INSTANCE, j));

    public static final MapCodec<ModelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(ModelDefinition::type),
            ResourceLocation.CODEC.fieldOf("model").forGetter(ModelDefinition::model),
            RAW_OBJECT.listOf().optionalFieldOf("tints", List.of()).forGetter(d -> List.of())
    ).apply(instance, (type, model, raw) ->
            new ModelDefinition(type, model, raw.stream().map(TintSource::fromJson).toList())));

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
