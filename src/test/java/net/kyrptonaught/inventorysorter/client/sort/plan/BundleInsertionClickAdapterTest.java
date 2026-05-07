package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.kyrptonaught.inventorysorter.client.sort.ClientSortScope;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleInsertionLayoutPass;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class BundleInsertionClickAdapterTest {
    private final BundleInsertionClickAdapter adapter = new BundleInsertionClickAdapter();

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void layoutTargetInsertionWithoutRemainderUsesSourceThenTargetClicks() {
        BundleInsertionLayoutPass.Result insertion = new BundleInsertionLayoutPass.Result(
                List.of(ItemStack.EMPTY, stack(Items.BUNDLE, 1)),
                List.of(),
                List.of(new BundleInsertionLayoutPass.BundleInsertion(
                        0,
                        List.of(new BundleInsertionLayoutPass.BundleInsertionTarget(BundleInsertionLayoutPass.TargetArea.LAYOUT, 1))
                ))
        );

        ClientSortScope scope = scope();
        List<PlannedContainerClick> clicks = adapter.clicks(scope, scope.extraBundleTargetSlots(true), insertion);

        Assertions.assertEquals(List.of(pickupClick(10), pickupClick(11)), clicks);
    }

    @Test
    void extraTargetInsertionWithRemainderReturnsRemainderToSource() {
        BundleInsertionLayoutPass.Result insertion = new BundleInsertionLayoutPass.Result(
                List.of(stack(Items.APPLE, 3), ItemStack.EMPTY),
                List.of(stack(Items.BUNDLE, 1)),
                List.of(new BundleInsertionLayoutPass.BundleInsertion(
                        0,
                        List.of(new BundleInsertionLayoutPass.BundleInsertionTarget(BundleInsertionLayoutPass.TargetArea.EXTRA_TARGET, 0))
                ))
        );

        ClientSortScope scope = scope();
        List<PlannedContainerClick> clicks = adapter.clicks(scope, scope.extraBundleTargetSlots(true), insertion);

        Assertions.assertEquals(List.of(pickupClick(10), pickupClick(20), pickupClick(10)), clicks);
    }

    @Test
    void emptyInsertionResultEmitsNoClicks() {
        BundleInsertionLayoutPass.Result insertion = new BundleInsertionLayoutPass.Result(
                List.of(stack(Items.APPLE, 1)),
                List.of(),
                List.of()
        );

        ClientSortScope scope = scope();
        Assertions.assertTrue(adapter.clicks(scope, scope.extraBundleTargetSlots(true), insertion).isEmpty());
    }

    private static ClientSortScope scope() {
        SimpleContainer layout = new SimpleContainer(2);
        SimpleContainer hotbar = new SimpleContainer(1);
        return new ClientSortScope(
                4,
                List.of(
                        new ClientSortScope.ScopedSlot(10, new Slot(layout, 0, 0, 0)),
                        new ClientSortScope.ScopedSlot(11, new Slot(layout, 1, 0, 0))
                ),
                List.of(new ClientSortScope.ScopedSlot(20, new Slot(hotbar, 0, 0, 0))),
                List.of()
        );
    }

    private static PlannedContainerClick pickupClick(int slot) {
        return new PlannedContainerClick(slot, 0, ContainerInput.PICKUP);
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, item == Items.BUNDLE ? 1 : 64).build()
        );
    }
}
