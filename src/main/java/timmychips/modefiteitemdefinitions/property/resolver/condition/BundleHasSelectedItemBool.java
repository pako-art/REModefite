package timmychips.modefiteitemdefinitions.property.resolver.condition;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import timmychips.modefiteitemdefinitions.property.handler.ConditionPropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.ConditionDefinition;

/**
 * {@code minecraft:bundle/has_selected_item} — always false on 1.21.1.
 *
 * <p>Bundles exist here, but selecting an item inside one does not: the scroll
 * interaction and the {@code selectedItem} index arrived with the bundle UI in
 * 1.21.2. {@link net.minecraft.world.item.component.BundleContents} on this
 * version holds a list and a weight and nothing else, and
 * {@link net.minecraft.world.item.BundleItem} has no selection at all — there
 * is no state to report.
 *
 * <p>Reporting false is the accurate answer rather than a stub. Vanilla's own
 * bundle definition branches on this to decide between the plain bundle model
 * and an open bundle showing its selection; with no selection possible, the
 * plain model is right. Registering it explicitly also stops it being treated
 * as an unknown property.
 */
public class BundleHasSelectedItemBool implements ConditionPropertyHandler {
    @Override
    public boolean getValue(ItemStack stack, LivingEntity entity, ConditionDefinition definition) {
        return false;
    }
}
