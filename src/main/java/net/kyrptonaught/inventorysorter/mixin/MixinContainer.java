package net.kyrptonaught.inventorysorter.mixin;

import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

@Mixin(ScreenHandler.class)
public abstract class MixinContainer {
    @Shadow
    @Final
    public DefaultedList<Slot> slots;

    @Shadow
    private ItemStack cursorStack;

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    public void sortOnDoubleClickEmpty(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        // Server side only
        if (!player.getWorld().isClient) {
            if (!(player instanceof ServerPlayerEntity)) {
                // Heuristics, just to be on the safe side
                LOGGER.debug("Player is not a ServerPlayerEntity, skipping sort on double click");
                return;
            }

            SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS);

            if (settings.enableDoubleClick() && button == 0 && actionType.equals(SlotActionType.PICKUP_ALL))
                if (cursorStack.isEmpty())
                    if (slotIndex >= 0 && slotIndex < this.slots.size() && this.slots.get(slotIndex).getStack().isEmpty()) {
                        boolean isPlayerInventory = slots.get(slotIndex).inventory instanceof PlayerInventory;
                        InventoryHelper.sortInventory(
                                (ServerPlayerEntity) player,
                                isPlayerInventory,
                                settings.sortType()
                        );

                        if (!isPlayerInventory && settings.sortPlayerInventory()) {
                            InventoryHelper.sortInventory(
                                    (ServerPlayerEntity) player,
                                    true,
                                    settings.sortType()
                            );
                        }

                        ci.cancel();
                    }
        }
    }
}
