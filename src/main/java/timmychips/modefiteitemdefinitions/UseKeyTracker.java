package timmychips.modefiteitemdefinitions;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import timmychips.modefiteitemdefinitions.objects.PlayerHeldItem;

import java.util.HashMap;
import java.util.UUID;

/**
 * Fabric -> NeoForge for the use-key tracking.
 *
 * <p>Three Fabric hooks are replaced by NeoForge events:
 * {@code ClientTickEvents.END_CLIENT_TICK} and {@code END_WORLD_TICK} both
 * become {@link ClientTickEvent.Post} - NeoForge has no separate client-world
 * tick, so the per-player pass runs off the same event, guarded on the level
 * being loaded. {@code UseItemCallback} becomes
 * {@link PlayerInteractEvent.RightClickItem}.
 *
 * <p><b>Upstream defect, fixed here.</b> The Fabric version guarded the C2S
 * send with {@code if (!world.isClient)} and then called
 * {@code ClientPlayNetworking.send}, i.e. it only tried to send the
 * client-to-server packet while running on the server, where that call has no
 * client connection to send on. The condition is inverted; this port sends when
 * {@code level.isClientSide} is true, which is the only side that can.
 */
@EventBusSubscriber(modid = ClientInitializer.MOD_ID, value = Dist.CLIENT)
public class UseKeyTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ItemStack itemUsed = ItemStack.EMPTY;
    private static boolean useKeyPressed = false;
    public static final HashMap<Player, PlayerHeldItem> itemMap = new HashMap<>();

    /**
     * Runs every client tick. Covers what Fabric split across END_CLIENT_TICK
     * and END_WORLD_TICK.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Level level = client.level;
        if (level == null) return;

        Player user = client.player;
        KeyMapping useKey = client.options.keyUse;
        useKeyPressed = useKey.isDown();

        if (user != null) {
            itemUsed = user.getMainHandItem().isEmpty() ? user.getOffhandItem() : user.getMainHandItem();

            if (useKeyPressed && !itemUsed.isEmpty()) {
                itemMap.put(user, new PlayerHeldItem(itemUsed.getItem().getDefaultInstance()));
            }
        }

        // Tick timer for other (non-client) players to retain item usage.
        for (Player player : level.players()) {
            useTickInterval(player);
        }
    }

    /** Tells the server this player started using an item. Client side only. */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player user = event.getEntity();
        if (!user.level().isClientSide) return;

        // Base item as a default stack, to avoid component-map crashes over the wire.
        Item item = user.getItemInHand(event.getHand()).getItem();
        ItemStack defaultStack = item.getDefaultInstance();

        if (!defaultStack.isEmpty()) {
            PacketDistributor.sendToServer(new UseKeyC2SPayload(user.getUUID(), defaultStack, true));
        }
    }

    /**
     * Another player's use-key state, relayed by the server.
     *
     * <p>Registered from {@link ModefiteNetworking}. NeoForge already dispatches
     * payload handlers on the main thread, so the {@code client.execute} hop the
     * Fabric version needed is gone.
     */
    public static void handleUseKeySync(UseKeyS2CPayload payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        Player sender = client.level.getPlayerByUUID(payload.playerUuid());
        if (sender != null && payload.isUsing()) {
            itemMap.put(sender, new PlayerHeldItem(payload.itemStack().copy()));
        }
    }

    // Countdown tick timer
    // Since UseItemCallback event doesn't occur every tick, we have a countdown before we update that the other player is no longer using an item
    public static void useTickInterval(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (itemMap.containsKey(player)) {
                int intervalTick = itemMap.get(player).checkInterval; // Gets current interval value
                if (intervalTick > 0) intervalTick--;
                if (intervalTick == 0) afterUseCooldown(player); // Does afterUseCooldown method when player stops using item

                else itemMap.get(player).checkInterval = intervalTick; // Update new interval value
            }
        }
    }

    public static void afterUseCooldown(Player player) {
        float useTimer = itemMap.get(player).lastUsed;

        if (useTimer > 0F) {
            if (matchesItemInHand(player, itemMap.get(player).lastItem)) useTimer--; // Item being used is held in hand
            else useTimer = 0F; // Stops timer if player changes items from what they last used
        }
        if (useTimer == 0F) itemMap.remove(player);
        else itemMap.get(player).lastUsed = useTimer; // Update new cooldown value
    }

    // Returns if the currently rendered ItemStack matches what the player is holding
    // Intended for the client player, as to prevent non-selected items to not have their models change
    // Only the actively selected item will change item models
    // TODO Merge/Cleanup with matchesItemInHand method in HeldItemPredicate.java
    //  Currently only changes player's main hand item model if two different items are in main/offhand at same time; Fix?
    //  Also possibly clean/split this class up into other class(es)
    private static boolean clientHasItemSelected(LivingEntity livingEntity, ItemStack stack) {
        if (livingEntity instanceof LocalPlayer clientPlayer) {
//            InteractionHand hand = clientPlayer.getUsedItemHand();
//            ItemStack currentStack = clientPlayer.getItemInHand(hand); // Only actually does it for player's main hand :(

            ItemStack currentStack = clientPlayer.getMainHandItem().isEmpty() ? clientPlayer.getOffhandItem() : clientPlayer.getMainHandItem();

            return ItemStack.matches(currentStack,stack);
        }
        return true;
    }

    // Item Predicate logic to set "is_using" predicate float based on some criteria
    public static float playerUseItemKey(LivingEntity livingEntity, ItemStack usableItem) {
        // Items that you can actually use (food, bow, shield, etc.)
        if (!(livingEntity instanceof Player)) return 0.0F;
        if (livingEntity.isUsingItem() && ItemStack.matches(livingEntity.getUseItem(), usableItem)) return 1.0F;

        // For non-usable items like pickaxes, blocks, materials, etc.
        Player player = (Player) livingEntity;

        float returnFloat = 0.0F;
        if (itemMap.containsKey(player)) {
            ItemStack lastItem = itemMap.get(player).lastItem;

            if (!clientHasItemSelected(player, usableItem)) return 0.0F; // If player is client and not has used item selected, return 0F

            if (lastItem != null) {
                returnFloat = itemMap.get(player).lastUsed / 18.0F; // Get normalized value of last used timer from 0 to 1 for that player
            }
        }
        return returnFloat;
    }

    public static boolean matchesItemInHand(LivingEntity entity, ItemStack stack) {
        ItemStack currentItem = entity.getMainHandItem().isEmpty() ? entity.getOffhandItem() : entity.getMainHandItem();
        return stack.toString().equals(currentItem.toString());
    }
}