package timmychips.modefiteitemdefinitions.property.resolver;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import timmychips.modefiteitemdefinitions.property.type.ItemModelTypes;
import timmychips.modefiteitemdefinitions.property.type.codec.ItemModelDefinition;

import timmychips.modefiteitemdefinitions.property.tint.ActiveTints;

import java.util.Optional;

    public class ItemModelResolver {
        public static Optional<BakedModel> resolveModel(ResourceLocation itemId, ItemDisplayContext renderMode, ItemStack stack, LivingEntity entity) {
            ItemModelDefinition def = ItemModelTypes.Registry.get(itemId);
//            if (def == null) return Optional.ofNullable(ResolveRecursive.getMissingModel());
            // Cleared before every resolve so a model without tints cannot inherit
            // the list left by the previous one.
            ActiveTints.clear();
            if (def == null) return Optional.empty();

            return ResolveRecursive.resolve(def, renderMode, stack, entity);
        }
    }
