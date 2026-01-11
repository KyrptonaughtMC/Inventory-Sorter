package net.kyrptonaught.inventorysorter.mixin;

import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinContainer {
    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Shadow
    private ItemStack carried;

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    public void sortOnDoubleClickEmpty(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        // Server side only
        if (!player.level().isClientSide()) {
            if (!(player instanceof ServerPlayer)) {
                // Heuristics, just to be on the safe side
                LOGGER.debug("Player is not a ServerPlayerEntity, skipping sort on double click");
                return;
            }

            SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS);

            if (settings.enableDoubleClick() && button == 0 && actionType.equals(ClickType.PICKUP_ALL))
                if (carried.isEmpty())
                    if (slotIndex >= 0 && slotIndex < this.slots.size() && this.slots.get(slotIndex).getItem().isEmpty()) {
                        boolean isPlayerInventory = slots.get(slotIndex).container instanceof Inventory;
                        InventoryHelper.sortInventory(
                                (ServerPlayer) player,
                                isPlayerInventory,
                                settings.sortType()
                        );

                        if (!isPlayerInventory && settings.sortPlayerInventory()) {
                            InventoryHelper.sortInventory(
                                    (ServerPlayer) player,
                                    true,
                                    settings.sortType()
                            );
                        }

                        ci.cancel();
                    }
        }
    }
}
