package net.kyrptonaught.inventorysorter.e2e;

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.impl.SlotGroupImpl;
import eu.pb4.trinkets.impl.SlotTypeImpl;
import eu.pb4.trinkets.impl.data.EntitySlotLoader;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.inventorysorter.inventory.ServerInventorySorter;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.kyrptonaught.inventorysorter.e2e.TestUtils.assertContents;
import static net.kyrptonaught.inventorysorter.e2e.TestUtils.setUpScene;

public class BundleSortingTests {
    @GameTest()
    public void testServerSortMovesMatchingItemsIntoExistingBundle(GameTestHelper ctx) {
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                3, new ItemStack(Items.DIAMOND, 4),
                7, new ItemStack(Items.APPLE, 12),
                12, new ItemStack(Items.FEATHER, 5)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, new ItemStack(Items.DIAMOND, 4),
                2, new ItemStack(Items.FEATHER, 5)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 20));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortFillsBundleWithSmallestMatchingStackFirst(GameTestHelper ctx) {
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 44));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                5, new ItemStack(Items.APPLE, 30),
                9, new ItemStack(Items.APPLE, 6),
                15, new ItemStack(Items.DIAMOND, 2)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, new ItemStack(Items.APPLE, 16),
                2, new ItemStack(Items.DIAMOND, 2)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 64));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortSplitsMatchingItemsAcrossMultipleExistingBundles(GameTestHelper ctx) {
        ItemStack firstAppleBundle = bundleContaining(new ItemStack(Items.APPLE, 60));
        ItemStack secondAppleBundle = bundleContaining(new ItemStack(Items.APPLE, 48));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, firstAppleBundle,
                1, secondAppleBundle,
                7, new ItemStack(Items.APPLE, 20),
                12, new ItemStack(Items.CRAFTING_TABLE, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, firstAppleBundle,
                1, secondAppleBundle,
                2, new ItemStack(Items.CRAFTING_TABLE, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 64));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.APPLE, 64));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortOnlyMergesItemsIntoBundlesThatAlreadyContainThatItem(GameTestHelper ctx) {
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        ItemStack featherBundle = bundleContaining(new ItemStack(Items.FEATHER, 10));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                1, featherBundle,
                5, new ItemStack(Items.APPLE, 6),
                6, new ItemStack(Items.FEATHER, 7),
                7, new ItemStack(Items.DIAMOND, 3),
                8, new ItemStack(Items.STICK, 9)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, featherBundle,
                2, new ItemStack(Items.DIAMOND, 3),
                3, new ItemStack(Items.STICK, 9)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 14));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.FEATHER, 17));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortDoesNotMergeDifferentItemComponentsIntoBundle(GameTestHelper ctx) {
        ItemStack namedApple = namedStack(Items.APPLE, "Lunch");
        ItemStack appleBundle = bundleContaining(namedApple.copyWithCount(4));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                4, namedApple.copyWithCount(3),
                5, new ItemStack(Items.APPLE, 5),
                6, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, new ItemStack(Items.APPLE, 5),
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 7));
        ctx.assertValueEqual(
                "Lunch",
                scenario.chest().getItem(0).get(DataComponents.BUNDLE_CONTENTS).itemCopyStream().findFirst().orElseThrow().getHoverName().getString(),
                Component.nullToEmpty("Named apple should keep its display name inside the bundle")
        );
        ctx.succeed();
    }

    @GameTest()
    public void testSortCommandUsesStoredSortIntoBundlesSetting(GameTestHelper ctx) {
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                4, new ItemStack(Items.APPLE, 12),
                8, new ItemStack(Items.DIAMOND, 1)
        ));

        runCommand(scenario.player(), "/invsort sortIntoBundles off");
        runCommand(scenario.player(), "/invsort sortIntoBundles on");
        runCommand(scenario.player(), "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 20));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortDoesNotTreatNestedBundleContentsAsDirectBundleContents(GameTestHelper ctx) {
        ItemStack directStringBundle = bundleContaining(new ItemStack(Items.STRING, 4));
        ItemStack nestedStringBundle = bundleContaining(bundleContaining(new ItemStack(Items.STRING, 5)));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, directStringBundle,
                1, nestedStringBundle,
                6, new ItemStack(Items.STRING, 3),
                9, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, directStringBundle,
                1, nestedStringBundle,
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.STRING, 7));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.BUNDLE, 1));
        assertNestedBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.STRING, 5));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortDoesNotMoveNonEmptyBundleIntoAnotherBundle(GameTestHelper ctx) {
        ItemStack targetStringBundle = bundleContaining(new ItemStack(Items.STRING, 8));
        ItemStack nonEmptyBundle = bundleContaining(new ItemStack(Items.STRING, 3));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, targetStringBundle,
                4, nonEmptyBundle,
                8, new ItemStack(Items.STRING, 5),
                12, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, targetStringBundle,
                1, nonEmptyBundle,
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.STRING, 13));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.STRING, 3));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortDoesNotUseEmptyBundleAsATarget(GameTestHelper ctx) {
        ItemStack emptyBundle = bundleContaining();
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, emptyBundle,
                1, appleBundle,
                6, new ItemStack(Items.APPLE, 4),
                9, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, emptyBundle,
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 12));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of());
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortIgnoresFullBundleAsATarget(GameTestHelper ctx) {
        ItemStack fullAppleBundle = bundleContaining(new ItemStack(Items.APPLE, 64));
        ItemStack partialAppleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, fullAppleBundle,
                1, partialAppleBundle,
                6, new ItemStack(Items.APPLE, 4),
                9, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, fullAppleBundle,
                1, partialAppleBundle,
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 64));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.APPLE, 12));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortMergesMultipleDirectItemTypesIntoOneBundle(GameTestHelper ctx) {
        ItemStack mixedBundle = bundleContaining(
                new ItemStack(Items.APPLE, 8),
                new ItemStack(Items.STRING, 10)
        );
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, mixedBundle,
                5, new ItemStack(Items.APPLE, 6),
                6, new ItemStack(Items.STRING, 7),
                9, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, mixedBundle,
                1, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(
                Items.APPLE, 14,
                Items.STRING, 17
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortFillsMatchingBundlesInChestSlotOrder(GameTestHelper ctx) {
        ItemStack firstAppleBundle = bundleContaining(new ItemStack(Items.APPLE, 62));
        ItemStack secondAppleBundle = bundleContaining(new ItemStack(Items.APPLE, 40));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, firstAppleBundle,
                1, secondAppleBundle,
                7, new ItemStack(Items.APPLE, 10),
                12, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, firstAppleBundle,
                1, secondAppleBundle,
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 64));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.APPLE, 48));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortUsesVanillaBundleWeightForLowStackSizeItems(GameTestHelper ctx) {
        ItemStack eggBundle = bundleContaining(new ItemStack(Items.EGG, 12));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, eggBundle,
                5, new ItemStack(Items.EGG, 6),
                9, new ItemStack(Items.EGG, 2),
                12, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, eggBundle,
                1, new ItemStack(Items.DIAMOND, 1),
                2, new ItemStack(Items.EGG, 4)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.EGG, 16));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortAppliesPriorityRulesAfterBundleInsertion(GameTestHelper ctx) {
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                4, new ItemStack(Items.APPLE, 5),
                8, new ItemStack(Items.WHITE_SHULKER_BOX),
                12, new ItemStack(Items.DIAMOND, 1)
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                true,
                SortType.NAME,
                List.of(new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.FIRST))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.WHITE_SHULKER_BOX),
                1, appleBundle,
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(1), Map.of(Items.APPLE, 13));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortDoesNotMoveIgnoredItemsIntoBundles(GameTestHelper ctx) {
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                4, new ItemStack(Items.APPLE, 5),
                8, new ItemStack(Items.DIAMOND, 1)
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                true,
                SortType.NAME,
                List.of(new SortPriorityRuleSetting("minecraft:apple", SortPriorityPosition.IGNORE))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, new ItemStack(Items.DIAMOND, 1),
                4, new ItemStack(Items.APPLE, 5)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(Items.APPLE, 8));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortPrioritizesSmallestMatchingComponentStackWhenBundleSpaceIsLimited(GameTestHelper ctx) {
        ItemStack smallNamedApples = namedStack(Items.APPLE, "Small");
        ItemStack largeNamedApples = namedStack(Items.APPLE, "Large");
        ItemStack appleBundle = bundleContaining(
                smallNamedApples.copyWithCount(10),
                largeNamedApples.copyWithCount(50)
        );
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, appleBundle,
                4, largeNamedApples.copyWithCount(4),
                8, smallNamedApples.copyWithCount(2),
                12, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, appleBundle,
                1, largeNamedApples.copyWithCount(2),
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleNamedContents(ctx, scenario.chest().getItem(0), Map.of(
                "Small", 12,
                "Large", 52
        ));
        assertSlotName(ctx, scenario, 1, "Large");
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortPrioritizesSmallestMatchingStackAcrossMixedBundleContents(GameTestHelper ctx) {
        ItemStack mixedBundle = bundleContaining(
                new ItemStack(Items.APPLE, 10),
                new ItemStack(Items.STRING, 50)
        );
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, mixedBundle,
                4, new ItemStack(Items.APPLE, 4),
                8, new ItemStack(Items.STRING, 1),
                12, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, mixedBundle,
                1, new ItemStack(Items.APPLE, 1),
                2, new ItemStack(Items.DIAMOND, 1)
        ));
        assertBundleContents(ctx, scenario.chest().getItem(0), Map.of(
                Items.APPLE, 13,
                Items.STRING, 51
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testServerSortPrioritizesSmallestMatchingLowStackSizeItemWhenBundleSpaceIsLimited(GameTestHelper ctx) {
        ItemStack smallNamedEggs = namedStack(Items.EGG, "Small Eggs");
        ItemStack largeNamedEggs = namedStack(Items.EGG, "Large Eggs");
        ItemStack eggBundle = bundleContaining(
                smallNamedEggs.copyWithCount(6),
                largeNamedEggs.copyWithCount(6)
        );
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                0, eggBundle,
                4, largeNamedEggs.copyWithCount(6),
                8, smallNamedEggs.copyWithCount(1),
                12, new ItemStack(Items.DIAMOND, 1)
        ));

        sortWithBundles(scenario.player());

        assertContents(ctx, scenario, Map.of(
                0, eggBundle,
                1, new ItemStack(Items.DIAMOND, 1),
                2, largeNamedEggs.copyWithCount(3)
        ));
        assertBundleNamedContents(ctx, scenario.chest().getItem(0), Map.of(
                "Small Eggs", 7,
                "Large Eggs", 9
        ));
        assertSlotName(ctx, scenario, 2, "Large Eggs");
        ctx.succeed();
    }

    @GameTest()
    public void testServerPlayerInventorySortCanUseHotbarBundleAsTarget(GameTestHelper ctx) {
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of());
        ServerPlayer player = scenario.player();
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));

        player.getInventory().setItem(0, appleBundle);
        player.getInventory().setItem(12, new ItemStack(Items.STICK, 6));
        player.getInventory().setItem(14, new ItemStack(Items.APPLE, 12));
        player.getInventory().setItem(18, new ItemStack(Items.DIAMOND, 1));

        sortPlayerInventoryWithBundles(player);

        assertHotbarContents(ctx, player, Map.of(0, appleBundle));
        assertBundleContents(ctx, player.getInventory().getItem(0), Map.of(Items.APPLE, 20));
        assertPlayerMainInventoryContents(ctx, player, Map.of(
                9, new ItemStack(Items.DIAMOND, 1),
                10, new ItemStack(Items.STICK, 6)
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testServerPlayerInventorySortCanUseTrinketsBundleAsTarget(GameTestHelper ctx) {
        configureTrinketsRingSlot();
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of());
        ServerPlayer player = scenario.player();
        TrinketInventory ring = TrinketsApi.getAttachment(player).getInventory("hand/ring");
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));

        ctx.assertValueEqual(ring != null, true, Component.nullToEmpty("Expected Trinkets hand/ring inventory"));
        ring.setItem(0, appleBundle);
        player.getInventory().setItem(12, new ItemStack(Items.STICK, 6));
        player.getInventory().setItem(14, new ItemStack(Items.APPLE, 12));
        player.getInventory().setItem(18, new ItemStack(Items.DIAMOND, 1));

        sortPlayerInventoryWithBundles(player);

        assertBundleContents(ctx, ring.getItem(0), Map.of(Items.APPLE, 20));
        assertPlayerMainInventoryContents(ctx, player, Map.of(
                9, new ItemStack(Items.DIAMOND, 1),
                10, new ItemStack(Items.STICK, 6)
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testServerPlayerInventorySortDoesNotMoveItemsIntoHotbarBundleWhenBundleSortingIsOff(GameTestHelper ctx) {
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of());
        ServerPlayer player = scenario.player();
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));

        player.getInventory().setItem(0, appleBundle);
        player.getInventory().setItem(14, new ItemStack(Items.APPLE, 12));
        player.getInventory().setItem(18, new ItemStack(Items.DIAMOND, 1));

        ServerInventorySorter.sort(player, SortTarget.PLAYER_INVENTORY, new SortSettings(
                true,
                false,
                true,
                false,
                SortType.NAME,
                List.of()
        ));

        assertHotbarContents(ctx, player, Map.of(0, appleBundle));
        assertBundleContents(ctx, player.getInventory().getItem(0), Map.of(Items.APPLE, 8));
        assertPlayerMainInventoryContents(ctx, player, Map.of(
                9, new ItemStack(Items.APPLE, 12),
                10, new ItemStack(Items.DIAMOND, 1)
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testServerPlayerInventorySortDoesNotMoveItemsIntoHotbarBundleWhenHotbarBundleSortingIsOff(GameTestHelper ctx) {
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of());
        ServerPlayer player = scenario.player();
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));

        player.getInventory().setItem(0, appleBundle);
        player.getInventory().setItem(14, new ItemStack(Items.APPLE, 12));
        player.getInventory().setItem(18, new ItemStack(Items.DIAMOND, 1));

        ServerInventorySorter.sort(player, SortTarget.PLAYER_INVENTORY, new SortSettings(
                true,
                false,
                true,
                true,
                false,
                SortType.NAME,
                List.of()
        ));

        assertHotbarContents(ctx, player, Map.of(0, appleBundle));
        assertBundleContents(ctx, player.getInventory().getItem(0), Map.of(Items.APPLE, 8));
        assertPlayerMainInventoryContents(ctx, player, Map.of(
                9, new ItemStack(Items.APPLE, 12),
                10, new ItemStack(Items.DIAMOND, 1)
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testServerContainerSortDoesNotUsePlayerHotbarBundleAsTarget(GameTestHelper ctx) {
        TestUtils.Scenario scenario = setUpScene(ctx, Map.of(
                4, new ItemStack(Items.APPLE, 12),
                8, new ItemStack(Items.DIAMOND, 1)
        ));
        ServerPlayer player = scenario.player();
        ItemStack appleBundle = bundleContaining(new ItemStack(Items.APPLE, 8));
        player.getInventory().setItem(0, appleBundle);

        sortWithBundles(player);

        assertHotbarContents(ctx, player, Map.of(0, appleBundle));
        assertBundleContents(ctx, player.getInventory().getItem(0), Map.of(Items.APPLE, 8));
        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE, 12),
                1, new ItemStack(Items.DIAMOND, 1)
        ));
        ctx.succeed();
    }

    private static ItemStack bundleContaining(ItemStack... contents) {
        BundleContents.Mutable mutable = new BundleContents.Mutable(BundleContents.EMPTY);
        for (ItemStack content : contents) {
            mutable.tryInsert(content.copy());
        }

        ItemStack bundle = new ItemStack(Items.BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
        return bundle;
    }

    private static ItemStack namedStack(Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static void sortWithBundles(ServerPlayer player) {
        ServerInventorySorter.sort(player, SortTarget.CONTAINER, new SortSettings(
                true,
                false,
                true,
                true,
                SortType.NAME,
                List.of()
        ));
    }

    private static void sortPlayerInventoryWithBundles(ServerPlayer player) {
        ServerInventorySorter.sort(player, SortTarget.PLAYER_INVENTORY, new SortSettings(
                true,
                false,
                true,
                true,
                SortType.NAME,
                List.of()
        ));
    }

    private static void configureTrinketsRingSlot() {
        SlotTypeImpl ring = new SlotTypeImpl(
                "hand/ring",
                "hand",
                0,
                1,
                Optional.empty(),
                new SlotTypeImpl.ConstantCondition(true),
                new SlotTypeImpl.ConstantCondition(true),
                new SlotTypeImpl.ConstantCondition(true),
                TrinketDropRule.DEFAULT,
                false,
                false,
                1
        );
        SlotGroupImpl hand = new SlotGroupImpl.Builder("hand", -1, 0)
                .addSlot("ring", ring)
                .build();
        EntitySlotLoader.SERVER.setSlots(Map.of(EntityType.PLAYER, Map.of("hand", hand)));
    }

    private static void assertHotbarContents(GameTestHelper ctx, ServerPlayer player, Map<Integer, ItemStack> expectedContents) {
        assertPlayerInventoryRange(ctx, player, 0, 9, expectedContents);
    }

    private static void assertPlayerMainInventoryContents(GameTestHelper ctx, ServerPlayer player, Map<Integer, ItemStack> expectedContents) {
        assertPlayerInventoryRange(ctx, player, 9, 36, expectedContents);
    }

    private static void assertPlayerInventoryRange(GameTestHelper ctx, ServerPlayer player, int startSlot, int endSlot, Map<Integer, ItemStack> expectedContents) {
        for (int slot = startSlot; slot < endSlot; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            ItemStack expectedStack = expectedContents.getOrDefault(slot, ItemStack.EMPTY);

            ctx.assertValueEqual(stack.getItem(), expectedStack.getItem(), Component.nullToEmpty("Player inventory slot " + slot + " does not have the expected item"));
            ctx.assertValueEqual(stack.getCount(), expectedStack.getCount(), Component.nullToEmpty("Player inventory slot " + slot + " does not have the expected count"));
        }
    }

    private static void assertBundleContents(GameTestHelper ctx, ItemStack bundle, Map<Item, Integer> expectedContents) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        ctx.assertValueEqual(bundle.is(Items.BUNDLE), true, Component.nullToEmpty("Expected a bundle stack"));
        ctx.assertValueEqual(contents != null, true, Component.nullToEmpty("Expected bundle contents"));

        Map<Item, Integer> actualContents = new HashMap<>();
        contents.itemCopyStream().forEach(stack -> actualContents.merge(stack.getItem(), stack.getCount(), Integer::sum));

        ctx.assertValueEqual(actualContents, expectedContents, Component.nullToEmpty("Bundle does not have the expected contents"));
    }

    private static void assertBundleNamedContents(GameTestHelper ctx, ItemStack bundle, Map<String, Integer> expectedContents) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        ctx.assertValueEqual(bundle.is(Items.BUNDLE), true, Component.nullToEmpty("Expected a bundle stack"));
        ctx.assertValueEqual(contents != null, true, Component.nullToEmpty("Expected bundle contents"));

        Map<String, Integer> actualContents = new HashMap<>();
        contents.itemCopyStream().forEach(stack -> actualContents.merge(stack.getHoverName().getString(), stack.getCount(), Integer::sum));

        ctx.assertValueEqual(actualContents, expectedContents, Component.nullToEmpty("Bundle does not have the expected named contents"));
    }

    private static void assertNestedBundleContents(GameTestHelper ctx, ItemStack bundle, Map<Item, Integer> expectedNestedContents) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        ItemStack nestedBundle = contents.itemCopyStream()
                .filter(stack -> stack.is(Items.BUNDLE))
                .findFirst()
                .orElse(ItemStack.EMPTY);

        assertBundleContents(ctx, nestedBundle, expectedNestedContents);
    }

    private static void assertSlotName(GameTestHelper ctx, TestUtils.Scenario scenario, int slot, String expectedName) {
        ctx.assertValueEqual(
                scenario.chest().getItem(slot).getHoverName().getString(),
                expectedName,
                Component.nullToEmpty("Slot " + slot + " does not have the expected display name")
        );
    }

    private static void runCommand(net.minecraft.server.level.ServerPlayer player, String command) {
        player.level().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
    }
}
