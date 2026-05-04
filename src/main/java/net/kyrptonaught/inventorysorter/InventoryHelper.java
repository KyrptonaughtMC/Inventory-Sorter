package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;

public class InventoryHelper {

    public static final double MAX_LOOKUP_DISTANCE = 6.0D;
    private static final long TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static Identifier lastCheckedId;
    private static long lastCheckedTimestamp;

    public static <T> T withTargetedScreenHandler(ServerPlayer player, Function<ScreenContext, T> action) {
        HitResult hit = player.pick(MAX_LOOKUP_DISTANCE, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) return null;

        BlockPos blockPos = blockHit.getBlockPos();
        Level world = player.level();
        BlockState blockState = world.getBlockState(blockPos);

        // Inventory to sort
        Container inventory = null;
        // Screen to open and check
        MenuProvider namedScreenHandlerFactory = null;


        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            inventory = HopperBlockEntity.getContainerAt(world, blockPos);
            namedScreenHandlerFactory = blockState.getMenuProvider(world, blockPos);
            if (namedScreenHandlerFactory == null && blockEntity instanceof MenuProvider)
                namedScreenHandlerFactory = (MenuProvider) blockEntity;
        } else {
            namedScreenHandlerFactory = blockState.getMenuProvider(world, blockPos);
        }
        // fail if either is not present
        if (namedScreenHandlerFactory == null) {
            return null;
        }

        OptionalInt syncId = player.openMenu(namedScreenHandlerFactory);
        if (syncId.isEmpty()) return null;

        AbstractContainerMenu screenHandler = namedScreenHandlerFactory.createMenu(syncId.getAsInt(), player.getInventory(), player);

        try {
            Identifier id = BuiltInRegistries.MENU.getKey(screenHandler.getType());
            if (id == null) return null;

            return action.apply(new ScreenContext(screenHandler, id, inventory));
        } catch (Exception e) {
            return null;
        } finally {
            player.closeContainer();
            screenHandler.removed(player);
        }
    }

    public static Component sortTargetedBlock(ServerPlayer player, SortType sortType) {

        Boolean result = withTargetedScreenHandler(player, (context) -> {
            if (context.inventory == null) {
                return false;
            }
            if (canSortInventory(player, context.handler)) {
                String languageCode = player.clientInformation().language().toLowerCase();
                sortInventory(context.inventory, 0, context.inventory.getContainerSize(), sortType, languageCode);
                return true;
            }
            return false;
        });

        if (result == null) {
            return Component.translatable("inventorysorter.cmd.sort.error");
        }
        if (result) {
            return Component.translatable("inventorysorter.cmd.sort.sorted");
        }

        return Component.translatable("inventorysorter.cmd.sort.notsortable");
    }

    public static boolean sortInventory(ServerPlayer player, boolean shouldSortPlayerInventory, SortType sortType) {
        String languageCode = player.clientInformation().language().toLowerCase();
        if (shouldSortPlayerInventory) {
            sortInventory(player.getInventory(), 9, 27, sortType, languageCode);
            return true;
        } else if (canSortInventory(player)) {
            Container inv = getInventory(player.containerMenu);
            if (inv != null) {
                sortInventory(inv, 0, inv.getContainerSize(), sortType, languageCode);
                return true;
            }
        }
        return false;
    }

    public static Container getInventory(AbstractContainerMenu screenHandler) {
        if (screenHandler.slots.isEmpty()) return null;
        return screenHandler.slots.getFirst().container;
    }

    private static void sortInventory(Container inv, int startSlot, int invSize, SortType sortType, String languageCode) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < invSize; i++) {
            addStackWithMerge(stacks, inv.getItem(startSlot + i));
        }

        stacks.sort(SortCases.getComparator(sortType, languageCode));
        if (stacks.size() == 0) {
            return;
        }
        for (int i = 0; i < invSize; i++)
            inv.setItem(startSlot + i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
        inv.setChanged();
    }

    private static void addStackWithMerge(List<ItemStack> stacks, ItemStack newStack) {
        if (newStack.getItem() == Items.AIR) {
            return;
        }
        if (newStack.isStackable() && newStack.getCount() != newStack.getMaxStackSize())
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
        if (stack.getMaxStackSize() >= stack.getCount() + stack2.getCount()) {
            stack.grow(stack2.getCount());
            stack2.setCount(0);
        }
        int maxInsertAmount = Math.min(stack.getMaxStackSize() - stack.getCount(), stack2.getCount());
        stack.grow(maxInsertAmount);
        stack2.shrink(maxInsertAmount);
    }

    private static boolean canMergeItems(ItemStack itemStack_1, ItemStack itemStack_2) {
        if (!itemStack_1.isStackable() || !itemStack_2.isStackable()) {
            return false;
        }
        if (itemStack_1.getCount() == itemStack_1.getMaxStackSize() || itemStack_2.getCount() == itemStack_2.getMaxStackSize()) {
            return false;
        }
        if (itemStack_1.getItem() != itemStack_2.getItem()) {
            return false;
        }
        if (itemStack_1.getDamageValue() != itemStack_2.getDamageValue()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(itemStack_1, itemStack_2);
    }

    public static boolean shouldDisplayButtons(Player player) {

        if (player.containerMenu == null || !player.containerMenu.stillValid(player)) {
            return false;
        }

        if (player.containerMenu instanceof InventoryMenu) {
            return true;
        }

        if (player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu) {
            return true;
        }

        try {
            Identifier id = BuiltInRegistries.MENU.getKey(player.containerMenu.getType());

            if (id == null) {
                return false;
            }
            setLastChecked(id);
            return compatibility.shouldShowSortButton(id);

        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public static boolean canSortInventory(Player player) {
        if (player.containerMenu instanceof InventoryMenu) {
            return false;
        }
        return canSortInventory(player, player.containerMenu);
    }

    public static boolean canSortInventory(Player player, AbstractContainerMenu screenHandler) {
        if (screenHandler == null || !screenHandler.stillValid(player)) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }

        try {
            Identifier id = BuiltInRegistries.MENU.getKey(screenHandler.getType());

            if (id == null) {
                return false;
            }
            return isSortableContainer(player, screenHandler, id);

        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private static boolean isSortableContainer(Player player, AbstractContainerMenu screenHandler, Identifier screenID) {
        PlayerSortPrevention playerSortPrevention = player instanceof ServerPlayer serverPlayer
                ? PlatformServices.PLAYER_DATA.getPlayerSortPrevention(serverPlayer)
                : PlayerSortPrevention.DEFAULT;
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

    private static void setLastChecked(Identifier id) {
        lastCheckedId = id;
        lastCheckedTimestamp = System.currentTimeMillis();
    }

    public static Optional<Identifier> getLastCheckedId() {
        if (lastCheckedId != null && System.currentTimeMillis() - lastCheckedTimestamp > TIMEOUT_MS) {
            lastCheckedId = null;
        }
        return Optional.ofNullable(lastCheckedId);
    }

    public record ScreenContext(AbstractContainerMenu handler, Identifier screenId, Container inventory) {
    }
}
