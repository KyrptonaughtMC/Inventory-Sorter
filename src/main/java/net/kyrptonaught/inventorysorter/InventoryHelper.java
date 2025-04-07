package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.PLAYER_SORT_PREVENTION;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;

public class InventoryHelper {

    public static final double MAX_LOOKUP_DISTANCE = 6.0D;

    public record ScreenContext(ScreenHandler handler, Identifier screenId, Inventory inventory) {}

    public static <T> T withTargetedScreenHandler(ServerPlayerEntity player, Function<ScreenContext, T> action) {
        HitResult hit = player.raycast(MAX_LOOKUP_DISTANCE, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) return null;

        BlockPos blockPos = blockHit.getBlockPos();
        World world = player.getWorld();
        BlockState blockState = world.getBlockState(blockPos);

        // Inventory to sort
        Inventory inventory = null;
        // Screen to open and check
        NamedScreenHandlerFactory namedScreenHandlerFactory = null;


        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            inventory = HopperBlockEntity.getInventoryAt(world, blockPos);
            namedScreenHandlerFactory = blockState.createScreenHandlerFactory(world, blockPos);
            if (namedScreenHandlerFactory == null && blockEntity instanceof NamedScreenHandlerFactory)
                namedScreenHandlerFactory = (NamedScreenHandlerFactory) blockEntity;
        } else {
            namedScreenHandlerFactory = blockState.createScreenHandlerFactory(world, blockPos);
        }
        // fail if either is not present
        if (namedScreenHandlerFactory == null) {
            return null;
        }

        OptionalInt syncId = player.openHandledScreen(namedScreenHandlerFactory);
        if (syncId.isEmpty()) return null;

        ScreenHandler screenHandler = namedScreenHandlerFactory.createMenu(syncId.getAsInt(), player.getInventory(), player);

        try {
            Identifier id = Registries.SCREEN_HANDLER.getId(screenHandler.getType());
            if (id == null) return null;

            return action.apply(new ScreenContext(screenHandler, id, inventory));
        } catch (Exception e) {
            return null;
        } finally {
            player.closeHandledScreen();
            screenHandler.onClosed(player);
        }
    }


    public static Text sortTargetedBlock(ServerPlayerEntity player, SortType sortType) {

        Boolean result = withTargetedScreenHandler(player, (context) -> {
            if (context.inventory == null) {
                return false;
            }
            if (canSortInventory(player, context.handler)) {
                sortInventory(context.inventory, 0, context.inventory.size(), sortType);
                return true;
            }
            return false;
        });

        if (result == null) {
            return Text.translatable("inventorysorter.cmd.sort.error");
        }
        if (result) {
            return Text.translatable("inventorysorter.cmd.sort.sorted");
        }

        return Text.translatable("inventorysorter.cmd.sort.notsortable");
    }

    public static boolean sortInventory(PlayerEntity player, boolean shouldSortPlayerInventory, SortType sortType) {
        if (shouldSortPlayerInventory) {
            sortInventory(player.getInventory(), 9, 27, sortType);
            return true;
        } else if (canSortInventory(player)) {
            Inventory inv = getInventory(player.currentScreenHandler);
            if (inv != null) {
                sortInventory(inv, 0, inv.size(), sortType);
                return true;
            }
        }
        return false;
    }

    public static Inventory getInventory(ScreenHandler screenHandler) {
        if (screenHandler.slots.isEmpty()) return null;
        return screenHandler.slots.getFirst().inventory;
    }

    static void sortInventory(Inventory inv, int startSlot, int invSize, SortType sortType) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < invSize; i++) {
            addStackWithMerge(stacks, inv.getStack(startSlot + i));
        }

        stacks.sort(SortCases.getComparator(sortType));
        if (stacks.size() == 0) {
            return;
        }
        for (int i = 0; i < invSize; i++)
            inv.setStack(startSlot + i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
        inv.markDirty();
    }

    private static void addStackWithMerge(List<ItemStack> stacks, ItemStack newStack) {
        if (newStack.getItem() == Items.AIR) {
            return;
        }
        if (newStack.isStackable() && newStack.getCount() != newStack.getMaxCount())
            for (int j = stacks.size() - 1; j >= 0; j--) {
                ItemStack oldStack = stacks.get(j);
                if (canMergeItems(newStack, oldStack)) {
                    combineStacks(newStack, oldStack);
                    if (oldStack.getItem() == Items.AIR || oldStack.getCount() == 0) {
                        stacks.remove(j);
                    }
                }
            }
        stacks.add(newStack);
    }

    private static void combineStacks(ItemStack stack, ItemStack stack2) {
        if (stack.getMaxCount() >= stack.getCount() + stack2.getCount()) {
            stack.increment(stack2.getCount());
            stack2.setCount(0);
        }
        int maxInsertAmount = Math.min(stack.getMaxCount() - stack.getCount(), stack2.getCount());
        stack.increment(maxInsertAmount);
        stack2.decrement(maxInsertAmount);
    }

    private static boolean canMergeItems(ItemStack itemStack_1, ItemStack itemStack_2) {
        if (!itemStack_1.isStackable() || !itemStack_2.isStackable()) {
            return false;
        }
        if (itemStack_1.getCount() == itemStack_1.getMaxCount() || itemStack_2.getCount() == itemStack_2.getMaxCount()) {
            return false;
        }
        if (itemStack_1.getItem() != itemStack_2.getItem()) {
            return false;
        }
        if (itemStack_1.getDamage() != itemStack_2.getDamage()) {
            return false;
        }
        return ItemStack.areItemsAndComponentsEqual(itemStack_1, itemStack_2);
    }

    public static boolean shouldDisplayButtons(PlayerEntity player) {

        if (player.currentScreenHandler == null || !player.currentScreenHandler.canUse(player)) {
            return false;
        }

        if (player.currentScreenHandler instanceof PlayerScreenHandler) {
            return true;
        }

        if (player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler) {
            return true;
        }

        try {
            Identifier id = Registries.SCREEN_HANDLER.getId(player.currentScreenHandler.getType());

            if (id == null) {
                return false;
            }
            return compatibility.shouldShowSortButton(id);

        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public static boolean canSortInventory(PlayerEntity player) {
        if (player.currentScreenHandler instanceof PlayerScreenHandler) {
            return false;
        }
        return canSortInventory(player, player.currentScreenHandler);
    }

    public static boolean canSortInventory(PlayerEntity player, ScreenHandler screenHandler) {
        if (screenHandler == null || !screenHandler.canUse(player)) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }

        try {
            Identifier id = Registries.SCREEN_HANDLER.getId(screenHandler.getType());

            if (id == null) {
                return false;
            }
            return isSortableContainer(player, screenHandler, id);

        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private static boolean isSortableContainer(PlayerEntity player, ScreenHandler screenHandler, Identifier screenID) {
        PlayerSortPrevention playerSortPrevention = player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);
        if (!compatibility.isSortAllowed(screenID, playerSortPrevention.preventSortForScreens())) {
            return false;
        }

        // This seems to exist to prevent the sorting of non-storage-type containers
        int numSlots = screenHandler.slots.size();
        if (numSlots <= 36) {
            return false;
        }
        return numSlots - 36 >= 9;
    }
}
