package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.compat.TrinketsBundleTargets;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.sort.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleInsertionLayoutPass;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlots;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
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
    private static InventoryScreenId lastCheckedId;
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
            InventoryScreenId screenId = InventoryScreenId.fromMenu(screenHandler).orElse(null);
            if (screenId == null) return null;

            return action.apply(new ScreenContext(screenHandler, screenId, inventory));
        } catch (Exception e) {
            return null;
        } finally {
            player.closeContainer();
            screenHandler.removed(player);
        }
    }

    public static Component sortTargetedBlock(ServerPlayer player, SortType sortType) {
        return sortTargetedBlock(player, new SortSettings(true, false, true, sortType));
    }

    public static Component sortTargetedBlock(ServerPlayer player, SortSettings settings) {

        Boolean result = withTargetedScreenHandler(player, (context) -> {
            if (context.inventory == null) {
                return false;
            }
            if (canSortInventory(player, context.handler)) {
                String languageCode = player.clientInformation().language().toLowerCase();
                sortInventory(context.inventory, 0, context.inventory.getContainerSize(), settings.sortType(), languageCode, settings.sortPriorityRules(), settings.sortIntoBundles());
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

    public static boolean sortInventory(ServerPlayer player, SortTarget target, SortType sortType) {
        return sortInventory(player, target, new SortSettings(true, false, true, sortType));
    }

    public static boolean sortInventory(ServerPlayer player, SortTarget target, SortSettings settings) {
        String languageCode = player.clientInformation().language().toLowerCase();
        if (target == SortTarget.PLAYER_INVENTORY) {
            sortPlayerInventory(player, settings, languageCode);
            return true;
        } else if (target == SortTarget.CONTAINER && canSortInventory(player)) {
            Container inv = getInventory(player.containerMenu);
            if (inv != null) {
                sortInventory(inv, 0, inv.getContainerSize(), settings.sortType(), languageCode, settings.sortPriorityRules(), settings.sortIntoBundles());
                return true;
            }
        }
        return false;
    }

    public static Container getInventory(AbstractContainerMenu screenHandler) {
        if (screenHandler.slots.isEmpty()) return null;
        return screenHandler.slots.getFirst().container;
    }

    private static void sortInventory(Container inv, int startSlot, int invSize, SortType sortType, String languageCode, List<SortPriorityRuleSetting> sortPriorityRules, boolean sortIntoBundles) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < invSize; i++) {
            stacks.add(inv.getItem(startSlot + i));
        }

        SortedInventoryLayout sortedInventoryLayout = SortedInventoryLayout.from(stacks, sortType, languageCode, sortPriorityRules, sortIntoBundles);
        if (sortedInventoryLayout.stacks().stream().allMatch(ItemStack::isEmpty)) {
            return;
        }
        for (int i = 0; i < invSize; i++)
            inv.setItem(startSlot + i, sortedInventoryLayout.stacks().get(i));
        inv.setChanged();
    }

    private static void sortPlayerInventory(ServerPlayer player, SortSettings settings, String languageCode) {
        if (!settings.sortIntoBundles()) {
            sortInventory(player.getInventory(), 9, 27, settings.sortType(), languageCode, settings.sortPriorityRules(), false);
            return;
        }

        List<ItemStack> mainInventoryStacks = getStacks(player.getInventory(), 9, 27);
        BundleTargetSlots extraBundleTargets = BundleTargetSlots.fromContainers(extraBundleTargetContainers(player, settings));
        BundleInsertionLayoutPass.Result bundleInsertion = BundleInsertionLayoutPass.apply(
                mainInventoryStacks,
                extraBundleTargets.stacks(),
                SortPriorityRules.compile(settings.sortPriorityRules())
        );
        SortedInventoryLayout sortedInventoryLayout = SortedInventoryLayout.fromBundleAdjusted(
                bundleInsertion.layoutStacks(),
                settings.sortType(),
                languageCode,
                settings.sortPriorityRules()
        );

        extraBundleTargets.setStacks(bundleInsertion.extraTargetStacks());
        setStacks(player.getInventory(), 9, sortedInventoryLayout.stacks());
        player.getInventory().setChanged();
    }

    private static List<Container> extraBundleTargetContainers(ServerPlayer player, SortSettings settings) {
        List<Container> containers = new ArrayList<>();
        if (settings.sortIntoHotbarBundles()) {
            containers.add(new InventorySlice(player.getInventory(), 0, 9));
        }
        containers.addAll(TrinketsBundleTargets.containers(player));
        return containers;
    }

    private static List<ItemStack> getStacks(Container inv, int startSlot, int invSize) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < invSize; i++) {
            stacks.add(inv.getItem(startSlot + i));
        }
        return stacks;
    }

    private static void setStacks(Container inv, int startSlot, List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            inv.setItem(startSlot + i, stacks.get(i));
        }
    }

    private record InventorySlice(Container container, int startSlot, int size) implements Container {
        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < size; i++) {
                if (!getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return container.getItem(startSlot + slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return container.removeItem(startSlot + slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return container.removeItemNoUpdate(startSlot + slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            container.setItem(startSlot + slot, stack);
        }

        @Override
        public void setChanged() {
            container.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return container.stillValid(player);
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < size; i++) {
                setItem(i, ItemStack.EMPTY);
            }
        }
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
            InventoryScreenId screenId = InventoryScreenId.fromMenu(player.containerMenu).orElse(null);

            if (screenId == null) {
                return false;
            }
            setLastChecked(screenId);
            return compatibility.shouldShowSortButton(screenId.value());

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
            InventoryScreenId screenId = InventoryScreenId.fromMenu(screenHandler).orElse(null);

            if (screenId == null) {
                return false;
            }
            return isSortableContainer(player, screenHandler, screenId);

        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private static boolean isSortableContainer(Player player, AbstractContainerMenu screenHandler, InventoryScreenId screenId) {
        PlayerSortPrevention playerSortPrevention = player instanceof ServerPlayer serverPlayer
                ? PlatformServices.PLAYER_DATA.getPlayerSortPrevention(serverPlayer)
                : PlayerSortPrevention.DEFAULT;
        if (!compatibility.isSortAllowed(screenId.value(), playerSortPrevention.preventSortForScreens())) {
            return false;
        }

        // This seems to exist to prevent the sorting of non-storage-type containers
        int numSlots = screenHandler.slots.size();
        if (numSlots <= 36) {
            return false;
        }
        return numSlots - 36 >= 9;
    }

    private static void setLastChecked(InventoryScreenId id) {
        lastCheckedId = id;
        lastCheckedTimestamp = System.currentTimeMillis();
    }

    public static Optional<InventoryScreenId> getLastCheckedId() {
        if (lastCheckedId != null && System.currentTimeMillis() - lastCheckedTimestamp > TIMEOUT_MS) {
            lastCheckedId = null;
        }
        return Optional.ofNullable(lastCheckedId);
    }

    public record ScreenContext(AbstractContainerMenu handler, InventoryScreenId screenId, Container inventory) {
    }
}
