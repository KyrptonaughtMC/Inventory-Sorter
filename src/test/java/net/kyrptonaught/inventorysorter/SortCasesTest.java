package net.kyrptonaught.inventorysorter;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.core.component.DataComponents.ITEM_NAME;

public class SortCasesTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testChineseSorting() {
        ItemStack stack1 = createStackWithName("白色混凝土");
        ItemStack stack2 = createStackWithName("白色陶瓦");
        ItemStack stack3 = createStackWithName("白色羊毛");
        ItemStack stack4 = createStackWithName("光滑的石头");
        ItemStack stack5 = createStackWithName("黑色地毯");
        ItemStack stack6 = createStackWithName("红色地毯");
        ItemStack stack7 = createStackWithName("透明冰");
        ItemStack stack8 = createStackWithName("透明玻璃");
        ItemStack stack9 = createStackWithName("砖楼梯");
        ItemStack stack10 = createStackWithName("砖墙");

        Comparator<ItemStack> comparator = SortCases.getComparator(SortType.NAME, "zh_cn");

        List<ItemStack> input = new ArrayList<>(List.of(stack1, stack3, stack2, stack6, stack5, stack9, stack10, stack4, stack7, stack8));
        List<ItemStack> expected = List.of(stack1, stack2, stack3, stack4, stack5, stack6, stack7, stack8, stack9, stack10);

        input.sort(comparator);

        for (int i = 0; i < input.size(); i++) {
            System.out.println("Input: " + input.get(i).getItemName().getString());
            Assertions.assertEquals(expected.get(i).getItemName().getString(), input.get(i).getItemName().getString(), "Sorting failed at index " + i);
        }
    }

    private ItemStack createStackWithName(String name) {
        DataComponentPatch changes = DataComponentPatch.builder()
                .set(ITEM_NAME, Component.nullToEmpty(name))
                .build();

        return new ItemStack(Items.EGG.builtInRegistryHolder(), 4, changes);
    }
}
