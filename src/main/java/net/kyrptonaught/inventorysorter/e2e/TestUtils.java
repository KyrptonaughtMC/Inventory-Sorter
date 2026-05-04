package net.kyrptonaught.inventorysorter.e2e;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.Map;
import java.util.UUID;

public class TestUtils {
    public static boolean IS_SPECTATOR = true;
    private static ServerPlayer player;

    private static Component getMessage(String message) {
        return Component.nullToEmpty(message);
    }

    public static void assertContents(GameTestHelper ctx, Scenario scenario, Map<Integer, ItemStack> expectedContents) {

        int slotCount = scenario.chest.getContainerSize();

        for (int i = 0; i < slotCount; i++) {
            if (!expectedContents.containsKey(i)) {
                ItemStack stack = scenario.chest.getItem(i);
                ctx.assertValueEqual(stack, ItemStack.EMPTY, getMessage("Slot " + i + " should be empty"));
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : expectedContents.entrySet()) {
            ItemStack stack = scenario.chest.getItem(entry.getKey());
            ctx.assertValueEqual(stack.getItem(), entry.getValue().getItem(), getMessage("Slot " + entry.getKey() + " does not have the expected item"));
            ctx.assertValueEqual(stack.getCount(), entry.getValue().getCount(), getMessage("Slot " + entry.getKey() + " does not have the expected count"));

            int expectedDamage = entry.getValue().getDamageValue();
            int actualDamage = stack.getDamageValue();
            ctx.assertValueEqual(actualDamage, expectedDamage, getMessage("Slot " + entry.getKey() + " does not have the expected damage"));

            if (entry.getValue().getComponents().has(DataComponents.OMINOUS_BOTTLE_AMPLIFIER)) {
                int expectedAmplifier = entry.getValue().getComponents().get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER).value();
                int actualAmplifier = stack.getComponents().get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER).value();
                ctx.assertValueEqual(actualAmplifier, expectedAmplifier, getMessage("Slot " + entry.getKey() + " does not have the expected ominous bottle amplifier"));
            }

            if (entry.getValue().getComponents().has(DataComponents.BLOCK_STATE)) {
                Map<String, String> expectedBlockState = entry.getValue().getComponents().get(DataComponents.BLOCK_STATE).properties();
                Map<String, String> actualBlockState = stack.getComponents().get(DataComponents.BLOCK_STATE).properties();
                ctx.assertValueEqual(actualBlockState, expectedBlockState, getMessage("Slot " + entry.getKey() + " does not have the expected block state"));
            }

        }
    }

    public static Scenario setUpScene(GameTestHelper ctx, Map<Integer, ItemStack> inventoryContents) {
        return setUpScene(ctx, inventoryContents, false);
    }

    public static Scenario setUpScene(GameTestHelper ctx, Map<Integer, ItemStack> inventoryContents, boolean isSpectator) {
        player = createMockServerPlayer(ctx, isSpectator);
        BlockPos inventoryPosition = new BlockPos(0, 0, 0);
        BlockPos abspos = ctx.absolutePos(inventoryPosition);
        ctx.setBlock(inventoryPosition, Blocks.CHEST.defaultBlockState());

        player.randomTeleport(abspos.getX() + 2, abspos.getY(), abspos.getZ() + 2, false);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, abspos.getCenter());

        ChestBlockEntity chest = ctx.getBlockEntity(inventoryPosition, ChestBlockEntity.class);

        for (Map.Entry<Integer, ItemStack> entry : inventoryContents.entrySet()) {
            chest.setItem(entry.getKey(), entry.getValue());
        }

        ctx.useBlock(inventoryPosition, player);

        return new Scenario(player, chest);
    }

    public static ServerPlayer createMockServerPlayer(GameTestHelper ctx, boolean isSpectator) {
        CommonListenerCookie connectedClientData = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer serverPlayerEntity = new ServerPlayer(ctx.getLevel().getServer(), ctx.getLevel(), connectedClientData.gameProfile(), connectedClientData.clientInformation()) {
            public boolean isSpectator() {
                return isSpectator;
            }

            public boolean isCreative() {
                return false;
            }
        };
        Connection clientConnection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(clientConnection);
        ctx.getLevel().getServer().getPlayerList().placeNewPlayer(clientConnection, serverPlayerEntity, connectedClientData);
        return serverPlayerEntity;
    }

    public static int damageForPercent(Item item, int percent) {
        int maxDamage = item.components().getOrDefault(DataComponents.MAX_DAMAGE, 0);
        return (int) Math.floor(maxDamage * (percent / 100.0));
    }

    public record Scenario(ServerPlayer player, ChestBlockEntity chest) {
    }
}
