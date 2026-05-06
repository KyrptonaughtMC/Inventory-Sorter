package net.kyrptonaught.inventorysorter.sort.bundle;

import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Applies vanilla bundle insertion before top-level inventory sorting.
 *
 * <p>The pass is deliberately narrow: it only decides whether loose stacks should
 * move into existing non-empty bundle targets. The normal layout algorithm still
 * owns ignored slots, stack merging, priority ordering, and final slot filling.
 */
public final class BundleInsertionLayoutPass {
    private BundleInsertionLayoutPass() {
    }

    public static List<ItemStack> apply(List<ItemStack> stacks, SortPriorityRules priorityRules) {
        return apply(stacks, List.of(), priorityRules).layoutStacks();
    }

    public static Result apply(List<ItemStack> layoutStacks, List<ItemStack> extraTargetStacks, SortPriorityRules priorityRules) {
        List<ItemStack> output = copyStacks(layoutStacks);
        List<ItemStack> extraTargets = copyStacks(extraTargetStacks);
        List<BundleTarget> targets = new ArrayList<>();
        targets.addAll(findTargets(output, TargetArea.LAYOUT));
        targets.addAll(findTargets(extraTargets, TargetArea.EXTRA_TARGET));
        if (targets.isEmpty()) {
            return new Result(List.copyOf(output), List.copyOf(extraTargets), List.of());
        }

        List<BundleInsertion> insertions = insertionCandidates(output, priorityRules).stream()
                .map(candidate -> insertIntoMatchingTargets(candidate, targets))
                .filter(insertion -> !insertion.targets().isEmpty())
                .toList();
        return new Result(List.copyOf(output), List.copyOf(extraTargets), insertions);
    }

    public static Comparator<ItemStack> targetAwareOrdering(List<ItemStack> stacks, Comparator<ItemStack> comparator) {
        List<ItemStack> directBundleContents = stacks.stream()
                .map(stack -> stack.get(DataComponents.BUNDLE_CONTENTS))
                .filter(contents -> contents != null && !contents.isEmpty())
                .flatMap(BundleContents::itemCopyStream)
                .toList();
        return Comparator
                .comparingInt((ItemStack stack) -> bundleSortBucket(stack, directBundleContents))
                .thenComparing(comparator);
    }

    private static int bundleSortBucket(ItemStack stack, List<ItemStack> directBundleContents) {
        if (hasNonEmptyBundleContents(stack)) {
            return 0;
        }
        if (stack.getMaxStackSize() == 64 && directBundleContents.stream().anyMatch(content -> ItemStack.isSameItemSameComponents(content, stack))) {
            return 1;
        }
        return 2;
    }

    private static List<BundleTarget> findTargets(List<ItemStack> stacks, TargetArea area) {
        List<BundleTarget> targets = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            BundleTarget target = BundleTarget.from(stacks.get(i), new BundleInsertionTarget(area, i));
            if (target != null) {
                targets.add(target);
            }
        }
        return targets;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> output = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            output.add(stack.copy());
        }
        return output;
    }

    private static List<InsertionCandidate> insertionCandidates(List<ItemStack> stacks, SortPriorityRules priorityRules) {
        List<InsertionCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty() || priorityRules.shouldIgnore(stack) || hasNonEmptyBundleContents(stack)) {
                continue;
            }
            candidates.add(new InsertionCandidate(stack, i));
        }
        candidates.sort(Comparator
                .comparingInt((InsertionCandidate candidate) -> candidate.stack().getCount())
                .thenComparingInt(InsertionCandidate::slotIndex));
        return candidates;
    }

    private static BundleInsertion insertIntoMatchingTargets(InsertionCandidate candidate, List<BundleTarget> targets) {
        List<BundleInsertionTarget> insertionTargets = new ArrayList<>();
        for (BundleTarget target : targets) {
            if (candidate.stack().isEmpty()) {
                return new BundleInsertion(candidate.slotIndex(), List.copyOf(insertionTargets));
            }
            if (target.tryInsert(candidate.stack())) {
                insertionTargets.add(target.location());
            }
        }
        return new BundleInsertion(candidate.slotIndex(), List.copyOf(insertionTargets));
    }

    private static boolean hasNonEmptyBundleContents(ItemStack stack) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        return contents != null && !contents.isEmpty();
    }

    private record InsertionCandidate(ItemStack stack, int slotIndex) {
    }

    public record Result(List<ItemStack> layoutStacks, List<ItemStack> extraTargetStacks, List<BundleInsertion> insertions) {
    }

    public enum TargetArea {
        LAYOUT,
        EXTRA_TARGET
    }

    public record BundleInsertion(int sourceLayoutIndex, List<BundleInsertionTarget> targets) {
    }

    public record BundleInsertionTarget(TargetArea area, int index) {
    }

    private record BundleTarget(ItemStack bundle, BundleContents.Mutable mutable, List<ItemStack> directContents, BundleInsertionTarget location) {
        static BundleTarget from(ItemStack stack, BundleInsertionTarget location) {
            BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
            if (contents == null || contents.isEmpty()) {
                return null;
            }
            return new BundleTarget(stack, new BundleContents.Mutable(contents), contents.itemCopyStream().toList(), location);
        }

        boolean tryInsert(ItemStack candidate) {
            if (!matchesDirectContent(candidate)) {
                return false;
            }
            int inserted = mutable.tryInsert(candidate);
            if (inserted > 0) {
                bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                return true;
            }
            return false;
        }

        private boolean matchesDirectContent(ItemStack candidate) {
            return directContents.stream().anyMatch(content -> ItemStack.isSameItemSameComponents(content, candidate));
        }
    }
}
