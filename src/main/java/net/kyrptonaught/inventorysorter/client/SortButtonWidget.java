package net.kyrptonaught.inventorysorter.client;

/*? if <1.21.5 {*/
/*import com.mojang.blaze3d.systems.RenderSystem;
*//*?}*/
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.config.ScrollBehaviour;
import net.kyrptonaught.inventorysorter.network.InventorySortPacket;
import net.minecraft.client.MinecraftClient;
/*? if <1.21.5 {*/
/*import net.minecraft.client.gl.ShaderProgramKeys;
*//*?}*/
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.modifierButton;

@Environment(EnvType.CLIENT)
public class SortButtonWidget extends TexturedButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.of(InventorySorterMod.MOD_ID, "textures/gui/button_unfocused.png"),
            Identifier.of(InventorySorterMod.MOD_ID, "textures/gui/button_focused.png"));
    private final boolean playerInv;
    private final TooltipPositioner widgetTooltipPositioner = HoveredTooltipPositioner.INSTANCE;
    private final InputUtil.Key modifierKey;

    public SortButtonWidget(int int_1, int int_2, boolean playerInv) {
        super(int_1, int_2, 10, 9, TEXTURES, null, Text.literal(""));
        this.playerInv = playerInv;
        this.modifierKey = KeyBindingHelper.getBoundKeyOf(modifierButton);
    }

    @Override
    public void onPress() {
        MinecraftClient instance = MinecraftClient.getInstance();
        if (isModifierPressed()) {
            if (InventoryHelper.canSortInventory(instance.player)) {
                String screenID = Registries.SCREEN_HANDLER.getId(instance.player.currentScreenHandler.getType()).toString();
                getConfig().disableButtonForScreen(screenID);
                compatibility.addShouldHideSortButton(screenID);
                getConfig().save();
                compatibility.reload();
                InventorySorterModClient.syncConfig();
                SystemToast.add(instance.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.translatable("inventorysorter.sortButton.toast.hide.success.title"),
                        Text.translatable("inventorysorter.sortButton.toast.hide.success.description", screenID));
            }
        } else {
            InventorySortPacket.sendSortPacket(playerInv);
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (!this.visible) return;
        /*? if <1.21.5 {*/
        /*RenderSystem.setShader(ShaderProgramKeys.POSITION);
        RenderSystem.enableDepthTest();
        *//*?}*/
        context.getMatrices().push();
        context.getMatrices().scale(.5f, .5f, 1);
        context.getMatrices().translate(getX(), getY(), 0);
        Identifier identifier = TEXTURES.get(true, isHovered());
        context.drawTexture(RenderLayer::getGuiTextured, identifier, getX(), getY(), 0, 0, 20, 18, 20, 18);
        context.getMatrices().pop();
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        NewConfigOptions config = getConfig();
        if (config.scrollBehaviour == ScrollBehaviour.DISABLED) {
            return false;
        }

        if ((config.scrollBehaviour == ScrollBehaviour.MODIFIER) && !isModifierPressed()) {
            return false;
        }

        if ((config.scrollBehaviour == ScrollBehaviour.FREE) && isModifierPressed()) {
            return false;
        }

        int current = config.sortType.ordinal();
        if (verticalAmount > 0) {
            current++;
            if (current >= SortType.values().length)
                current = 0;
        } else {
            current--;
            if (current < 0)
                current = SortType.values().length - 1;
        }
        config.sortType = SortType.values()[current];
        config.save();
        InventorySorterModClient.syncConfig();
        return true;
    }

    private boolean isModifierPressed() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), modifierKey.getCode());
    }


    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        NewConfigOptions config = getConfig();
        if (config.showTooltips && this.isHovered()) {
            MinecraftClient instance = MinecraftClient.getInstance();
            TextRenderer textRenderer = instance.textRenderer;

            List<OrderedText> lines = new ArrayList<>();

            if ((config.scrollBehaviour == ScrollBehaviour.FREE || config.scrollBehaviour == ScrollBehaviour.DISABLED) && isModifierPressed()) {
                lines.add(Text.translatable("inventorysorter.sortButton.tooltip.hide").asOrderedText());
            }

            if  ((config.scrollBehaviour == ScrollBehaviour.MODIFIER) && isModifierPressed()) {
                lines.add(Text.translatable("inventorysorter.sortButton.tooltip.sortType", Text.translatable(getConfig().sortType.getTranslationKey()).formatted(Formatting.BOLD)).asOrderedText());
                lines.add(Text.translatable("inventorysorter.sortButton.tooltip.help.sortType").formatted(Formatting.GRAY).asOrderedText());
                lines.add(Text.translatable("inventorysorter.sortButton.tooltip.hide").formatted(Formatting.GRAY).asOrderedText());
            }

            if (!isModifierPressed()) {
                lines.add(Text.translatable("inventorysorter.sortButton.tooltip.sortType", Text.translatable(getConfig().sortType.getTranslationKey()).formatted(Formatting.BOLD)).asOrderedText());
                if (config.scrollBehaviour == ScrollBehaviour.MODIFIER) {
                    lines.add(Text.translatable("inventorysorter.sortButton.tooltip.help.sortType.modifier", modifierKey.getLocalizedText()).formatted(Formatting.DARK_GRAY).asOrderedText());
                } else if (config.scrollBehaviour != ScrollBehaviour.DISABLED) {
                    lines.add(Text.translatable("inventorysorter.sortButton.tooltip.help.sortType").formatted(Formatting.DARK_GRAY).asOrderedText());
                }
                lines.add(Text.translatable("inventorysorter.sortButton.tooltip.help.hide", modifierKey.getLocalizedText()).formatted(Formatting.DARK_GRAY).asOrderedText());

            }

            context.drawTooltip(
                    textRenderer,
                    lines,
                    widgetTooltipPositioner,
                    mouseX, mouseY
            );
        }
    }
}
