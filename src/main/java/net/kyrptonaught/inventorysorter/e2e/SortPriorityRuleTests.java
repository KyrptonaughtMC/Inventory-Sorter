package net.kyrptonaught.inventorysorter.e2e;

//? if fabric
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.inventorysorter.inventory.ServerInventorySorter;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.e2e.TestUtils.Scenario;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.List;
import java.util.Map;

import static net.kyrptonaught.inventorysorter.e2e.TestUtils.assertContents;
import static net.kyrptonaught.inventorysorter.e2e.TestUtils.setUpScene;

public class SortPriorityRuleTests {
    //? if fabric
    @GameTest
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
                List.of(new SortPriorityRuleSetting("minecraft:bundle", SortPriorityPosition.LAST))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.DIAMOND),
                2, new ItemStack(Items.BUNDLE)
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
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
                        List.of(new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.LAST))
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

    //? if fabric
    @GameTest
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
                        List.of(new SortPriorityRuleSetting("minecraft:bundle", SortPriorityPosition.LAST))
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

    //? if fabric
    @GameTest
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

    //? if fabric
    @GameTest
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

    //? if fabric
    @GameTest
    public void testPriorityIgnoreCommandLeavesMatchingChestSlotsInPlace(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.FEATHER),
                1, new ItemStack(Items.WHITE_SHULKER_BOX),
                2, new ItemStack(Items.DIAMOND),
                3, new ItemStack(Items.BUNDLE),
                4, new ItemStack(Items.APPLE),
                5, new ItemStack(Items.CRAFTING_TABLE)
        ));
        ServerPlayer player = scenario.player();

        runCommand(player, "/invsort priority add ignore #minecraft:shulker_boxes");
        runCommand(player, "/invsort sort");

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.WHITE_SHULKER_BOX),
                2, new ItemStack(Items.BUNDLE),
                3, new ItemStack(Items.CRAFTING_TABLE),
                4, new ItemStack(Items.DIAMOND),
                5, new ItemStack(Items.FEATHER)
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testNamePriorityCommandSupportsPrefixGlobForNamedTools(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, namedStack(Items.DIAMOND_PICKAXE, "Meza's Fortune Pickaxe"),
                1, namedStack(Items.APPLE, "Apple"),
                2, namedStack(Items.IRON_AXE, "Workshop Axe"),
                3, namedStack(Items.FEATHER, "Feather"),
                4, namedStack(Items.BUNDLE, "Bundle")
        ));
        ServerPlayer player = scenario.player();

        runCommand(player, "/invsort priority add last name:\"Meza's *\"");
        runCommand(player, "/invsort sort");

        assertNamedContents(ctx, scenario, Map.of(
                0, namedStack(Items.APPLE, "Apple"),
                1, namedStack(Items.BUNDLE, "Bundle"),
                2, namedStack(Items.FEATHER, "Feather"),
                3, namedStack(Items.IRON_AXE, "Workshop Axe"),
                4, namedStack(Items.DIAMOND_PICKAXE, "Meza's Fortune Pickaxe")
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testNamePriorityRuleSupportsContainsGlobAndCaseInsensitiveMatching(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, namedStack(Items.IRON_PICKAXE, "backup pickaxe"),
                1, namedStack(Items.DIAMOND_AXE, "MEZA'S Axe"),
                2, namedStack(Items.APPLE, "Apple"),
                3, namedStack(Items.NETHERITE_SHOVEL, "Meza's Shovel"),
                4, namedStack(Items.FEATHER, "Feather")
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                SortType.NAME,
                List.of(new SortPriorityRuleSetting("name:\"*meza's*\"", SortPriorityPosition.FIRST))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertNamedContents(ctx, scenario, Map.of(
                0, namedStack(Items.DIAMOND_AXE, "MEZA'S Axe"),
                1, namedStack(Items.NETHERITE_SHOVEL, "Meza's Shovel"),
                2, namedStack(Items.APPLE, "Apple"),
                3, namedStack(Items.IRON_PICKAXE, "backup pickaxe"),
                4, namedStack(Items.FEATHER, "Feather")
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testNamePriorityRuleDoesNotTreatPlainTextAsContainsMatch(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, namedStack(Items.FEATHER, "Zeta"),
                1, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                2, namedStack(Items.APPLE, "Alpha")
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                SortType.NAME,
                List.of(new SortPriorityRuleSetting("name:\"Meza's\"", SortPriorityPosition.LAST))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertNamedContents(ctx, scenario, Map.of(
                0, namedStack(Items.APPLE, "Alpha"),
                1, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                2, namedStack(Items.FEATHER, "Zeta")
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testNamePriorityRuleCanComposeWithItemRules(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, namedStack(Items.IRON_AXE, "Meza's Axe"),
                1, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                2, namedStack(Items.APPLE, "Apple"),
                3, namedStack(Items.BUNDLE, "Bundle"),
                4, namedStack(Items.FEATHER, "Feather")
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                SortType.NAME,
                List.of(new SortPriorityRuleSetting("name:\"Meza's *\" & !minecraft:diamond_pickaxe", SortPriorityPosition.LAST))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertNamedContents(ctx, scenario, Map.of(
                0, namedStack(Items.APPLE, "Apple"),
                1, namedStack(Items.BUNDLE, "Bundle"),
                2, namedStack(Items.FEATHER, "Feather"),
                3, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                4, namedStack(Items.IRON_AXE, "Meza's Axe")
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testNameIgnoreRuleKeepsMatchingNamedStacksInTheirSlots(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, namedStack(Items.FEATHER, "Feather"),
                1, namedStack(Items.DIAMOND_PICKAXE, "Meza's Vault Pickaxe"),
                2, namedStack(Items.APPLE, "Apple"),
                3, namedStack(Items.IRON_AXE, "Meza's Axe"),
                4, namedStack(Items.BUNDLE, "Bundle")
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                SortType.NAME,
                List.of(
                        new SortPriorityRuleSetting("name:\"Meza's *\"", SortPriorityPosition.FIRST),
                        new SortPriorityRuleSetting("name:\"*Vault*\"", SortPriorityPosition.IGNORE)
                )
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertNamedContents(ctx, scenario, Map.of(
                0, namedStack(Items.IRON_AXE, "Meza's Axe"),
                1, namedStack(Items.DIAMOND_PICKAXE, "Meza's Vault Pickaxe"),
                2, namedStack(Items.APPLE, "Apple"),
                3, namedStack(Items.BUNDLE, "Bundle"),
                4, namedStack(Items.FEATHER, "Feather")
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testNamePriorityRuleCanMatchLocalizedVanillaDisplayNames(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.BUNDLE),
                1, new ItemStack(Items.APPLE),
                2, new ItemStack(Items.DIAMOND),
                3, new ItemStack(Items.FEATHER)
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                SortType.NAME,
                List.of(new SortPriorityRuleSetting("name:\"Bundle\"", SortPriorityPosition.LAST))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.APPLE),
                1, new ItemStack(Items.DIAMOND),
                2, new ItemStack(Items.FEATHER),
                3, new ItemStack(Items.BUNDLE)
        ));
        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testInvalidNamePriorityRuleIsIgnoredAtRuntime(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, namedStack(Items.FEATHER, "Zeta"),
                1, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                2, namedStack(Items.APPLE, "Alpha")
        ));
        SortSettings settings = new SortSettings(
                true,
                false,
                true,
                SortType.NAME,
                List.of(new SortPriorityRuleSetting("name:\"Meza's *", SortPriorityPosition.LAST))
        );

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, settings);

        assertNamedContents(ctx, scenario, Map.of(
                0, namedStack(Items.APPLE, "Alpha"),
                1, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                2, namedStack(Items.FEATHER, "Zeta")
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

    private static ItemStack namedStack(Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static void assertNamedContents(GameTestHelper ctx, Scenario scenario, Map<Integer, ItemStack> expectedContents) {
        assertContents(ctx, scenario, expectedContents);

        for (Map.Entry<Integer, ItemStack> entry : expectedContents.entrySet()) {
            ItemStack stack = scenario.chest().getItem(entry.getKey());
            ctx.assertValueEqual(
                    stack.getHoverName().getString(),
                    entry.getValue().getHoverName().getString(),
                    Component.nullToEmpty("Slot " + entry.getKey() + " does not have the expected display name")
            );
        }
    }

    private static void runCommand(ServerPlayer player, String command) {
        player.level().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
    }

}
