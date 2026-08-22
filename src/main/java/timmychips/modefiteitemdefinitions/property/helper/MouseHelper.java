package timmychips.modefiteitemdefinitions.property.helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import timmychips.modefiteitemdefinitions.mixin.client.HandleSlotAccessor;

public class MouseHelper {
    public static boolean isHoveredOverStack(ItemStack target, Minecraft client) {

        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) return false;

        double mouseX = client.mouseHandler.xpos() * (double) client.getWindow().getGuiScaledWidth() / (double) client.getWindow().getWidth();
        double mouseY = client.mouseHandler.ypos() * (double) client.getWindow().getGuiScaledHeight() / (double) client.getWindow().getHeight();


        int guiLeft = ((HandleSlotAccessor) screen).getX(); // Get screen coordinates from Mixin accessor
        int guiTop = ((HandleSlotAccessor) screen).getY();

        for (Slot slot : screen.getMenu().slots) {
            int slotX = guiLeft + slot.x;
            int slotY = guiTop + slot.y;

            boolean mouseOver = mouseX >= slotX - 1 && mouseX < slotX + 17 && // numbers are offsets so it highlights correctly
                    mouseY >= slotY - 1 && mouseY < slotY + 17;

            if (mouseOver && slot.getItem() == target) { // returns true if rendered model matches the hovered item
                return true;
            }
        }

        return false;
    }
}
