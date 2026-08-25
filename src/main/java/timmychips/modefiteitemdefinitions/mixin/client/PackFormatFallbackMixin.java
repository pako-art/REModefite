package timmychips.modefiteitemdefinitions.mixin.client;

import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import timmychips.modefiteitemdefinitions.ClientInitializer;

/**
 * Lets a pack load when its {@code pack.mcmeta} omits {@code pack_format}.
 *
 * <p>{@code pack_format} became optional in later versions, and packs built
 * there routinely leave it out — declaring only {@code min_format} and
 * {@code max_format}, or nothing at all. On 1.21.1 the codec still requires it,
 * so the whole pack is rejected before any of its contents are read:
 *
 * <pre>
 * Couldn't load pack metadata
 * JsonParseException: No key pack_format in MapLike[{"description":…,"min_format":69,"max_format":75}]
 * </pre>
 *
 * <p>The pack does not appear in the resource pack list at all, which reads as
 * a broken download rather than a version mismatch. Two of the packs this was
 * written against fail exactly this way, and both are otherwise readable.
 *
 * <p>When the field is absent this substitutes the running game's own pack
 * format, which is the same as saying "assume it is meant for this version".
 * That is a deliberate assumption, not a detection: a pack really built for a
 * newer version will still be missing models and textures, and will still say
 * so in the log, item by item. What changes is that the user sees a pack that
 * partially works instead of a pack that does not exist.
 *
 * <p>Only ever fills in a missing value. A pack that declares
 * {@code pack_format} is left exactly as it is, including one declaring a
 * format this version considers incompatible.
 */
@Mixin(AbstractPackResources.class)
public abstract class PackFormatFallbackMixin {

    @Redirect(
            method = "getMetadataFromStream",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/metadata/MetadataSectionSerializer;fromJson(Lcom/google/gson/JsonObject;)Ljava/lang/Object;"
            )
    )
    private static Object modefite$supplyMissingPackFormat(MetadataSectionSerializer<?> serializer, JsonObject json) {
        if ("pack".equals(serializer.getMetadataSectionName()) && !json.has("pack_format")) {
            int current = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
            json.addProperty("pack_format", current);
            ClientInitializer.LOGGER.info(
                    "pack.mcmeta declares no pack_format; assuming {} so the pack can load. Missing content will still be reported per file.",
                    current);
        }
        return serializer.fromJson(json);
    }
}
