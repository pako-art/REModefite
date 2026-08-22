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
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ServerInitializer.MOD_ID, "use_key_sync");
    public static final CustomPacketPayload.Id<UseKeyS2CPayload> PACKET_ID = new CustomPacketPayload.Id<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UseKeyS2CPayload> CODEC = StreamCodec.tuple(
            UUIDUtil.PACKET_CODEC, UseKeyS2CPayload::playerUuid,
            ItemStack.PACKET_CODEC, UseKeyS2CPayload::itemStack,
            ByteBufCodecs.BOOL, UseKeyS2CPayload::isUsing,
            UseKeyS2CPayload::new
    );

    @Override
    public CustomPacketPayload.Id<? extends CustomPacketPayload> getId() { return PACKET_ID; }
}
