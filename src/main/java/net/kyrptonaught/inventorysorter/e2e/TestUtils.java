package net.kyrptonaught.inventorysorter.e2e;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
/*? if >=1.21.5 {*/import net.minecraft.text.Text;/*?}*/
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;

public class TestUtils {

    private static ServerPlayerEntity player;

    public record Scenario(ServerPlayerEntity player, ChestBlockEntity chest) {
    }

    private static /*? if <1.21.5 {*//*String*//*?} else {*/Text/*?}*/ getMessage(String message) {
        return /*? if <1.21.5 {*//*message*//*?} else {*/Text.of(message)/*?}*/;
    }

    public static void assertContents(TestContext ctx, Scenario scenario, Map<Integer, ItemStack> expectedContents) {

        int slotCount = scenario.chest.size();

        for (int i = 0; i < slotCount; i++) {
            if (!expectedContents.containsKey(i)) {
                ItemStack stack = scenario.chest.getStack(i);
                ctx.assertEquals(stack, ItemStack.EMPTY, getMessage("Slot " + i + " should be empty"));
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : expectedContents.entrySet()) {
            ItemStack stack = scenario.chest.getStack(entry.getKey());
            ctx.assertEquals(stack.getItem(), entry.getValue().getItem(), getMessage("Slot " + entry.getKey() + " does not have the expected item"));
            ctx.assertEquals(stack.getCount(), entry.getValue().getCount(), getMessage("Slot " + entry.getKey() + " does not have the expected count"));

            int expectedDamage = entry.getValue().getDamage();
            int actualDamage = stack.getDamage();
            ctx.assertEquals(actualDamage, expectedDamage, getMessage("Slot " + entry.getKey() + " does not have the expected damage"));

            if (entry.getValue().getComponents().contains(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER)) {
                int expectedAmplifier = entry.getValue().getComponents().get(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER).value();
                int actualAmplifier = stack.getComponents().get(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER).value();
                ctx.assertEquals(actualAmplifier, expectedAmplifier, getMessage("Slot " + entry.getKey() + " does not have the expected ominous bottle amplifier"));
            }

            if (entry.getValue().getComponents().contains(DataComponentTypes.BLOCK_STATE)) {
                Map<String, String> expectedBlockState = entry.getValue().getComponents().get(DataComponentTypes.BLOCK_STATE).properties();
                Map<String, String> actualBlockState = stack.getComponents().get(DataComponentTypes.BLOCK_STATE).properties();
                ctx.assertEquals(actualBlockState, expectedBlockState, getMessage("Slot " + entry.getKey() + " does not have the expected block state"));
            }

        }
    }

    public static Scenario setUpScene(TestContext ctx, Map<Integer, ItemStack> inventoryContents) {
        if (player == null) {
            player = createMockServerPlayer(ctx);
        }
        BlockPos inventoryPosition = new BlockPos(0, 0, 0);
        BlockPos abspos = ctx.getAbsolutePos(inventoryPosition);
        ctx.setBlockState(inventoryPosition, Blocks.CHEST.getDefaultState());

        player.teleport(abspos.getX() + 2, abspos.getY(), abspos.getZ() + 2, false);
        player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, abspos.toCenterPos());

        ChestBlockEntity chest = (ChestBlockEntity) ctx.getBlockEntity(inventoryPosition/*? if >=1.21.5 {*/, ChestBlockEntity.class/*?}*/);

        for (Map.Entry<Integer, ItemStack> entry : inventoryContents.entrySet()) {
            chest.setStack(entry.getKey(), entry.getValue());
        }

        ctx.useBlock(inventoryPosition, player);

        return new Scenario(player, chest);
    }

    public static ServerPlayerEntity createMockServerPlayer(TestContext ctx) {
        ConnectedClientData connectedClientData = ConnectedClientData.createDefault(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(ctx.getWorld().getServer(), ctx.getWorld(), connectedClientData.gameProfile(), connectedClientData.syncedOptions()) {
            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return false;
            }
        };
        ClientConnection clientConnection = new ClientConnection(NetworkSide.SERVERBOUND);
        new EmbeddedChannel(new ChannelHandler[]{clientConnection});
        ctx.getWorld().getServer().getPlayerManager().onPlayerConnect(clientConnection, serverPlayerEntity, connectedClientData);
        return serverPlayerEntity;
    }

    public static int damageForPercent(Item item, int percent) {
        int maxDamage = item.getComponents().getOrDefault(DataComponentTypes.MAX_DAMAGE, 0);
        return (int) Math.floor(maxDamage * (percent / 100.0));
    }
}
