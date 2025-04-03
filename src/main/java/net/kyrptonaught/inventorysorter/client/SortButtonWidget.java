package net.kyrptonaught.inventorysorter.client;

/*? if <1.21.5 {*/
/*import com.mojang.blaze3d.systems.RenderSystem;
*//*?}*/
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.network.InventorySortPacket;
import net.minecraft.client.MinecraftClient;
/*? if <1.21.5 {*/
/*import net.minecraft.client.gl.ShaderProgramKeys;
*//*?}*/
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

@Environment(EnvType.CLIENT)
public class SortButtonWidget extends TexturedButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.of(InventorySorterMod.MOD_ID, "textures/gui/button_unfocused.png"),
            Identifier.of(InventorySorterMod.MOD_ID, "textures/gui/button_focused.png"));
    private final boolean playerInv;

    public SortButtonWidget(int int_1, int int_2, boolean playerInv) {
        super(int_1, int_2, 10, 9, TEXTURES, null, Text.literal(""));
        this.playerInv = playerInv;
    }

    @Override
    public void onPress() {
        if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1) {
            if (InventoryHelper.canSortInventory(MinecraftClient.getInstance().player)) {
                String screenID = Registries.SCREEN_HANDLER.getId(MinecraftClient.getInstance().player.currentScreenHandler.getType()).toString();
                System.out.println("Add the line below to config/inventorysorter.json to blacklist this inventory");
                System.out.println(screenID);

                MutableText MODID = Text.literal("[" + InventorySorterMod.MOD_ID + "]: ").formatted(Formatting.BLUE);
                MutableText autoDNS = (Text.translatable("key.inventorysorter.sortbtn.clickhere"))
                        .formatted(Formatting.UNDERLINE, Formatting.WHITE)
                        .styled(
                                (style) -> style.withClickEvent(
                                        /*? if <1.21.5 {*/
                                        /*new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invsort preventSort " + screenID)
                                        *//*?} else {*/
                                        new ClickEvent.RunCommand("/invsort preventSort " + screenID)
                                        /*?}*/
                                )
                        );

                MutableText autoDND = (Text.translatable("key.inventorysorter.sortbtn.clickhere"))
                        .formatted(Formatting.UNDERLINE, Formatting.WHITE)
                        .styled(
                                (style) -> style.withClickEvent(
                                        /*? if <1.21.5 {*/
                                        /*new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invsort hideButton " + screenID)
                                        *//*?} else {*/
                                        new ClickEvent.RunCommand("/invsort hideButton " + screenID)
                                        /*?}*/
                                )
                        );
                MinecraftClient.getInstance().player.sendMessage(MODID.copyContentOnly().append(autoDNS).append(Text.translatable("key.inventorysorter.sortbtn.dnsadd").formatted(Formatting.WHITE)), false);
                MinecraftClient.getInstance().player.sendMessage(MODID.copyContentOnly().append(autoDND).append(Text.translatable("key.inventorysorter.sortbtn.dndadd").formatted(Formatting.WHITE)), false);
            } else
                MinecraftClient.getInstance().player.sendMessage(Text.literal("[" + InventorySorterMod.MOD_ID + "]: ").append(Text.translatable("key.inventorysorter.sortbtn.error")), false);
        } else
            InventorySortPacket.sendSortPacket(playerInv);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        /*? if <1.21.5 {*/
        /*RenderSystem.setShader(ShaderProgramKeys.POSITION);
        RenderSystem.enableDepthTest();
        *//*?}*/
        context.getMatrices().push();
        context.getMatrices().scale(.5f, .5f, 1);
        context.getMatrices().translate(getX(), getY(), 0);
        Identifier identifier = TEXTURES.get(true, isSelected() || isHovered());
        context.drawTexture(RenderLayer::getGuiTextured, identifier, getX(), getY(), 0, 0, 20, 18, 20, 18);
        this.renderTooltip(context, mouseX, mouseY);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int current = getConfig().sortType.ordinal();
        if (verticalAmount > 0) {
            current++;
            if (current >= SortCases.SortType.values().length)
                current = 0;
        } else {
            current--;
            if (current < 0)
                current = SortCases.SortType.values().length - 1;
        }
        getConfig().sortType = SortCases.SortType.values()[current];
        getConfig().save();
        InventorySorterModClient.syncConfig();
        return true;
    }


    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        if (getConfig().showTooltips && this.isHovered()) {
            List<Text> lines = new ArrayList<>();
            lines.add(Text.translatable("key.inventorysorter.sortbtn.sort").append(Text.translatable(getConfig().sortType.getTranslationKey())));
            if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1) {
                lines.add(Text.translatable("key.inventorysorter.sortbtn.debug"));
                lines.add(Text.translatable("key.inventorysorter.sortbtn.debug2"));
            }
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, lines, getX(), getY());
        }
    }
}
