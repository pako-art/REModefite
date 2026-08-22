package timmychips.modefiteitemdefinitions.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandleSlotAccessor {

    @Accessor("x")
    int getX();

    @Accessor("y")
    int getY();
}
