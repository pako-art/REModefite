package timmychips.modefiteitemdefinitions;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

/**
 * Yarn -> Mojmap and Fabric -> NeoForge for the use-key channel.
 *
 * <p>Fabric split this in two: {@code PayloadTypeRegistry} declared the codecs
 * and {@code ServerPlayNetworking.registerGlobalReceiver} attached the handler.
 * NeoForge does both in one call on {@link RegisterPayloadHandlersEvent}, so
 * the codec and its handler can no longer drift apart.
 *
 * <p>The version string matters: NeoForge negotiates it during login and
 * disconnects a client whose version differs, which is the behaviour we want -
 * the old Fabric channel had no such check and would simply drop packets.
 */
@EventBusSubscriber(modid = ClientInitializer.MOD_ID)
public final class ModefiteNetworking {

    private ModefiteNetworking() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                UseKeyC2SPayload.TYPE,
                UseKeyC2SPayload.CODEC,
                ModefiteNetworking::handleUseKey);

        // Client-bound. The handler lives on the client entrypoint; here we only
        // declare the type so the server may send it.
        registrar.playToClient(
                UseKeyS2CPayload.TYPE,
                UseKeyS2CPayload.CODEC,
                UseKeyTracker::handleUseKeySync);
    }

    /**
     * Rebroadcasts a player's use-key state to everyone else.
     *
     * <p>Fabric handed the receiver a context carrying the sending player.
     * NeoForge gives an {@code IPayloadContext}; the player comes from
     * {@code context.player()} and must be cast, since the same context type is
     * used on both sides.
     */
    private static void handleUseKey(UseKeyC2SPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        UUID senderUuid = payload.playerUuid();
        UseKeyS2CPayload broadcast = new UseKeyS2CPayload(senderUuid, payload.itemStack(), payload.isUsing());

        for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
            if (!player.getUUID().equals(senderUuid)) {
                PacketDistributor.sendToPlayer(player, broadcast);
            }
        }
    }
}
