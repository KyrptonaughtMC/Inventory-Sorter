package net.kyrptonaught.inventorysorter.e2e;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.SortPriorityRule;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.e2e.TestUtils.Scenario;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.List;
import java.util.Map;

import static net.kyrptonaught.inventorysorter.e2e.TestUtils.assertContents;
import static net.kyrptonaught.inventorysorter.e2e.TestUtils.setUpScene;

public class SortPriorityRuleTests {
    @GameTest()
    public void testServerSortUsesPlayerPriorityRules(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.BUNDLE),
                1, new ItemStack(Items.APPLE),
                2, new ItemStack(Items.DIAMOND)
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                SortType.NAME,
                List.of(new SortPriorityRule("minecraft:bundle", SortPriorityPosition.LAST))
        );

        InventoryHelper.sortInventory(scenario.player(), SortTarget.CONTAINER, settings);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.DIAMOND),
                2, new ItemStack(Items.BUNDLE)
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testSortCommandUsesStoredPlayerPriorityRules(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.WHITE_SHULKER_BOX),
                1, new ItemStack(Items.APPLE),
                2, new ItemStack(Items.DIAMOND)
        ));
        PlatformServices.PLAYER_DATA.setSortSettings(
                scenario.player(),
                new SortSettings(
                        true,
                        false,
                        true,
                        SortType.NAME,
                        List.of(new SortPriorityRule("#minecraft:shulker_boxes", SortPriorityPosition.LAST))
                )
        );

        runCommand(scenario.player(), "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.DIAMOND),
                2, new ItemStack(Items.WHITE_SHULKER_BOX)
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testPriorityRulesStayScopedToThePlayerSettings(GameTestHelper ctx) {
        Scenario customSettingsScenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.BUNDLE),
                1, new ItemStack(Items.APPLE),
                2, new ItemStack(Items.DIAMOND)
        ));
        PlatformServices.PLAYER_DATA.setSortSettings(
                customSettingsScenario.player(),
                new SortSettings(
                        true,
                        false,
                        true,
                        SortType.NAME,
                        List.of(new SortPriorityRule("minecraft:bundle", SortPriorityPosition.LAST))
                )
        );

        runCommand(customSettingsScenario.player(), "/invsort sort");

        assertContents(ctx, customSettingsScenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.DIAMOND),
                2, new ItemStack(Items.BUNDLE)
        ));

        Scenario defaultSettingsScenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.BUNDLE),
                1, new ItemStack(Items.APPLE),
                2, new ItemStack(Items.DIAMOND)
        ));

        runCommand(defaultSettingsScenario.player(), "/invsort sort");

        assertContents(ctx, defaultSettingsScenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.BUNDLE),
                2, new ItemStack(Items.DIAMOND)
        ));
        ctx.succeed();
    }

    @GameTest()
    public void testPriorityCommandsChangeChestSortOrderOnTheFly(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, unsortedPriorityCommandChest());
        ServerPlayer player = scenario.player();

        runCommand(player, "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.BUNDLE),
                2, new ItemStack(Items.CRAFTING_TABLE),
                3, new ItemStack(Items.DIAMOND),
                4, new ItemStack(Items.FEATHER),
                5, new ItemStack(Items.WHITE_SHULKER_BOX)
        ));

        refillChest(scenario.chest(), unsortedPriorityCommandChest());
        runCommand(player, "/invsort priority add first #minecraft:shulker_boxes");
        runCommand(player, "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.WHITE_SHULKER_BOX),
                1, new ItemStack(Items.APPLE),
                2, new ItemStack(Items.BUNDLE),
                3, new ItemStack(Items.CRAFTING_TABLE),
                4, new ItemStack(Items.DIAMOND),
                5, new ItemStack(Items.FEATHER)
        ));

        refillChest(scenario.chest(), unsortedPriorityCommandChest());
        runCommand(player, "/invsort priority add first #minecraft:bundles");
        runCommand(player, "/invsort priority move 2 up");
        runCommand(player, "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.BUNDLE),
                1, new ItemStack(Items.WHITE_SHULKER_BOX),
                2, new ItemStack(Items.APPLE),
                3, new ItemStack(Items.CRAFTING_TABLE),
                4, new ItemStack(Items.DIAMOND),
                5, new ItemStack(Items.FEATHER)
        ));

        ctx.succeed();
    }

    @GameTest()
    public void testPrioritySetCommandChangesExistingRuleBeforeSortingChest(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, unsortedPriorityCommandChest());
        ServerPlayer player = scenario.player();

        runCommand(player, "/invsort priority add first #minecraft:shulker_boxes");
        runCommand(player, "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.WHITE_SHULKER_BOX),
                1, new ItemStack(Items.APPLE),
                2, new ItemStack(Items.BUNDLE),
                3, new ItemStack(Items.CRAFTING_TABLE),
                4, new ItemStack(Items.DIAMOND),
                5, new ItemStack(Items.FEATHER)
        ));

        refillChest(scenario.chest(), unsortedPriorityCommandChest());
        runCommand(player, "/invsort priority set 1 last");
        runCommand(player, "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.BUNDLE),
                2, new ItemStack(Items.CRAFTING_TABLE),
                3, new ItemStack(Items.DIAMOND),
                4, new ItemStack(Items.FEATHER),
                5, new ItemStack(Items.WHITE_SHULKER_BOX)
        ));

        ctx.succeed();
    }

    private static Map<Integer, ItemStack> unsortedPriorityCommandChest() {
        return Map.of(
                3, new ItemStack(Items.WHITE_SHULKER_BOX),
                8, new ItemStack(Items.DIAMOND),
                12, new ItemStack(Items.BUNDLE),
                17, new ItemStack(Items.APPLE),
                21, new ItemStack(Items.CRAFTING_TABLE),
                26, new ItemStack(Items.FEATHER)
        );
    }

    private static void refillChest(ChestBlockEntity chest, Map<Integer, ItemStack> contents) {
        chest.clearContent();
        contents.forEach(chest::setItem);
    }

    private static void runCommand(ServerPlayer player, String command) {
        player.level().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
    }
}
