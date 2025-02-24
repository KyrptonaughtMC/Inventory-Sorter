package net.kyrptonaught.inventorysorter.e2e;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortCases;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.OminousBottleAmplifierComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.function.IntFunction;

import static net.fabricmc.fabric.api.gametest.v1.FabricGameTest.EMPTY_STRUCTURE;
import static net.kyrptonaught.inventorysorter.e2e.TestUtils.*;

public class SortingTests {

    public static final String template = EMPTY_STRUCTURE;

    @GameTest(templateName = template)
    public void testSimpleStackable(TestContext ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.DIAMOND, 32),
                6, new ItemStack(Items.DIAMOND, 32)
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(0, new ItemStack(Items.DIAMOND, 64)));

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testSimpleStackableWithLeftovers(TestContext ctx) {

        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.DIAMOND, 32),
                6, new ItemStack(Items.DIAMOND, 33)
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.DIAMOND, 64),
                1, new ItemStack(Items.DIAMOND, 1)
        ));

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testSortWithStackables(TestContext ctx) {
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

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

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

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testCustomMaxStackSizeSorting(TestContext ctx) {

        ComponentChanges changes = ComponentChanges.builder().add(DataComponentTypes.MAX_STACK_SIZE, 99).build();

        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.EGG, 7),
                6, new ItemStack(Items.EGG, 8),
                7, new ItemStack(Items.EGG.getRegistryEntry(), 99, changes),
                9, new ItemStack(Items.EGG.getRegistryEntry(), 99, changes),
                11, new ItemStack(Items.EGG.getRegistryEntry(), 99, changes),
                15, new ItemStack(Items.EGG.getRegistryEntry(), 99, changes),
                19, new ItemStack(Items.EGG.getRegistryEntry(), 99, changes),
                26, new ItemStack(Items.EGG.getRegistryEntry(), 99, changes),
                1, new ItemStack(Items.EGG, 16)
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

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

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testSameItemDifferentName(TestContext ctx) {

        ComponentChanges changes = ComponentChanges.builder()
                .add(DataComponentTypes.ITEM_NAME, Text.of("omelette"))
                .build();

        ItemStack omelette = new ItemStack(Items.EGG.getRegistryEntry(), 4, changes);

        Scenario scenario = setUpScene(ctx, Map.of(
                5, new ItemStack(Items.EGG, 7),
                6, new ItemStack(Items.EGG, 8),
                12, new ItemStack(Items.EGG, 4),
                1, omelette
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.EGG, 16),
                1, new ItemStack(Items.EGG, 3),
                2, omelette
        ));

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testSimplePickaxes(TestContext ctx) {
        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(Items.NETHERITE_PICKAXE, 1),
                1, new ItemStack(Items.DIAMOND_PICKAXE, 1),
                2, new ItemStack(Items.IRON_PICKAXE, 1),
                3, new ItemStack(Items.GOLDEN_PICKAXE, 1),
                4, new ItemStack(Items.STONE_PICKAXE, 1),
                5, new ItemStack(Items.WOODEN_PICKAXE, 1)
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(Items.DIAMOND_PICKAXE, 1),
                1, new ItemStack(Items.GOLDEN_PICKAXE, 1),
                2, new ItemStack(Items.IRON_PICKAXE, 1),
                3, new ItemStack(Items.NETHERITE_PICKAXE, 1),
                4, new ItemStack(Items.STONE_PICKAXE, 1),
                5, new ItemStack(Items.WOODEN_PICKAXE, 1)
        ));

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testDamagedPickaxes(TestContext ctx) {
        ItemStack diamondPick80PercentDamaged = new ItemStack(
                RegistryEntry.of(Items.DIAMOND_PICKAXE), 1,
                ComponentChanges.builder().add(DataComponentTypes.DAMAGE, damageForPercent(Items.DIAMOND_PICKAXE, 20)).build());

        ItemStack diamondPick25PercentDamaged = new ItemStack(
                RegistryEntry.of(Items.DIAMOND_PICKAXE), 1,
                ComponentChanges.builder().add(DataComponentTypes.DAMAGE, damageForPercent(Items.DIAMOND_PICKAXE, 75)).build());

        ItemStack netheritePick75PercentDamaged = new ItemStack(
                RegistryEntry.of(Items.NETHERITE_PICKAXE), 1,
                ComponentChanges.builder().add(DataComponentTypes.DAMAGE, damageForPercent(Items.NETHERITE_PICKAXE, 25)).build());

        ItemStack netheritePick50PercentDamaged = new ItemStack(
                RegistryEntry.of(Items.NETHERITE_PICKAXE), 1,
                ComponentChanges.builder().add(DataComponentTypes.DAMAGE, damageForPercent(Items.NETHERITE_PICKAXE, 50)).build());

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

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, diamondPickNotDamaged,
                1, diamondPick80PercentDamaged,
                2, diamondPick25PercentDamaged,
                3, netheritePickNotDamaged,
                4, netheritePick75PercentDamaged,
                5, netheritePick50PercentDamaged
        ));

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testPlayerHeads(TestContext ctx) {

        ProfileComponent houseofmeza = new ProfileComponent(new GameProfile(UUID.randomUUID(), "houseofmeza"));
        ProfileComponent kyrptonaught = new ProfileComponent(new GameProfile(UUID.randomUUID(), "Kyrptonaught"));
        ProfileComponent morgant1c = new ProfileComponent(new GameProfile(UUID.randomUUID(), "morgant1c"));
        ProfileComponent zombie_konsti = new ProfileComponent(new GameProfile(UUID.randomUUID(), "Zombie_konsti"));

        ComponentChanges houseofmezaHead =
                ComponentChanges.builder().add(DataComponentTypes.PROFILE, houseofmeza).build();

        ComponentChanges kyrptonaughtHead =
                ComponentChanges.builder().add(DataComponentTypes.PROFILE, kyrptonaught).build();

        ComponentChanges morgant1cHead =
                ComponentChanges.builder().add(DataComponentTypes.PROFILE, morgant1c).build();

        ComponentChanges zombie_konstiHead =
                ComponentChanges.builder().add(DataComponentTypes.PROFILE, zombie_konsti).build();

        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 1, zombie_konstiHead),
                1, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 4, morgant1cHead),
                2, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 1, houseofmezaHead),
                3, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 32, kyrptonaughtHead),
                4, new ItemStack(Items.PLAYER_HEAD, 16),
                5, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 33, kyrptonaughtHead)
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 1, houseofmezaHead),
                1, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 64, kyrptonaughtHead),
                2, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 1, kyrptonaughtHead),
                3, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 4, morgant1cHead),
                4, new ItemStack(Items.PLAYER_HEAD, 16),
                5, new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 1, zombie_konstiHead)
        ));


        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testEnchantedBooks(TestContext ctx) {
        Registry<Enchantment> registry = ctx.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        ItemStack sharpnessBook = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack silkTouchBook = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack fortune1Book = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack fortune3Book = new ItemStack(Items.ENCHANTED_BOOK, 1);
        ItemStack bulkBook = new ItemStack(Items.ENCHANTED_BOOK, 1);

        EnchantmentHelper.apply(sharpnessBook, builder -> {
            builder.add(registry.getEntry(registry.get(Enchantments.SHARPNESS)), 1);
        });

        EnchantmentHelper.apply(silkTouchBook, builder -> {
            builder.add(registry.getEntry(registry.get(Enchantments.SILK_TOUCH)), 1);
        });

        EnchantmentHelper.apply(fortune1Book, builder -> {
            builder.add(registry.getEntry(registry.get(Enchantments.FORTUNE)), 1);
        });

        EnchantmentHelper.apply(fortune3Book, builder -> {
            builder.add(registry.getEntry(registry.get(Enchantments.FORTUNE)), 3);
        });

        EnchantmentHelper.apply(bulkBook, builder -> {
            builder.add(registry.getEntry(registry.get(Enchantments.SILK_TOUCH)), 1);
            builder.add(registry.getEntry(registry.get(Enchantments.FORTUNE)), 3);
            builder.add(registry.getEntry(registry.get(Enchantments.EFFICIENCY)), 5);
            builder.add(registry.getEntry(registry.get(Enchantments.UNBREAKING)), 3);
        });

        Scenario scenario = setUpScene(ctx, Map.of(
                2, fortune3Book,
                7, bulkBook,
                12, fortune1Book,
                16, silkTouchBook,
                24, sharpnessBook
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, fortune1Book,
                1, fortune3Book,
                2, sharpnessBook,
                3, silkTouchBook,
                4, bulkBook
        ));

        ctx.complete();
    }

    @GameTest(templateName = template)
    public void testCategorySort(TestContext ctx) {
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

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.CATEGORY);

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
        ctx.complete();

    }

    @GameTest(templateName = template)
    public void testOminousPotions(TestContext ctx) {

        IntFunction<ComponentChanges> potionLevel = (int level) -> ComponentChanges.builder()
                .add(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifierComponent(level - 1))
                .build();


        Scenario scenario = setUpScene(ctx, Map.of(
                0, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 12, potionLevel.apply(1)),
                3, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 42, potionLevel.apply(4)),
                6, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 34, potionLevel.apply(5)),
                9, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 55, potionLevel.apply(1)),
                10, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 3, potionLevel.apply(2)),
                12, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 58, potionLevel.apply(3)),
                14, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 45, potionLevel.apply(4)),
                20, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 5, potionLevel.apply(2)),
                25, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 11, potionLevel.apply(4))
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 64, potionLevel.apply(1)),
                1, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 3, potionLevel.apply(1)),
                2, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 8, potionLevel.apply(2)),
                3, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 58, potionLevel.apply(3)),
                4, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 64, potionLevel.apply(4)),
                5, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 34, potionLevel.apply(4)),
                6, new ItemStack(RegistryEntry.of(Items.OMINOUS_BOTTLE), 34, potionLevel.apply(5))
        ));

        ctx.complete();

    }

    @GameTest(templateName = template)
    public void testVaults(TestContext ctx) {
        Boolean2ObjectFunction<ComponentChanges> setOminous = (boolean isOminous) -> ComponentChanges.builder()
                .add(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(Map.of("ominous", String.valueOf(isOminous))))
                .build();

        Scenario scenario = setUpScene(ctx, Map.of(
                2, new ItemStack(RegistryEntry.of(Items.VAULT), 12, setOminous.apply(false)),
                12, new ItemStack(RegistryEntry.of(Items.VAULT), 32, setOminous.apply(true)),
                22, new ItemStack(RegistryEntry.of(Items.VAULT), 10, setOminous.apply(false)),
                6, new ItemStack(RegistryEntry.of(Items.VAULT), 12, setOminous.apply(false)),
                3, new ItemStack(RegistryEntry.of(Items.VAULT), 12, setOminous.apply(true))
        ));

        InventoryHelper.sortInventory(scenario.player(), false, SortCases.SortType.NAME);

        assertContents(ctx, scenario, Map.of(
                0, new ItemStack(RegistryEntry.of(Items.VAULT), 34, setOminous.apply(false)),
                1, new ItemStack(RegistryEntry.of(Items.VAULT), 44, setOminous.apply(true))
        ));

        ctx.complete();
    }
}
