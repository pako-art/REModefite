package timmychips.modefiteitemdefinitions.property.helper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityVariantHelper {

    // TODO: add rest of entity variants from 1.21.5
    //  https://minecraft.wiki/w/Data_component_format#Entity_variant_components

    /// Not components in this version, but are in 1.21.5
    static final List<ResourceLocation> ENTITY_VARIANTS = List.of(
            ResourceLocation.withDefaultNamespace("axolotl/variant"),
            ResourceLocation.withDefaultNamespace("frog/variant")
    );

    // Axolotl variant names
    static final ArrayList<ResourceLocation> AXOLOTL_VARIANT_LIST = new ArrayList<>(
            Arrays.asList(
                    ResourceLocation.withDefaultNamespace("lucy"),
                    ResourceLocation.withDefaultNamespace("wild"),
                    ResourceLocation.withDefaultNamespace("gold"),
                    ResourceLocation.withDefaultNamespace("cyan"),
                    ResourceLocation.withDefaultNamespace("blue")
            )
    );


    /**
     *
     * @param component Component identifier to check if it's a type of entity variant
     * @return If component is in list
     */
    public static boolean isEntityVariant(ResourceLocation component) {
        return ENTITY_VARIANTS.contains(component);
    }


    /**
     * Casts the component data to its identifier version from 1.21.5
     * @param stack ItemStack to cast component data to string
     * @param component The component to cast from
     * @return String of entity variant
     */
    public static String castEntityVariantComponents(ItemStack stack, String component) {
        switch (component) {
            case "minecraft:axolotl/variant" -> {
                String axolotlVariant = getBucketEntityVariant(stack);
                if (axolotlVariant != null) return axolotlVariant;
            }
        }
        return null;
    }


    /**
     * Get the bucket_entity_data component as a string like for 1.21.5+
     * <p>E.g. a minecraft:axolotl_bucket item that has the gold variant will return variant int 3, and will be cast to its identifier equivalent, "minecraft:gold"
     * @param stack ItemStack to get the variant data from
     * @return String of the entity variant id from the bucket
     */
    public static String getBucketEntityVariant(ItemStack stack) {

        CustomData bucketData = stack.get(DataComponents.BUCKET_ENTITY_DATA);

        if (bucketData != null) {
            CompoundTag root = bucketData.copyTag(); // copy nbt data for safe reading

            // Axolotl Variants
            if (root.contains("Variant", 3)) { // Get value from axolotl variant string/index
                int value = root.getInt("Variant");
                return AXOLOTL_VARIANT_LIST.get(value).toString(); // Return the string associated with int
            }
        }
        return null;
    }
}
