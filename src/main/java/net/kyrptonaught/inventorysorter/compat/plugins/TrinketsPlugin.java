package net.kyrptonaught.inventorysorter.compat.plugins;

import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.List;
import net.kyrptonaught.inventorysorter.compat.CompatibilityPlugin;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public final class TrinketsPlugin implements CompatibilityPlugin {
    //~ if neoforge 'trinkets' -> 'trinkets_updated'
    public static final String TRINKETS_MOD_ID = "trinkets";
    private static final String TRINKET_SLOT_CLASS = "eu.pb4.trinkets.impl.TrinketSlot";
    private static final String TRINKETS_CLIENT_CLASS = "eu.pb4.trinkets.impl.client.TrinketsClient";

    @Override
    public List<BundleTargetSlot> serverBundleSlots(ServerPlayer player, SortSettings settings) {
        if (!isLoaded()) {
            return List.of();
        }
        List<BundleTargetSlot> slots = new ArrayList<>();
        TrinketsApi.getAttachment(player).forEach((slot, stack) -> {
            if (slot != null && stack.is(Items.BUNDLE)) {
                TrinketInventory inventory = slot.inventory();
                int index = slot.index();

                slots.add(new BundleTargetSlot(new SlotAccess() {
                    @Override
                    public @NonNull ItemStack get() {
                        return inventory.getItem(index);
                    }

                    @Override
                    public boolean set(@NonNull ItemStack stack) {
                        inventory.setItem(index, stack);
                        return true;
                    }
                }, inventory::setChanged));
            }
        });
        return List.copyOf(slots);
    }

    @Override
    public boolean isClientBundleSlot(Slot slot) {
        return isLoaded() && isBundleSlot(trinketAccess(slot));
    }

    @Override
    public void prepareClientBundleSlotClick(Slot slot) {
        if (!isLoaded()) {
            return;
        }

        ClientSlotFocus.prepare(trinketAccess(slot));
    }

    private static boolean isBundleSlot(TrinketSlotAccess slot) {
        return slot != null && slot.get().is(Items.BUNDLE);
    }

    private static boolean isLoaded() {
        return PlatformServices.PLATFORM.isModLoaded(TRINKETS_MOD_ID);
    }

    private static TrinketSlotAccess trinketAccess(Slot slot) {
        try {
            Class<?> trinketSlotClass = Class.forName(TRINKET_SLOT_CLASS, false, slot.getClass().getClassLoader());
            if (!trinketSlotClass.isInstance(slot)) {
                return null;
            }
            Object access = trinketSlotClass.getMethod("getAccess").invoke(slot);
            return access instanceof TrinketSlotAccess trinketSlotAccess ? trinketSlotAccess : null;
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private static final class ClientSlotFocus {
        private ClientSlotFocus() {
        }

        private static void prepare(TrinketSlotAccess access) {
            if (access == null) {
                return;
            }

            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            try {
                Class<?> trinketsClientClass = Class.forName(TRINKETS_CLIENT_CLASS);
                trinketsClientClass.getField("activeGroup").set(null,
                        TrinketsApi.getPlayerSlots(minecraft.player).get(access.slotType().group()));
                trinketsClientClass.getField("activeType").set(null, access.slotType());
                trinketsClientClass.getField("quickMoveGroup").set(null, null);
                trinketsClientClass.getField("quickMoveType").set(null, null);
            } catch (ReflectiveOperationException | LinkageError e) {
                // Trinkets exposes no public API for focusing hidden client slots.
            }
        }
    }
}
