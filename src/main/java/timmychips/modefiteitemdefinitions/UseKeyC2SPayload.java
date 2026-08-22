package timmychips.modefiteitemdefinitions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

import static timmychips.modefiteitemdefinitions.ClientInitializer.MOD_ID;

public record UseKeyC2SPayload(UUID playerUuid, ItemStack itemStack, boolean isUsing) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "use_key");
    public static final CustomPacketPayload.Type<UseKeyC2SPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UseKeyC2SPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UseKeyC2SPayload::playerUuid,
            ItemStack.STREAM_CODEC, UseKeyC2SPayload::itemStack,
            ByteBufCodecs.BOOL, UseKeyC2SPayload::isUsing,
            UseKeyC2SPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
