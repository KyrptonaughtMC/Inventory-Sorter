package net.kyrptonaught.inventorysorter.sort.ordering;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.language.MinecraftLocale;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.minecraft.core.component.DataComponents.ITEM_NAME;

class StackOrderingStrategyTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void sortTypeStrategyUsesRequestedLanguageForNameOrdering() {
        ItemStack concrete = namedEgg("白色混凝土");
        ItemStack terracotta = namedEgg("白色陶瓦");
        ItemStack wool = namedEgg("白色羊毛");
        ItemStack stone = namedEgg("光滑的石头");
        ItemStack blackCarpet = namedEgg("黑色地毯");
        ItemStack redCarpet = namedEgg("红色地毯");
        ItemStack ice = namedEgg("透明冰");
        ItemStack glass = namedEgg("透明玻璃");
        ItemStack stairs = namedEgg("砖楼梯");
        ItemStack wall = namedEgg("砖墙");
        List<ItemStack> stacks = new ArrayList<>(List.of(concrete, wool, terracotta, redCarpet, blackCarpet, stairs, wall, stone, ice, glass));

        stacks.sort(StackOrderingStrategy.bySortType(SortType.NAME, "zh_cn").comparator());

        Assertions.assertEquals(List.of(concrete, terracotta, wool, stone, blackCarpet, redCarpet, ice, glass, stairs, wall), stacks);
    }

    @Test
    void factoryCreatesConcreteStrategyForEachSortType() {
        Assertions.assertInstanceOf(NameStackOrderingStrategy.class, StackOrderingStrategy.bySortType(SortType.NAME, "en_us"));
        Assertions.assertInstanceOf(CategoryStackOrderingStrategy.class, StackOrderingStrategy.bySortType(SortType.CATEGORY, "en_us"));
        Assertions.assertInstanceOf(ModStackOrderingStrategy.class, StackOrderingStrategy.bySortType(SortType.MOD, "en_us"));
        Assertions.assertInstanceOf(IdStackOrderingStrategy.class, StackOrderingStrategy.bySortType(SortType.ID, "en_us"));
    }

    @Test
    void idStrategyOrdersByRegistryIdBeforeDefaultNameOrdering() {
        List<ItemStack> stacks = new ArrayList<>(List.of(stack(Items.DIAMOND), stack(Items.APPLE)));

        stacks.sort(StackOrderingStrategy.bySortType(SortType.ID, "en_us").comparator());

        Assertions.assertTrue(stacks.get(0).is(Items.APPLE));
        Assertions.assertTrue(stacks.get(1).is(Items.DIAMOND));
    }

    @Test
    void modStrategyFallsBackToDefaultOrderingWithinTheSameNamespace() {
        List<ItemStack> stacks = new ArrayList<>(List.of(namedEgg("Beta"), namedEgg("Alpha")));

        stacks.sort(StackOrderingStrategy.bySortType(SortType.MOD, "en_us").comparator());

        Assertions.assertEquals("Alpha", stacks.get(0).getHoverName().getString());
        Assertions.assertEquals("Beta", stacks.get(1).getHoverName().getString());
    }

    @Test
    void categoryStrategyUsesDefaultOrderingWhenCreativeTabPositionTies() {
        List<ItemStack> stacks = new ArrayList<>(List.of(namedEgg("Beta"), namedEgg("Alpha")));

        stacks.sort(StackOrderingStrategy.bySortType(SortType.CATEGORY, "en_us").comparator());

        Assertions.assertEquals("Alpha", stacks.get(0).getHoverName().getString());
        Assertions.assertEquals("Beta", stacks.get(1).getHoverName().getString());
    }

    @Test
    void defaultOrderingUsesCountDescendingAsFinalTieBreaker() {
        ItemStack smaller = namedEgg("Egg", 1);
        ItemStack larger = namedEgg("Egg", 4);
        List<ItemStack> stacks = new ArrayList<>(List.of(smaller, larger));

        stacks.sort(new NameStackOrderingStrategy("en_us").comparator());

        Assertions.assertEquals(larger, stacks.get(0));
        Assertions.assertEquals(smaller, stacks.get(1));
    }

    @Test
    void minecraftLocaleParsesLanguageAndCountryOrFallsBackToJvmDefault() {
        Assertions.assertEquals(Locale.of("zh", "CN"), MinecraftLocale.fromLanguageCode("zh_cn"));
        Assertions.assertEquals(Locale.getDefault(), MinecraftLocale.fromLanguageCode("invalid"));
    }

    @Test
    void stackNameKeyUsesProfileNameBeforeHoverName() {
        ItemStack playerHead = namedStack(Items.PLAYER_HEAD, "Fallback");
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("Meza"));

        Assertions.assertEquals("meza", StackNameOrderingKey.value(playerHead));
    }

    @Test
    void stackNameKeyFallsBackToHoverNameWhenProfileHasNoName() {
        ItemStack playerHead = namedStack(Items.PLAYER_HEAD, "Fallback");
        playerHead.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(new java.util.UUID(0, 1)));

        Assertions.assertEquals("fallback", StackNameOrderingKey.value(playerHead));
    }

    @Test
    void enchantedBookNameIncludesStoredEnchantmentMetadata() throws Exception {
        ItemStack enchantedBook = stack(Items.ENCHANTED_BOOK);
        enchantedBook.set(DataComponents.STORED_ENCHANTMENTS, enchantments(
                enchantment("Beta"),
                enchantment("Alpha")
        ));

        String orderingKey = StackNameOrderingKey.value(enchantedBook);

        Assertions.assertTrue(orderingKey.contains(" 2 "));
    }

    @Test
    void enchantedBookNameFormatsSortedEnchantmentNames() {
        Assertions.assertEquals(
                "Enchanted Book 2 Alpha Beta ",
                StackNameOrderingKey.formatEnchantedBookName("Enchanted Book", 2, List.of("Alpha", "Beta"))
        );
    }

    @Test
    void ominousBlockKeyReadsBlockStateComponent() {
        ItemStack normal = stack(Items.TRIAL_SPAWNER);
        ItemStack ominous = stack(Items.TRIAL_SPAWNER);
        ominous.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.of("ominous", "true")));

        Assertions.assertFalse(OminousBlockOrderingKey.value(normal));
        Assertions.assertTrue(OminousBlockOrderingKey.value(ominous));
    }

    @Test
    void ominousBottleKeyUsesOneBasedAmplifierOrdering() {
        ItemStack normal = stack(Items.OMINOUS_BOTTLE);
        ItemStack levelThree = stack(Items.OMINOUS_BOTTLE);
        levelThree.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(2));

        Assertions.assertEquals(0, OminousBottleOrderingKey.amplifier(normal));
        Assertions.assertEquals(3, OminousBottleOrderingKey.amplifier(levelThree));
    }

    @Test
    void creativeTabKeyFallsBackWhenStackIsNotInATab() {
        Assertions.assertEquals(99999, CreativeTabOrderingKey.position(ItemStack.EMPTY));
    }

    @Test
    void creativeTabKeyFindsVanillaStacksWhenTabContentsAreAvailable() {
        ItemStack apple = stack(Items.APPLE);
        List<List<ItemStack>> groups = List.of(
                List.of(stack(Items.DIAMOND)),
                List.of(stack(Items.STICK), apple)
        );

        Assertions.assertEquals(1001, CreativeTabOrderingKey.position(apple, groups));
    }

    private static ItemStack namedEgg(String name) {
        return namedEgg(name, 1);
    }

    private static ItemStack namedEgg(String name, int count) {
        ItemStack stack = new ItemStack(
                Holder.direct(Items.EGG),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
        stack.set(ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack namedStack(Item item, String name) {
        ItemStack stack = stack(item);
        stack.set(ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(
                Holder.direct(item),
                1,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }

    private static ItemEnchantments enchantments(Enchantment... enchantments) throws Exception {
        Object2IntOpenHashMap<Holder<Enchantment>> values = new Object2IntOpenHashMap<>();
        for (Enchantment enchantment : enchantments) {
            values.put(Holder.direct(enchantment), 1);
        }
        Constructor<ItemEnchantments> constructor = ItemEnchantments.class.getDeclaredConstructor(Object2IntOpenHashMap.class);
        constructor.setAccessible(true);
        return constructor.newInstance(values);
    }

    private static Enchantment enchantment(String name) {
        return new Enchantment(
                Component.literal(name),
                Enchantment.definition(
                        HolderSet.direct(Holder.direct(Items.BOOK)),
                        1,
                        1,
                        Enchantment.constantCost(1),
                        Enchantment.constantCost(1),
                        1,
                        EquipmentSlotGroup.MAINHAND
                ),
                HolderSet.empty(),
                DataComponentMap.EMPTY
        );
    }
}
