package net.kyrptonaught.inventorysorter.e2e;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
//? if fabric
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.inventorysorter.inventory.ServerInventorySorter;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.network.ClientSync;
import net.kyrptonaught.inventorysorter.network.LastSeenVersionPacket;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntFunction;

import static net.kyrptonaught.inventorysorter.e2e.TestUtils.*;

public class SortingTests {
    //? if fabric
    @GameTest
    public void testSortCommandSortsTargetInventory(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.DIAMOND, 32),
                6, new ItemStack(Items.DIAMOND, 32)
        ));

        runCommand(scenario.player(), "/invsort sort");

        assertContents(ctx, scenario, Map.of(0, new ItemStack(Items.DIAMOND, 64)));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testSortMeCommandSortsPlayerInventory(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of());
        ServerPlayer player = scenario.player();

        player.getInventory().setItem(14, new ItemStack(Items.DIAMOND, 32));
        player.getInventory().setItem(15, new ItemStack(Items.DIAMOND, 32));

        runCommand(player, "/invsort sortme");

        assertPlayerInventoryContents(ctx, player, Map.of(9, new ItemStack(Items.DIAMOND, 64)));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testPlayerDataPlatformStoresPlayerData(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of());
        ServerPlayer player = scenario.player();

        SortSettings sortSettings = new SortSettings(false, true, false, SortType.MOD);
        PlayerSortPrevention playerSortPrevention = new PlayerSortPrevention(Set.of("minecraft:anvil"));
        ClientSync clientSync = new ClientSync(true);
        LastSeenVersionPacket lastSeenVersion = new LastSeenVersionPacket("26.1.2", "en_us");

        PlatformServices.PLAYER_DATA.setSortSettings(player, sortSettings);
        PlatformServices.PLAYER_DATA.setPlayerSortPrevention(player, playerSortPrevention);
        PlatformServices.PLAYER_DATA.setClientSync(player, clientSync);
        PlatformServices.PLAYER_DATA.setLastSeenVersion(player, lastSeenVersion);

        ctx.assertValueEqual(PlatformServices.PLAYER_DATA.getSortSettings(player), sortSettings, Component.nullToEmpty("Sort settings should round-trip through player data"));
        ctx.assertValueEqual(PlatformServices.PLAYER_DATA.getPlayerSortPrevention(player), playerSortPrevention, Component.nullToEmpty("Player sort prevention should round-trip through player data"));
        ctx.assertValueEqual(PlatformServices.PLAYER_DATA.getClientSync(player), clientSync, Component.nullToEmpty("Client sync should round-trip through player data"));
        ctx.assertValueEqual(PlatformServices.PLAYER_DATA.getLastSeenVersion(player), lastSeenVersion, Component.nullToEmpty("Last seen version should round-trip through player data"));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testSimpleStackable(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.DIAMOND, 32),
                6, new ItemStack(Items.DIAMOND, 32)
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(0, new ItemStack(Items.DIAMOND, 64)));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testSimpleStackableWithLeftovers(GameTestHelper ctx) {

        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.DIAMOND, 32),
                6, new ItemStack(Items.DIAMOND, 33)
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.DIAMOND, 64),
                1, new ItemStack(Items.DIAMOND, 1)
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testSpectatorsCannotSort(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.DIAMOND, 32),
                6, new ItemStack(Items.DIAMOND, 33)
        ), TestUtils.IS_SPECTATOR);

        ServerPlayer player = scenario.player();
        player.setGameMode(GameType.SPECTATOR);

        ServerInventorySorter.sort(player, SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                5, new ItemStack(Items.DIAMOND, 32),
                6, new ItemStack(Items.DIAMOND, 33)
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testSortWithStackables(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.ofEntries(
                Map.entry(0, new ItemStack(Items.ACACIA_LEAVES, 12)),
                Map.entry(1, new ItemStack(Items.BLACKSTONE, 9)),
                Map.entry(2, new ItemStack(Items.CACTUS, 55)),
                Map.entry(3, new ItemStack(Items.EGG, 1)),
                Map.entry(26, new ItemStack(Items.EGG, 2)),
                Map.entry(5, new ItemStack(Items.DIAMOND, 32)),
                Map.entry(15, new ItemStack(Items.DIAMOND, 32)),
                Map.entry(7, new ItemStack(Items.GLASS, 2)),
                Map.entry(8, new ItemStack(Items.FEATHER, 33)),
                Map.entry(24, new ItemStack(Items.HANGING_ROOTS, 40)),
                Map.entry(10, new ItemStack(Items.ITEM_FRAME, 45)),
                Map.entry(11, new ItemStack(Items.HANGING_ROOTS, 51))
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.ofEntries(
                Map.entry(0, new ItemStack(Items.ACACIA_LEAVES, 12)),
                Map.entry(1, new ItemStack(Items.BLACKSTONE, 9)),
                Map.entry(2, new ItemStack(Items.CACTUS, 55)),
                Map.entry(3, new ItemStack(Items.DIAMOND, 64)),
                Map.entry(4, new ItemStack(Items.EGG, 3)),
                Map.entry(5, new ItemStack(Items.FEATHER, 33)),
                Map.entry(6, new ItemStack(Items.GLASS, 2)),
                Map.entry(7, new ItemStack(Items.HANGING_ROOTS, 64)),
                Map.entry(8, new ItemStack(Items.HANGING_ROOTS, 27)),
                Map.entry(9, new ItemStack(Items.ITEM_FRAME, 45))
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testCustomMaxStackSizeSorting(GameTestHelper ctx) {

        DataComponentPatch changes = DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 99).build();

        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.EGG, 7),
                6, new ItemStack(Items.EGG, 8),
                7, stackWithComponents(Items.EGG, 99, changes),
                9, stackWithComponents(Items.EGG, 99, changes),
                11, stackWithComponents(Items.EGG, 99, changes),
                15, stackWithComponents(Items.EGG, 99, changes),
                19, stackWithComponents(Items.EGG, 99, changes),
                26, stackWithComponents(Items.EGG, 99, changes),
                1, new ItemStack(Items.EGG, 16)
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.EGG, 99),
                1, new ItemStack(Items.EGG, 99),
                2, new ItemStack(Items.EGG, 99),
                3, new ItemStack(Items.EGG, 99),
                4, new ItemStack(Items.EGG, 99),
                5, new ItemStack(Items.EGG, 99),
                6, new ItemStack(Items.EGG, 16),
                7, new ItemStack(Items.EGG, 15)
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testSameItemDifferentName(GameTestHelper ctx) {

        DataComponentPatch changes = DataComponentPatch.builder()
                .set(DataComponents.ITEM_NAME, Component.nullToEmpty("omelette"))
                .build();

        ItemStack omelette = stackWithComponents(Items.EGG, 4, changes);

        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.EGG, 7),
                6, new ItemStack(Items.EGG, 8),
                12, new ItemStack(Items.EGG, 4),
                1, omelette
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.EGG, 16),
                1, new ItemStack(Items.EGG, 3),
                2, omelette
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testSimplePickaxes(GameTestHelper ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.NETHERITE_PICKAXE, 1),
                1, new ItemStack(Items.DIAMOND_PICKAXE, 1),
                2, new ItemStack(Items.IRON_PICKAXE, 1),
                3, new ItemStack(Items.GOLDEN_PICKAXE, 1),
                4, new ItemStack(Items.STONE_PICKAXE, 1),
                5, new ItemStack(Items.WOODEN_PICKAXE, 1)
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.DIAMOND_PICKAXE, 1),
                1, new ItemStack(Items.GOLDEN_PICKAXE, 1),
                2, new ItemStack(Items.IRON_PICKAXE, 1),
                3, new ItemStack(Items.NETHERITE_PICKAXE, 1),
                4, new ItemStack(Items.STONE_PICKAXE, 1),
                5, new ItemStack(Items.WOODEN_PICKAXE, 1)
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testDamagedPickaxes(GameTestHelper ctx) {
        ItemStack diamondPick80PercentDamaged = new ItemStack(
                Items.DIAMOND_PICKAXE, 1);
        diamondPick80PercentDamaged.applyComponents(DataComponentPatch.builder().set(DataComponents.DAMAGE, damageForPercent(Items.DIAMOND_PICKAXE, 20)).build());

        ItemStack diamondPick25PercentDamaged = new ItemStack(
                Items.DIAMOND_PICKAXE, 1);
        diamondPick25PercentDamaged.applyComponents(DataComponentPatch.builder().set(DataComponents.DAMAGE, damageForPercent(Items.DIAMOND_PICKAXE, 75)).build());

        ItemStack netheritePick75PercentDamaged = new ItemStack(
                Items.NETHERITE_PICKAXE, 1);
        netheritePick75PercentDamaged.applyComponents(DataComponentPatch.builder().set(DataComponents.DAMAGE, damageForPercent(Items.NETHERITE_PICKAXE, 25)).build());

        ItemStack netheritePick50PercentDamaged = new ItemStack(
                Items.NETHERITE_PICKAXE, 1);
        netheritePick50PercentDamaged.applyComponents(DataComponentPatch.builder().set(DataComponents.DAMAGE, damageForPercent(Items.NETHERITE_PICKAXE, 50)).build());

        ItemStack netheritePickNotDamaged = new ItemStack(Items.NETHERITE_PICKAXE, 1);

        ItemStack diamondPickNotDamaged = new ItemStack(Items.DIAMOND_PICKAXE, 1);

        Scenario scenario = setUpScene(ctx, Map.of(
                0, diamondPick80PercentDamaged,
                2, diamondPick25PercentDamaged,
                23, netheritePick75PercentDamaged,
                12, netheritePick50PercentDamaged,
                16, netheritePickNotDamaged,
                1, diamondPickNotDamaged
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, diamondPickNotDamaged,
                1, diamondPick80PercentDamaged,
                2, diamondPick25PercentDamaged,
                3, netheritePickNotDamaged,
                4, netheritePick75PercentDamaged,
                5, netheritePick50PercentDamaged
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testPlayerHeads(GameTestHelper ctx) {
        ResolvableProfile houseofmeza = ResolvableProfile.createResolved(new GameProfile(UUID.randomUUID(), "houseofmeza"));
        ResolvableProfile kyrptonaught = ResolvableProfile.createResolved(new GameProfile(UUID.randomUUID(), "Kyrptonaught"));
        ResolvableProfile morgant1c = ResolvableProfile.createResolved(new GameProfile(UUID.randomUUID(), "morgant1c"));
        ResolvableProfile zombie_konsti = ResolvableProfile.createResolved(new GameProfile(UUID.randomUUID(), "Zombie_konsti"));

        DataComponentPatch houseofmezaHead =
                DataComponentPatch.builder().set(DataComponents.PROFILE, houseofmeza).build();

        DataComponentPatch kyrptonaughtHead =
                DataComponentPatch.builder().set(DataComponents.PROFILE, kyrptonaught).build();

        DataComponentPatch morgant1cHead =
                DataComponentPatch.builder().set(DataComponents.PROFILE, morgant1c).build();

        DataComponentPatch zombie_konstiHead =
                DataComponentPatch.builder().set(DataComponents.PROFILE, zombie_konsti).build();

        Scenario scenario = setUpScene(ctx, Map.of(
                0, stackWithComponents(Items.PLAYER_HEAD, 1, zombie_konstiHead),
                1, stackWithComponents(Items.PLAYER_HEAD, 4, morgant1cHead),
                2, stackWithComponents(Items.PLAYER_HEAD, 1, houseofmezaHead),
                3, stackWithComponents(Items.PLAYER_HEAD, 32, kyrptonaughtHead),
                4, new ItemStack(Items.PLAYER_HEAD, 16),
                5, stackWithComponents(Items.PLAYER_HEAD, 33, kyrptonaughtHead)
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, stackWithComponents(Items.PLAYER_HEAD, 1, houseofmezaHead),
                1, stackWithComponents(Items.PLAYER_HEAD, 64, kyrptonaughtHead),
                2, stackWithComponents(Items.PLAYER_HEAD, 1, kyrptonaughtHead),
                3, stackWithComponents(Items.PLAYER_HEAD, 4, morgant1cHead),
                4, new ItemStack(Items.PLAYER_HEAD, 16),
                5, stackWithComponents(Items.PLAYER_HEAD, 1, zombie_konstiHead)
        ));


        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testEnchantedBooks(GameTestHelper ctx) {
        Registry<Enchantment> registry = ctx.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack sharpnessBook = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack silkTouchBook = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack fortune1Book = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack fortune3Book = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack bulkBook = new ItemStack(Items.ENCHANTED_BOOK, 1);

        EnchantmentHelper.updateEnchantments(sharpnessBook, builder -> {
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.SHARPNESS)), 1);
        });

        EnchantmentHelper.updateEnchantments(silkTouchBook, builder -> {
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.SILK_TOUCH)), 1);
        });

        EnchantmentHelper.updateEnchantments(fortune1Book, builder -> {
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.FORTUNE)), 1);
        });

        EnchantmentHelper.updateEnchantments(fortune3Book, builder -> {
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.FORTUNE)), 3);
        });

        EnchantmentHelper.updateEnchantments(bulkBook, builder -> {
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.SILK_TOUCH)), 1);
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.FORTUNE)), 3);
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.EFFICIENCY)), 5);
            builder.upgrade(registry.wrapAsHolder(registry.getValue(Enchantments.UNBREAKING)), 3);
        });

        Scenario scenario = setUpScene(ctx, Map.of(
                2, fortune3Book,
                7, bulkBook,
                12, fortune1Book,
                16, silkTouchBook,
                24, sharpnessBook
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, fortune1Book,
                1, fortune3Book,
                2, sharpnessBook,
                3, silkTouchBook,
                4, bulkBook
        ));

        ctx.succeed();
    }

    //? if fabric
    @GameTest
    public void testCategorySort(GameTestHelper ctx) {
        ItemStack coloredBlockStack = new ItemStack(Items.WHITE_WOOL, 64);
        ItemStack naturalBlockStack = new ItemStack(Items.DIRT, 64);
        ItemStack functionalBlockStack = new ItemStack(Items.CRAFTING_TABLE, 64);
        ItemStack redstoneBlockStack = new ItemStack(Items.REDSTONE_BLOCK, 64);
        ItemStack toolStack = new ItemStack(Items.DIAMOND_PICKAXE, 1);
        ItemStack combatStack = new ItemStack(Items.NETHERITE_SWORD, 1);
        ItemStack foodStack = new ItemStack(Items.COOKED_BEEF, 64);
        ItemStack ingredientStack = new ItemStack(Items.WHEAT, 64);
        ItemStack spawnEggStack = new ItemStack(Items.COW_SPAWN_EGG, 64);

        Scenario scenario = setUpScene(ctx, Map.ofEntries(
                Map.entry(20, new ItemStack(Items.OAK_PLANKS, 4)),
                Map.entry(19, new ItemStack(Items.SPRUCE_PLANKS, 14)),
                Map.entry(11, coloredBlockStack),
                Map.entry(23, naturalBlockStack),
                Map.entry(13, functionalBlockStack),
                Map.entry(4, redstoneBlockStack),
                Map.entry(15, toolStack),
                Map.entry(6, combatStack),
                Map.entry(7, foodStack),
                Map.entry(18, ingredientStack),
                Map.entry(9, spawnEggStack)
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.CATEGORY);

        assertContents(ctx, scenario, Map.ofEntries(
                Map.entry(0, new ItemStack(Items.OAK_PLANKS, 4)),
                Map.entry(1, new ItemStack(Items.SPRUCE_PLANKS, 14)),
                Map.entry(2, redstoneBlockStack),
                Map.entry(3, coloredBlockStack),
                Map.entry(4, naturalBlockStack),
                Map.entry(5, functionalBlockStack),
                Map.entry(6, toolStack),
                Map.entry(7, combatStack),
                Map.entry(8, foodStack),
                Map.entry(9, ingredientStack),
                Map.entry(10, spawnEggStack)
        ));
        ctx.succeed();

    }

    //? if fabric
    @GameTest
    public void testOminousPotions(GameTestHelper ctx) {

        IntFunction<DataComponentPatch> potionLevel = (int level) -> DataComponentPatch.builder()
                .set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(level - 1))
                .build();


        Scenario scenario = setUpScene(ctx, Map.of(
                0, stackWithComponents(Items.OMINOUS_BOTTLE, 12, potionLevel.apply(1)),
                3, stackWithComponents(Items.OMINOUS_BOTTLE, 42, potionLevel.apply(4)),
                6, stackWithComponents(Items.OMINOUS_BOTTLE, 34, potionLevel.apply(5)),
                9, stackWithComponents(Items.OMINOUS_BOTTLE, 55, potionLevel.apply(1)),
                10, stackWithComponents(Items.OMINOUS_BOTTLE, 3, potionLevel.apply(2)),
                12, stackWithComponents(Items.OMINOUS_BOTTLE, 58, potionLevel.apply(3)),
                14, stackWithComponents(Items.OMINOUS_BOTTLE, 45, potionLevel.apply(4)),
                20, stackWithComponents(Items.OMINOUS_BOTTLE, 5, potionLevel.apply(2)),
                25, stackWithComponents(Items.OMINOUS_BOTTLE, 11, potionLevel.apply(4))
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, stackWithComponents(Items.OMINOUS_BOTTLE, 64, potionLevel.apply(1)),
                1, stackWithComponents(Items.OMINOUS_BOTTLE, 3, potionLevel.apply(1)),
                2, stackWithComponents(Items.OMINOUS_BOTTLE, 8, potionLevel.apply(2)),
                3, stackWithComponents(Items.OMINOUS_BOTTLE, 58, potionLevel.apply(3)),
                4, stackWithComponents(Items.OMINOUS_BOTTLE, 64, potionLevel.apply(4)),
                5, stackWithComponents(Items.OMINOUS_BOTTLE, 34, potionLevel.apply(4)),
                6, stackWithComponents(Items.OMINOUS_BOTTLE, 34, potionLevel.apply(5))
        ));

        ctx.succeed();

    }

    //? if fabric
    @GameTest
    public void testVaults(GameTestHelper ctx) {
        Boolean2ObjectFunction<DataComponentPatch> setOminous = (boolean isOminous) -> DataComponentPatch.builder()
                .set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.of("ominous", String.valueOf(isOminous))))
                .build();

        Scenario scenario = setUpScene(ctx, Map.of(
                2, stackWithComponents(Items.VAULT, 12, setOminous.apply(false)),
                12, stackWithComponents(Items.VAULT, 32, setOminous.apply(true)),
                22, stackWithComponents(Items.VAULT, 10, setOminous.apply(false)),
                6, stackWithComponents(Items.VAULT, 12, setOminous.apply(false)),
                3, stackWithComponents(Items.VAULT, 12, setOminous.apply(true))
        ));

        ServerInventorySorter.sort(scenario.player(), SortTarget.CONTAINER, SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, stackWithComponents(Items.VAULT, 34, setOminous.apply(false)),
                1, stackWithComponents(Items.VAULT, 44, setOminous.apply(true))
        ));

        ctx.succeed();
    }

    private static ItemStack stackWithComponents(Item item, int count, DataComponentPatch components) {
        ItemStack stack = new ItemStack(item, count);
        stack.applyComponents(components);
        return stack;
    }

    private static void runCommand(ServerPlayer player, String command) {
        player.level().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
    }

    private static void assertPlayerInventoryContents(GameTestHelper ctx, ServerPlayer player, Map<Integer, ItemStack> expectedContents) {
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            ItemStack expectedStack = expectedContents.getOrDefault(slot, ItemStack.EMPTY);

            ctx.assertValueEqual(stack.getItem(), expectedStack.getItem(), Component.nullToEmpty("Player inventory slot " + slot + " does not have the expected item"));
            ctx.assertValueEqual(stack.getCount(), expectedStack.getCount(), Component.nullToEmpty("Player inventory slot " + slot + " does not have the expected count"));
        }
    }
}
