package net.kyrptonaught.inventorysorter.mixin;

import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinContainer {
    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Shadow
    private ItemStack carried;

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    public void sortOnDoubleClickEmpty(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        // Server side only
        if (!player.level().isClientSide()) {
            if (!(player instanceof ServerPlayer)) {
                // Heuristics, just to be on the safe side
                LOGGER.debug("Player is not a ServerPlayerEntity, skipping sort on double click");
                return;
            }
            ServerPlayer serverPlayer = (ServerPlayer) player;
            SortSettings settings = PlatformServices.PLAYER_DATA.getSortSettings(serverPlayer);

            if (settings.enableDoubleClick() && buttonNum == 0 && containerInput.equals(ContainerInput.PICKUP_ALL))
                if (carried.isEmpty())
                    if (slotIndex >= 0 && slotIndex < this.slots.size() && this.slots.get(slotIndex).getItem().isEmpty()) {
                        boolean isPlayerInventory = slots.get(slotIndex).container instanceof Inventory;
                        SortTarget target = isPlayerInventory ? SortTarget.PLAYER_INVENTORY : SortTarget.CONTAINER;
                        InventoryHelper.sortInventory(
                                serverPlayer,
                                target,
                                settings
                        );

                        if (target == SortTarget.CONTAINER && settings.sortPlayerInventory()) {
                            InventoryHelper.sortInventory(
                                    serverPlayer,
                                    SortTarget.PLAYER_INVENTORY,
                                    settings
                            );
                        }

                        ci.cancel();
                    }
        }
    }
}
