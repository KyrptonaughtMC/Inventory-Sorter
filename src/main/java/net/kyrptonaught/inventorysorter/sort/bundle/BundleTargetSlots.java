package net.kyrptonaught.inventorysorter.sort.bundle;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class BundleTargetSlots {
    private final List<TargetSlot> targetSlots;

    private BundleTargetSlots(List<TargetSlot> targetSlots) {
        this.targetSlots = targetSlots;
    }

    /**
     * Flattens target containers into stable slot order for bundle insertion.
     */
    public static BundleTargetSlots fromContainers(List<? extends Container> containers) {
        List<TargetSlot> targetSlots = new ArrayList<>();
        for (Container container : containers) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                targetSlots.add(new TargetSlot(BundleTargetSlot.fromContainer(container, slot)));
            }
        }
        return new BundleTargetSlots(List.copyOf(targetSlots));
    }

    public static BundleTargetSlots fromSlots(List<BundleTargetSlot> slots) {
        return new BundleTargetSlots(slots.stream()
                .map(TargetSlot::new)
                .toList());
    }

    public List<ItemStack> stacks() {
        return targetSlots.stream()
                .map(TargetSlot::stack)
                .toList();
    }

    /**
     * Writes bundle-adjusted stacks back to the original containers.
     */
    public void setStacks(List<ItemStack> stacks) {
        if (stacks.size() != targetSlots.size()) {
            throw new IllegalArgumentException("Expected " + targetSlots.size() + " target stacks but got " + stacks.size());
        }
        for (int i = 0; i < targetSlots.size(); i++) {
            targetSlots.get(i).setStack(stacks.get(i));
        }
        for (TargetSlot targetSlot : targetSlots) {
            targetSlot.setChanged();
        }
    }

    private record TargetSlot(BundleTargetSlot slot) {
        ItemStack stack() {
            return slot.stack();
        }

        void setStack(ItemStack stack) {
            slot.setStack(stack);
        }

        void setChanged() {
            slot.setChanged().run();
        }
    }
}
