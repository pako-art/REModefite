package timmychips.modefiteitemdefinitions.mixin.client;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the rotation lock to 22.5 and 45 degree multiples
 */
@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public abstract class UnlockedModelRotationDeserializerMixin implements JsonDeserializer<BlockElement> {
    @Inject(method = "deserializeRotationAngle(Lcom/google/gson/JsonObject;)F", at = @At("HEAD"), cancellable = true)
    private void modefite$unlockedRotationAngle(JsonObject object, CallbackInfoReturnable<Float> cir) {
        float unlockedAngle = GsonHelper.getAsFloat(object, "angle");
        cir.setReturnValue(unlockedAngle);
    }
}
