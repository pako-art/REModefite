package timmychips.modefiteitemdefinitions.property.type.codec;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.type.ItemModelTypes;

/**
 * The {@code minecraft:special} model type.
 *
 * <p>Shape in vanilla:
 *
 * <pre>{@code
 * { "type": "minecraft:special",
 *   "base":  "minecraft:item/red_bed",
 *   "model": { "type": "minecraft:bed", "texture": "minecraft:red" } }
 * }</pre>
 *
 * <p>{@code base} is an ordinary flat model — what the item looks like in the
 * inventory, and the fallback when the special renderer cannot be used.
 * {@code model} names a sub-type plus whatever fields it takes; those fields
 * differ per sub-type, so they are carried as a raw {@link JsonObject} and
 * interpreted by {@link timmychips.modefiteitemdefinitions.property.type.SpecialModelRegistry}
 * rather than being modelled in the codec. That keeps a new sub-type from
 * needing a codec change.
 */
public record SpecialModelDefinition(ResourceLocation type, ResourceLocation base, String subType, JsonObject fields)
        implements ItemModelDefinition {

    public static final ResourceLocation TYPE = ResourceLocation.parse("minecraft:special");

    /** Passes the sub-type object through untouched; its fields are sub-type specific. */
    private static final Codec<JsonObject> RAW_OBJECT = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(com.mojang.serialization.JsonOps.INSTANCE).getValue().getAsJsonObject(),
            json -> new com.mojang.serialization.Dynamic<>(com.mojang.serialization.JsonOps.INSTANCE, json));

    public static final MapCodec<SpecialModelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(SpecialModelDefinition::type),
            ResourceLocation.CODEC.fieldOf("base").forGetter(SpecialModelDefinition::base),
            RAW_OBJECT.fieldOf("model").forGetter(SpecialModelDefinition::fields)
    ).apply(instance, (type, base, fields) -> {
        String sub = fields.has("type")
                ? fields.get("type").getAsString().replace("minecraft:", "")
                : "";
        return new SpecialModelDefinition(type, base, sub, fields);
    }));

    @Override
    public MapCodec<? extends ItemModelDefinition> getCodec() {
        return CODEC;
    }

    @Override
    public ResourceLocation expectedType() {
        return TYPE;
    }
}
