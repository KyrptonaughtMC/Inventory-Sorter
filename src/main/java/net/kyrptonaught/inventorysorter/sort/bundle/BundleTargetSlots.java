package net.kyrptonaught.inventorysorter.sort.bundle;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
                targetSlots.add(new TargetSlot(container, slot));
            }
        }
        return new BundleTargetSlots(List.copyOf(targetSlots));
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
        Set<Container> containers = new LinkedHashSet<>();
        for (TargetSlot targetSlot : targetSlots) {
            containers.add(targetSlot.container());
        }
        containers.forEach(Container::setChanged);
    }

    private record TargetSlot(Container container, int slot) {
        ItemStack stack() {
            return container.getItem(slot);
        }

        void setStack(ItemStack stack) {
            container.setItem(slot, stack);
        }
    }
}
