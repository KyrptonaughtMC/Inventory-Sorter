package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.client.sort.plan.ClientSortClickPlanner;
import net.kyrptonaught.inventorysorter.client.sort.plan.ClientFallbackSortPlanBuilder;
import net.kyrptonaught.inventorysorter.client.sort.plan.PlannedContainerClick;
import net.kyrptonaught.inventorysorter.client.sort.plan.SlotState;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientFallbackPlanningTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void nameSortWithStackMergingCanBePlannedFromConfiguredLayout() {
        assertClientFallbackCanPlan(
                Map.ofEntries(
                        Map.entry(5, stack(Items.DARK_OAK_PLANKS, 32)),
                        Map.entry(6, stack(Items.DARK_OAK_PLANKS, 64)),
                        Map.entry(10, stack(Items.OAK_LOG, 32)),
                        Map.entry(11, stack(Items.OAK_LOG, 32)),
                        Map.entry(14, stack(Items.DARK_OAK_PLANKS, 1)),
                        Map.entry(16, stack(Items.OAK_WOOD, 32)),
                        Map.entry(21, stack(Items.BUNDLE, 1, 1)),
                        Map.entry(22, stack(Items.DARK_OAK_PLANKS, 1)),
                        Map.entry(23, stack(Items.DARK_OAK_PLANKS, 1)),
                        Map.entry(24, stack(Items.OAK_LOG, 64)),
                        Map.entry(25, stack(Items.OAK_LOG, 64))
                ),
                List.of()
        );
    }

    @Test
    void shulkerFirstPriorityCanBePlannedWhenNoStacksNeedMerging() {
        assertClientFallbackCanPlan(
                Map.of(
                        0, stack(Items.STICK, 1),
                        1, stack(Items.CHEST, 1),
                        2, stack(Items.PURPLE_SHULKER_BOX, 1, 1),
                        3, stack(Items.REDSTONE, 1)
                ),
                shulkerRule(SortPriorityPosition.FIRST)
        );
    }

    @Test
    void shulkerFirstPriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                screenshotInventory(),
                shulkerRule(SortPriorityPosition.FIRST)
        );
    }

    @Test
    void shulkerLastPriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                screenshotInventory(),
                shulkerRule(SortPriorityPosition.LAST)
        );
    }

    @Test
    void shulkerIgnorePriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                screenshotInventory(),
                shulkerRule(SortPriorityPosition.IGNORE)
        );
    }

    @Test
    void bareBundleIgnorePriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                screenshotInventoryWithRealBundle(),
                List.of(new SortPriorityRuleSetting("bundle", SortPriorityPosition.IGNORE))
        );
    }

    @Test
    void vanillaBundleNameIgnorePriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                screenshotInventoryWithRealBundle(),
                List.of(new SortPriorityRuleSetting("name:\"Bundle\"", SortPriorityPosition.IGNORE))
        );
    }

    @Test
    void bareBundleIgnorePriorityCanBePlannedFromChestMenuScope() {
        assertClientFallbackCanPlanFromChestMenuScope(
                screenshotInventoryWithRealBundle(),
                List.of(new SortPriorityRuleSetting("bundle", SortPriorityPosition.IGNORE))
        );
    }

    @Test
    void clientFallbackPlansBundleInsertionBeforeContainerLayoutSort() {
        SimpleContainer chest = new SimpleContainer(9);
        TestMenu menu = new TestMenu();
        addSlots(menu, chest, 9);
        chest.setItem(0, bundleContaining(stack(Items.APPLE, 8)));
        chest.setItem(4, stack(Items.APPLE, 6));
        chest.setItem(7, stack(Items.DIAMOND, 1));

        ClientSortScope scope = ClientSortScope.resolve(menu, new SimpleContainer(36), net.kyrptonaught.inventorysorter.SortTarget.CONTAINER, null)
                .orElseThrow();
        Optional<List<PlannedContainerClick>> clicks = new ClientFallbackSortPlanBuilder(new ClientSortClickPlanner()).build(
                scope,
                SortType.NAME,
                "en_us",
                List.of(),
                true,
                true
        );

        Assertions.assertTrue(clicks.isPresent());
        Assertions.assertEquals(List.of(click(4), click(0)), clicks.get().subList(0, 2));

        List<ItemStack> expected = SortedInventoryLayout.from(
                containerStacks(chest),
                SortType.NAME,
                "en_us",
                List.of(),
                true
        ).stacks();
        assertClicksReachDesiredLayout(containerStacks(chest), clicks.get(), expected);
    }

    @Test
    void clientFallbackCanUseHotbarBundleWhenSortingPlayerInventory() {
        SimpleContainer playerInventory = new SimpleContainer(36);
        TestMenu menu = new TestMenu();
        addSlots(menu, playerInventory, 36);
        playerInventory.setItem(0, bundleContaining(stack(Items.APPLE, 8)));
        playerInventory.setItem(10, stack(Items.APPLE, 6));
        playerInventory.setItem(12, stack(Items.DIAMOND, 1));

        ClientSortScope scope = ClientSortScope.resolve(menu, playerInventory, net.kyrptonaught.inventorysorter.SortTarget.PLAYER_INVENTORY, null)
                .orElseThrow();
        Optional<List<PlannedContainerClick>> clicks = new ClientFallbackSortPlanBuilder(new ClientSortClickPlanner()).build(
                scope,
                SortType.NAME,
                "en_us",
                List.of(),
                true,
                true
        );

        Assertions.assertTrue(clicks.isPresent());
        Assertions.assertEquals(List.of(click(10), click(0)), clicks.get().subList(0, 2));

        List<ItemStack> actual = containerStacks(playerInventory);
        applyClicks(actual, clicks.get());
        Assertions.assertEquals(Map.of(Items.APPLE, 14), bundleContents(actual.get(0)));
        assertSameLayoutStack(stack(Items.DIAMOND, 1), actual.get(9));
        Assertions.assertTrue(actual.subList(10, 36).stream().allMatch(ItemStack::isEmpty));
    }

    @Test
    void clientFallbackDoesNotUseHotbarBundleWhenHotbarBundleSortingIsOff() {
        SimpleContainer playerInventory = new SimpleContainer(36);
        TestMenu menu = new TestMenu();
        addSlots(menu, playerInventory, 36);
        playerInventory.setItem(0, bundleContaining(stack(Items.APPLE, 8)));
        playerInventory.setItem(10, stack(Items.APPLE, 6));
        playerInventory.setItem(12, stack(Items.DIAMOND, 1));

        ClientSortScope scope = ClientSortScope.resolve(menu, playerInventory, net.kyrptonaught.inventorysorter.SortTarget.PLAYER_INVENTORY, null)
                .orElseThrow();
        Optional<List<PlannedContainerClick>> clicks = new ClientFallbackSortPlanBuilder(new ClientSortClickPlanner()).build(
                scope,
                SortType.NAME,
                "en_us",
                List.of(),
                true,
                false
        );

        Assertions.assertTrue(clicks.isPresent());
        Assertions.assertFalse(clicks.get().contains(click(0)));

        List<ItemStack> actual = containerStacks(playerInventory);
        applyClicks(actual, clicks.get());
        Assertions.assertEquals(Map.of(Items.APPLE, 8), bundleContents(actual.get(0)));
        assertSameLayoutStack(stack(Items.APPLE, 6), actual.get(9));
        assertSameLayoutStack(stack(Items.DIAMOND, 1), actual.get(10));
    }

    @Test
    void namedStackFirstPriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                mergeHeavyInventoryWithNamedStack(),
                namedToolRule(SortPriorityPosition.FIRST)
        );
    }

    @Test
    void namedStackLastPriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                mergeHeavyInventoryWithNamedStack(),
                namedToolRule(SortPriorityPosition.LAST)
        );
    }

    @Test
    void namedStackIgnorePriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                mergeHeavyInventoryWithNamedStack(),
                namedToolRule(SortPriorityPosition.IGNORE)
        );
    }

    @Test
    void customNamedStackFirstPriorityCanBePlannedWhenOtherStacksNeedMerging() {
        assertClientFallbackCanPlan(
                mergeHeavyInventoryWithCustomNamedStack(),
                namedToolRule(SortPriorityPosition.FIRST)
        );
    }

    @Test
    void sameMergeHeavyInventoryWithoutShulkerCanBePlanned() {
        assertClientFallbackCanPlan(
                Map.ofEntries(
                        Map.entry(5, stack(Items.DARK_OAK_PLANKS, 32)),
                        Map.entry(6, stack(Items.DARK_OAK_PLANKS, 64)),
                        Map.entry(10, stack(Items.OAK_LOG, 32)),
                        Map.entry(11, stack(Items.OAK_LOG, 32)),
                        Map.entry(14, stack(Items.DARK_OAK_PLANKS, 1)),
                        Map.entry(16, stack(Items.OAK_WOOD, 32)),
                        Map.entry(21, stack(Items.BUNDLE, 1, 1)),
                        Map.entry(22, stack(Items.DARK_OAK_PLANKS, 1)),
                        Map.entry(23, stack(Items.DARK_OAK_PLANKS, 1)),
                        Map.entry(24, stack(Items.OAK_LOG, 64)),
                        Map.entry(25, stack(Items.OAK_LOG, 64))
                ),
                shulkerRule(SortPriorityPosition.FIRST)
        );
    }

    private static void assertClientFallbackCanPlan(
            Map<Integer, ItemStack> initialItems,
            List<SortPriorityRuleSetting> rules
    ) {
        SimpleContainer chest = new SimpleContainer(27);
        TestMenu menu = new TestMenu();
        addSlots(menu, chest, 27);
        setItems(chest, initialItems);

        List<ItemStack> desiredStacks = sorted(containerStacks(chest), rules);
        Optional<List<PlannedContainerClick>> clicks = new ClientSortClickPlanner().plan(slotStates(menu), desiredStacks);

        Assertions.assertTrue(
                clicks.isPresent(),
                () -> "Client fallback should plan clicks for sort\ncurrent=" + containerStacks(chest) + "\ndesired=" + desiredStacks
        );
        Assertions.assertFalse(clicks.get().isEmpty(), "Client fallback should emit clicks when the layout changes");
        assertClicksReachDesiredLayout(containerStacks(chest), clicks.get(), desiredStacks);
    }

    private static void assertClientFallbackCanPlanFromChestMenuScope(
            Map<Integer, ItemStack> initialItems,
            List<SortPriorityRuleSetting> rules
    ) {
        SimpleContainer chest = new SimpleContainer(27);
        SimpleContainer playerInventory = new SimpleContainer(36);
        TestMenu menu = new TestMenu();
        addSlots(menu, chest, 27);
        addSlots(menu, playerInventory, 36);
        setItems(chest, initialItems);

        ClientSortScope scope = ClientSortScope.resolve(menu, playerInventory, net.kyrptonaught.inventorysorter.SortTarget.CONTAINER, null)
                .orElseThrow();
        List<ItemStack> currentStacks = scope.slots().stream()
                .map(scopedSlot -> scopedSlot.slot().getItem().copy())
                .toList();
        List<ItemStack> desiredStacks = sorted(currentStacks, rules);
        List<SlotState> slotStates = scope.slots().stream()
                .map(scopedSlot -> new SlotState(scopedSlot.menuSlotIndex(), scopedSlot.slot().getItem().copy()))
                .toList();
        Optional<List<PlannedContainerClick>> clicks = new ClientSortClickPlanner().plan(slotStates, desiredStacks);

        Assertions.assertTrue(
                clicks.isPresent(),
                () -> "Client fallback should plan clicks for sort\ncurrent=" + currentStacks + "\ndesired=" + desiredStacks
        );
        Assertions.assertFalse(clicks.get().isEmpty(), "Client fallback should emit clicks when the layout changes");
    }

    private static Map<Integer, ItemStack> screenshotInventory() {
        return Map.ofEntries(
                Map.entry(5, stack(Items.DARK_OAK_PLANKS, 32)),
                Map.entry(6, stack(Items.DARK_OAK_PLANKS, 64)),
                Map.entry(10, stack(Items.OAK_LOG, 32)),
                Map.entry(11, stack(Items.OAK_LOG, 32)),
                Map.entry(13, stack(Items.PURPLE_SHULKER_BOX, 1, 1)),
                Map.entry(14, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(16, stack(Items.OAK_WOOD, 32)),
                Map.entry(21, stack(Items.BUNDLE, 1, 1)),
                Map.entry(22, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(23, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(24, stack(Items.OAK_LOG, 64)),
                Map.entry(25, stack(Items.OAK_LOG, 64))
        );
    }

    private static Map<Integer, ItemStack> screenshotInventoryWithRealBundle() {
        return Map.ofEntries(
                Map.entry(5, stack(Items.DARK_OAK_PLANKS, 32)),
                Map.entry(6, stack(Items.DARK_OAK_PLANKS, 64)),
                Map.entry(10, stack(Items.OAK_LOG, 32)),
                Map.entry(11, stack(Items.OAK_LOG, 32)),
                Map.entry(13, stack(Items.PURPLE_SHULKER_BOX, 1, 1)),
                Map.entry(14, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(16, stack(Items.OAK_WOOD, 32)),
                Map.entry(21, bundle()),
                Map.entry(22, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(23, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(24, stack(Items.OAK_LOG, 64)),
                Map.entry(25, stack(Items.OAK_LOG, 64))
        );
    }

    private static Map<Integer, ItemStack> mergeHeavyInventoryWithNamedStack() {
        return Map.ofEntries(
                Map.entry(5, stack(Items.DARK_OAK_PLANKS, 32)),
                Map.entry(6, stack(Items.DARK_OAK_PLANKS, 64)),
                Map.entry(10, stack(Items.OAK_LOG, 32)),
                Map.entry(11, stack(Items.OAK_LOG, 32)),
                Map.entry(13, namedStack(Items.DIAMOND_PICKAXE, "Meza's Fortune Pickaxe")),
                Map.entry(14, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(16, stack(Items.OAK_WOOD, 32)),
                Map.entry(21, stack(Items.BUNDLE, 1, 1)),
                Map.entry(22, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(23, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(24, stack(Items.OAK_LOG, 64)),
                Map.entry(25, stack(Items.OAK_LOG, 64))
        );
    }

    private static Map<Integer, ItemStack> mergeHeavyInventoryWithCustomNamedStack() {
        return Map.ofEntries(
                Map.entry(5, stack(Items.DARK_OAK_PLANKS, 32)),
                Map.entry(6, stack(Items.DARK_OAK_PLANKS, 64)),
                Map.entry(10, stack(Items.OAK_LOG, 32)),
                Map.entry(11, stack(Items.OAK_LOG, 32)),
                Map.entry(13, customNamedStack(Items.DIAMOND_PICKAXE, "Meza's Fortune Pickaxe")),
                Map.entry(14, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(16, stack(Items.OAK_WOOD, 32)),
                Map.entry(21, stack(Items.BUNDLE, 1, 1)),
                Map.entry(22, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(23, stack(Items.DARK_OAK_PLANKS, 1)),
                Map.entry(24, stack(Items.OAK_LOG, 64)),
                Map.entry(25, stack(Items.OAK_LOG, 64))
        );
    }

    private static List<SortPriorityRuleSetting> shulkerRule(SortPriorityPosition position) {
        return List.of(new SortPriorityRuleSetting("#minecraft:shulker_boxes", position));
    }

    private static List<SortPriorityRuleSetting> namedToolRule(SortPriorityPosition position) {
        return List.of(new SortPriorityRuleSetting("name:\"Meza's *\"", position));
    }

    private static void addSlots(TestMenu menu, Container container, int count) {
        for (int i = 0; i < count; i++) {
            menu.add(new Slot(container, i, 0, 0));
        }
    }

    private static void setItems(Container container, Map<Integer, ItemStack> items) {
        items.forEach(container::setItem);
    }

    private static List<ItemStack> sorted(List<ItemStack> current, List<SortPriorityRuleSetting> rules) {
        TagKey<Item> shulkerBoxes = TagKey.create(Registries.ITEM, Identifier.parse("minecraft:shulker_boxes"));
        try {
            applyItemTags(Map.of(shulkerBoxes, List.of(
                    BuiltInRegistries.ITEM.wrapAsHolder(Items.PURPLE_SHULKER_BOX),
                    BuiltInRegistries.ITEM.wrapAsHolder(Items.WHITE_SHULKER_BOX)
            )));

            return SortedInventoryLayout.from(
                    current,
                    SortType.NAME,
                    "en_us",
                    rules
            ).stacks();
        } finally {
            applyItemTags(Map.of());
        }
    }

    private static void applyItemTags(Map<TagKey<Item>, List<Holder<Item>>> tags) {
        try {
            BuiltInRegistries.ITEM.prepareTagReload(new TagLoader.LoadResult<>(Registries.ITEM, tags)).apply();
        } catch (IllegalStateException e) {
            @SuppressWarnings("unchecked")
            WritableRegistry<Item> itemRegistry = (WritableRegistry<Item>) BuiltInRegistries.ITEM;
            @SuppressWarnings("unchecked")
            MappedRegistry<Item> mappedItemRegistry = (MappedRegistry<Item>) BuiltInRegistries.ITEM;
            mappedItemRegistry.bindAllTagsToEmpty();
            itemRegistry.bindTags(tags);
            BuiltInRegistries.ITEM.freeze();
        }
    }

    private static List<ItemStack> containerStacks(Container container) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            stacks.add(container.getItem(i).copy());
        }
        return stacks;
    }

    private static List<SlotState> slotStates(AbstractContainerMenu menu) {
        List<SlotState> states = new ArrayList<>();
        for (int i = 0; i < menu.slots.size(); i++) {
            states.add(new SlotState(i, menu.slots.get(i).getItem().copy()));
        }
        return states;
    }

    private static void assertClicksReachDesiredLayout(
            List<ItemStack> current,
            List<PlannedContainerClick> clicks,
            List<ItemStack> desired
    ) {
        List<ItemStack> actual = current.stream()
                .map(ItemStack::copy)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        applyClicks(actual, clicks);
        assertSameLayout(actual, desired);
    }

    private static void applyClicks(List<ItemStack> actual, List<PlannedContainerClick> clicks) {
        ItemStack cursor = ItemStack.EMPTY;

        for (PlannedContainerClick click : clicks) {
            Assertions.assertEquals(ContainerInput.PICKUP, click.input());
            Assertions.assertEquals(0, click.button());
            cursor = applyPickupClick(actual, cursor, click.slotIndex());
        }

        Assertions.assertTrue(cursor.isEmpty(), "planned clicks must leave the cursor empty");
    }

    private static ItemStack applyPickupClick(List<ItemStack> slots, ItemStack cursor, int slotIndex) {
        ItemStack slot = slots.get(slotIndex);
        if (cursor.isEmpty()) {
            slots.set(slotIndex, ItemStack.EMPTY);
            return slot;
        }

        if (slot.isEmpty()) {
            slots.set(slotIndex, cursor);
            return ItemStack.EMPTY;
        }

        BundleContents contents = slot.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null && slot.getCount() == 1) {
            BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
            int inserted = mutable.tryInsert(cursor);
            if (inserted > 0) {
                slot.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                return cursor.isEmpty() ? ItemStack.EMPTY : cursor;
            }
        }

        if (canMergePickup(cursor, slot)) {
            int moved = Math.min(cursor.getCount(), slot.getMaxStackSize() - slot.getCount());
            slot.grow(moved);
            cursor.shrink(moved);
            return cursor.isEmpty() ? ItemStack.EMPTY : cursor;
        }

        slots.set(slotIndex, cursor);
        return slot;
    }

    private static boolean canMergePickup(ItemStack cursor, ItemStack slot) {
        return ItemStack.isSameItemSameComponents(cursor, slot)
                && cursor.isStackable()
                && slot.isStackable()
                && slot.getCount() < slot.getMaxStackSize();
    }

    private static void assertSameLayout(List<ItemStack> actual, List<ItemStack> expected) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertTrue(
                    sameLayoutStack(actual.get(i), expected.get(i)),
                    "slot " + i + " expected " + expected.get(i) + " but was " + actual.get(i)
            );
        }
    }

    private static boolean sameLayoutStack(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return first.getCount() == second.getCount() && ItemStack.isSameItemSameComponents(first, second);
    }

    private static void assertSameLayoutStack(ItemStack expected, ItemStack actual) {
        Assertions.assertTrue(
                sameLayoutStack(expected, actual),
                () -> "Expected " + expected + " but was " + actual
        );
    }

    private static Map<Item, Integer> bundleContents(ItemStack bundle) {
        return bundle.get(DataComponents.BUNDLE_CONTENTS)
                .itemCopyStream()
                .collect(java.util.stream.Collectors.toMap(ItemStack::getItem, ItemStack::getCount, Integer::sum));
    }

    private static PlannedContainerClick click(int slot) {
        return new PlannedContainerClick(slot, 0, ContainerInput.PICKUP);
    }

    private static ItemStack stack(Item item, int count) {
        return stack(item, count, 64);
    }

    private static ItemStack stack(Item item, int count, int maxStackSize) {
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, maxStackSize).build()
        );
    }

    private static ItemStack namedStack(Item item, String name) {
        ItemStack stack = stack(item, 1, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack customNamedStack(Item item, String name) {
        ItemStack stack = stack(item, 1, 1);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack bundle() {
        return new ItemStack(
                Holder.direct(Items.BUNDLE),
                1,
                DataComponentPatch.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 1)
                        .set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
                        .build()
        );
    }

    private static ItemStack bundleContaining(ItemStack... contents) {
        BundleContents.Mutable mutable = new BundleContents.Mutable(BundleContents.EMPTY);
        for (ItemStack content : contents) {
            mutable.tryInsert(content.copy());
        }

        ItemStack bundle = bundle();
        bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
        return bundle;
    }

    private static class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super((MenuType<?>) null, 1);
        }

        private void add(Slot slot) {
            addSlot(slot);
        }

        @Override
        public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }
    }
}
