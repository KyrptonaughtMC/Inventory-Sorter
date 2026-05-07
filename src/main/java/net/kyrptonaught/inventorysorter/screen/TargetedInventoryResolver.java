package net.kyrptonaught.inventorysorter.screen;

import java.util.function.Function;
import java.util.OptionalInt;
import net.kyrptonaught.inventorysorter.InventoryScreenId;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class TargetedInventoryResolver {
    public static final double MAX_LOOKUP_DISTANCE = 6.0D;

    private TargetedInventoryResolver() {
    }

    public static <T> T withTargetedScreen(ServerPlayer player, Function<TargetedScreenContext, T> action) {
        HitResult hit = player.pick(MAX_LOOKUP_DISTANCE, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) return null;

        BlockPos blockPos = blockHit.getBlockPos();
        Level world = player.level();
        BlockState blockState = world.getBlockState(blockPos);

        Container inventory = null;
        MenuProvider menuProvider = null;

        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            inventory = HopperBlockEntity.getContainerAt(world, blockPos);
            menuProvider = blockState.getMenuProvider(world, blockPos);
            if (menuProvider == null && blockEntity instanceof MenuProvider blockEntityMenuProvider) {
                menuProvider = blockEntityMenuProvider;
            }
        } else {
            menuProvider = blockState.getMenuProvider(world, blockPos);
        }

        if (menuProvider == null) {
            return null;
        }

        OptionalInt syncId = player.openMenu(menuProvider);
        if (syncId.isEmpty()) return null;

        AbstractContainerMenu menu = menuProvider.createMenu(syncId.getAsInt(), player.getInventory(), player);

        try {
            InventoryScreenId screenId = InventoryScreenId.fromMenu(menu).orElse(null);
            if (screenId == null) return null;

            return action.apply(new TargetedScreenContext(menu, screenId, inventory));
        } catch (Exception e) {
            return null;
        } finally {
            player.closeContainer();
            menu.removed(player);
        }
    }
}
