/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.e2e;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = InventorySorterMod.MOD_ID)
public final class NeoForgeGameTests {
    private static final int MAX_TICKS = 400;
    private static final Identifier EMPTY_STRUCTURE = id("gametest/empty");

    private static final SortingTests SORTING_TESTS = new SortingTests();
    private static final SortPriorityRuleTests SORT_PRIORITY_RULE_TESTS = new SortPriorityRuleTests();
    private static final BundleSortingTests BUNDLE_SORTING_TESTS = new BundleSortingTests();

    private static final List<TestCase> TESTS = List.of(
            test("sorting/sort_command_sorts_target_inventory", SORTING_TESTS::testSortCommandSortsTargetInventory),
            test("sorting/sort_me_command_sorts_player_inventory", SORTING_TESTS::testSortMeCommandSortsPlayerInventory),
            test("sorting/player_data_platform_stores_player_data", SORTING_TESTS::testPlayerDataPlatformStoresPlayerData),
            test("sorting/simple_stackable", SORTING_TESTS::testSimpleStackable),
            test("sorting/simple_stackable_with_leftovers", SORTING_TESTS::testSimpleStackableWithLeftovers),
            test("sorting/spectators_cannot_sort", SORTING_TESTS::testSpectatorsCannotSort),
            test("sorting/sort_with_stackables", SORTING_TESTS::testSortWithStackables),
            test("sorting/custom_max_stack_size_sorting", SORTING_TESTS::testCustomMaxStackSizeSorting),
            test("sorting/same_item_different_name", SORTING_TESTS::testSameItemDifferentName),
            test("sorting/simple_pickaxes", SORTING_TESTS::testSimplePickaxes),
            test("sorting/damaged_pickaxes", SORTING_TESTS::testDamagedPickaxes),
            test("sorting/player_heads", SORTING_TESTS::testPlayerHeads),
            test("sorting/enchanted_books", SORTING_TESTS::testEnchantedBooks),
            test("sorting/category_sort", SORTING_TESTS::testCategorySort),
            test("sorting/ominous_potions", SORTING_TESTS::testOminousPotions),
            test("sorting/vaults", SORTING_TESTS::testVaults),

            test("priority/server_sort_uses_player_priority_rules", SORT_PRIORITY_RULE_TESTS::testServerSortUsesPlayerPriorityRules),
            test("priority/sort_command_uses_stored_player_priority_rules", SORT_PRIORITY_RULE_TESTS::testSortCommandUsesStoredPlayerPriorityRules),
            test("priority/priority_rules_stay_scoped_to_the_player_settings", SORT_PRIORITY_RULE_TESTS::testPriorityRulesStayScopedToThePlayerSettings),
            test("priority/priority_commands_change_chest_sort_order_on_the_fly", SORT_PRIORITY_RULE_TESTS::testPriorityCommandsChangeChestSortOrderOnTheFly),
            test("priority/priority_set_command_changes_existing_rule_before_sorting_chest", SORT_PRIORITY_RULE_TESTS::testPrioritySetCommandChangesExistingRuleBeforeSortingChest),
            test("priority/priority_ignore_command_leaves_matching_chest_slots_in_place", SORT_PRIORITY_RULE_TESTS::testPriorityIgnoreCommandLeavesMatchingChestSlotsInPlace),
            test("priority/name_priority_command_supports_prefix_glob_for_named_tools", SORT_PRIORITY_RULE_TESTS::testNamePriorityCommandSupportsPrefixGlobForNamedTools),
            test("priority/name_priority_rule_supports_contains_glob_and_case_insensitive_matching", SORT_PRIORITY_RULE_TESTS::testNamePriorityRuleSupportsContainsGlobAndCaseInsensitiveMatching),
            test("priority/name_priority_rule_does_not_treat_plain_text_as_contains_match", SORT_PRIORITY_RULE_TESTS::testNamePriorityRuleDoesNotTreatPlainTextAsContainsMatch),
            test("priority/name_priority_rule_can_compose_with_item_rules", SORT_PRIORITY_RULE_TESTS::testNamePriorityRuleCanComposeWithItemRules),
            test("priority/name_ignore_rule_keeps_matching_named_stacks_in_their_slots", SORT_PRIORITY_RULE_TESTS::testNameIgnoreRuleKeepsMatchingNamedStacksInTheirSlots),
            test("priority/name_priority_rule_can_match_localized_vanilla_display_names", SORT_PRIORITY_RULE_TESTS::testNamePriorityRuleCanMatchLocalizedVanillaDisplayNames),
            test("priority/invalid_name_priority_rule_is_ignored_at_runtime", SORT_PRIORITY_RULE_TESTS::testInvalidNamePriorityRuleIsIgnoredAtRuntime),

            test("bundles/server_sort_moves_matching_items_into_existing_bundle", BUNDLE_SORTING_TESTS::testServerSortMovesMatchingItemsIntoExistingBundle),
            test("bundles/server_sort_fills_bundle_with_smallest_matching_stack_first", BUNDLE_SORTING_TESTS::testServerSortFillsBundleWithSmallestMatchingStackFirst),
            test("bundles/server_sort_splits_matching_items_across_multiple_existing_bundles", BUNDLE_SORTING_TESTS::testServerSortSplitsMatchingItemsAcrossMultipleExistingBundles),
            test("bundles/server_sort_only_merges_items_into_bundles_that_already_contain_that_item", BUNDLE_SORTING_TESTS::testServerSortOnlyMergesItemsIntoBundlesThatAlreadyContainThatItem),
            test("bundles/server_sort_does_not_merge_different_item_components_into_bundle", BUNDLE_SORTING_TESTS::testServerSortDoesNotMergeDifferentItemComponentsIntoBundle),
            test("bundles/sort_command_uses_stored_sort_into_bundles_setting", BUNDLE_SORTING_TESTS::testSortCommandUsesStoredSortIntoBundlesSetting),
            test("bundles/server_sort_does_not_treat_nested_bundle_contents_as_direct_bundle_contents", BUNDLE_SORTING_TESTS::testServerSortDoesNotTreatNestedBundleContentsAsDirectBundleContents),
            test("bundles/server_sort_does_not_move_non_empty_bundle_into_another_bundle", BUNDLE_SORTING_TESTS::testServerSortDoesNotMoveNonEmptyBundleIntoAnotherBundle),
            test("bundles/server_sort_does_not_use_empty_bundle_as_a_target", BUNDLE_SORTING_TESTS::testServerSortDoesNotUseEmptyBundleAsATarget),
            test("bundles/server_sort_ignores_full_bundle_as_a_target", BUNDLE_SORTING_TESTS::testServerSortIgnoresFullBundleAsATarget),
            test("bundles/server_sort_merges_multiple_direct_item_types_into_one_bundle", BUNDLE_SORTING_TESTS::testServerSortMergesMultipleDirectItemTypesIntoOneBundle),
            test("bundles/server_sort_fills_matching_bundles_in_chest_slot_order", BUNDLE_SORTING_TESTS::testServerSortFillsMatchingBundlesInChestSlotOrder),
            test("bundles/server_sort_uses_vanilla_bundle_weight_for_low_stack_size_items", BUNDLE_SORTING_TESTS::testServerSortUsesVanillaBundleWeightForLowStackSizeItems),
            test("bundles/server_sort_applies_priority_rules_after_bundle_insertion", BUNDLE_SORTING_TESTS::testServerSortAppliesPriorityRulesAfterBundleInsertion),
            test("bundles/server_sort_does_not_move_ignored_items_into_bundles", BUNDLE_SORTING_TESTS::testServerSortDoesNotMoveIgnoredItemsIntoBundles),
            test("bundles/server_sort_prioritizes_smallest_matching_component_stack_when_bundle_space_is_limited", BUNDLE_SORTING_TESTS::testServerSortPrioritizesSmallestMatchingComponentStackWhenBundleSpaceIsLimited),
            test("bundles/server_sort_prioritizes_smallest_matching_stack_across_mixed_bundle_contents", BUNDLE_SORTING_TESTS::testServerSortPrioritizesSmallestMatchingStackAcrossMixedBundleContents),
            test("bundles/server_sort_prioritizes_smallest_matching_low_stack_size_item_when_bundle_space_is_limited", BUNDLE_SORTING_TESTS::testServerSortPrioritizesSmallestMatchingLowStackSizeItemWhenBundleSpaceIsLimited),
            test("bundles/server_player_inventory_sort_can_use_hotbar_bundle_as_target", BUNDLE_SORTING_TESTS::testServerPlayerInventorySortCanUseHotbarBundleAsTarget),
            test("bundles/server_player_inventory_sort_can_use_trinkets_bundle_as_target", BUNDLE_SORTING_TESTS::testServerPlayerInventorySortCanUseTrinketsBundleAsTarget),
            test("bundles/server_player_inventory_sort_does_not_move_items_into_hotbar_bundle_when_bundle_sorting_is_off", BUNDLE_SORTING_TESTS::testServerPlayerInventorySortDoesNotMoveItemsIntoHotbarBundleWhenBundleSortingIsOff),
            test("bundles/server_player_inventory_sort_does_not_move_items_into_hotbar_bundle_when_hotbar_bundle_sorting_is_off", BUNDLE_SORTING_TESTS::testServerPlayerInventorySortDoesNotMoveItemsIntoHotbarBundleWhenHotbarBundleSortingIsOff),
            test("bundles/server_container_sort_does_not_use_player_hotbar_bundle_as_target", BUNDLE_SORTING_TESTS::testServerContainerSortDoesNotUsePlayerHotbarBundleAsTarget)
    );

    private NeoForgeGameTests() {
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("environment/default"), new TestEnvironmentDefinition.AllOf());

        TESTS.forEach(test -> event.registerTest(
                test.instanceId(),
                new DirectFunctionGameTestInstance(test.functionKey(), test.runner(), new TestData<>(environment, EMPTY_STRUCTURE, MAX_TICKS, 0, true))
        ));
    }

    private static TestCase test(String path, Consumer<GameTestHelper> runner) {
        return new TestCase(
                id(path),
                ResourceKey.create(Registries.TEST_FUNCTION, id(path)),
                runner
        );
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, path);
    }

    private record TestCase(
            Identifier instanceId,
            ResourceKey<Consumer<GameTestHelper>> functionKey,
            Consumer<GameTestHelper> runner
    ) {
    }

    private static final class DirectFunctionGameTestInstance extends FunctionGameTestInstance {
        private final Consumer<GameTestHelper> runner;

        private DirectFunctionGameTestInstance(
                ResourceKey<Consumer<GameTestHelper>> functionKey,
                Consumer<GameTestHelper> runner,
                TestData<Holder<TestEnvironmentDefinition<?>>> testData
        ) {
            super(functionKey, testData);
            this.runner = runner;
        }

        @Override
        public void run(GameTestHelper helper) {
            runner.accept(helper);
        }
    }
}
*//*?}*/
