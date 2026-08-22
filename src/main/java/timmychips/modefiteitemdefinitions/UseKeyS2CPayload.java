package timmychips.modefiteitemdefinitions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record UseKeyS2CPayload(UUID playerUuid, ItemStack itemStack, boolean isUsing) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ClientInitializer.MOD_ID, "use_key_sync");
    public static final CustomPacketPayload.Type<UseKeyS2CPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UseKeyS2CPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UseKeyS2CPayload::playerUuid,
            ItemStack.STREAM_CODEC, UseKeyS2CPayload::itemStack,
            ByteBufCodecs.BOOL, UseKeyS2CPayload::isUsing,
            UseKeyS2CPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
