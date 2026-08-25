package timmychips.modefiteitemdefinitions.property.type.codec;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code minecraft:bundle/selected_item} — draws the item currently selected
 * inside a bundle. Takes no fields.
 *
 * <p>Unreachable in practice on 1.21.1. Vanilla only ever places it under
 * {@code bundle/has_selected_item} being true, and that is always false here
 * because this version has no bundle selection. It is registered so a pack
 * using it decodes cleanly instead of failing the whole definition, and it
 * resolves to nothing — which is what an absent selection should draw.
 */
public record BundleSelectedItemDefinition(ResourceLocation type) implements ItemModelDefinition {

    public static final ResourceLocation TYPE = ResourceLocation.parse("minecraft:bundle/selected_item");

    public static final MapCodec<BundleSelectedItemDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(BundleSelectedItemDefinition::type)
    ).apply(instance, BundleSelectedItemDefinition::new));

    @Override
    public MapCodec<? extends ItemModelDefinition> getCodec() {
        return CODEC;
    }

    @Override
    public ResourceLocation expectedType() {
        return TYPE;
    }
}
