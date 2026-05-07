package net.kyrptonaught.inventorysorter.compat.plugins;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.impl.TrinketSlot;
import java.util.ArrayList;
import java.util.List;
import net.kyrptonaught.inventorysorter.compat.CompatibilityPlugin;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

public final class TrinketsPlugin implements CompatibilityPlugin {
    private static final String TRINKETS_MOD_ID = "trinkets";

    @Override
    public List<BundleTargetSlot> serverBundleSlots(ServerPlayer player, SortSettings settings) {
        if (!isLoaded()) {
            return List.of();
        }
        List<BundleTargetSlot> slots = new ArrayList<>();
        TrinketsApi.getAttachment(player).forEach((slot, stack) -> {
            if (slot != null && stack.is(Items.BUNDLE)) {
                slots.add(new BundleTargetSlot(slot, slot.inventory()::setChanged));
            }
        });
        return List.copyOf(slots);
    }

    @Override
    public boolean isClientBundleSlot(Slot slot) {
        return isLoaded()
                && slot instanceof TrinketSlot trinketSlot
                && isBundleSlot(trinketSlot.getAccess());
    }

    @Override
    public void prepareClientBundleSlotClick(Slot slot) {
        if (!isLoaded() || !(slot instanceof TrinketSlot trinketSlot)) {
            return;
        }

        ClientSlotFocus.prepare(trinketSlot);
    }

    private static boolean isBundleSlot(TrinketSlotAccess slot) {
        return slot != null && slot.get().is(Items.BUNDLE);
    }

    private static boolean isLoaded() {
        return PlatformServices.PLATFORM.isModLoaded(TRINKETS_MOD_ID);
    }

    private static final class ClientSlotFocus {
        private ClientSlotFocus() {
        }

        private static void prepare(TrinketSlot slot) {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            TrinketSlotAccess access = slot.getAccess();
            eu.pb4.trinkets.impl.client.TrinketsClient.activeGroup =
                    TrinketsApi.getPlayerSlots(minecraft.player).get(access.slotType().group());
            eu.pb4.trinkets.impl.client.TrinketsClient.activeType = slot.getType();
            eu.pb4.trinkets.impl.client.TrinketsClient.quickMoveGroup = null;
            eu.pb4.trinkets.impl.client.TrinketsClient.quickMoveType = null;
        }
    }
}
