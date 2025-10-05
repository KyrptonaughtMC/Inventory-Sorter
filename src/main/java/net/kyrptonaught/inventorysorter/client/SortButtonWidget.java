package net.kyrptonaught.inventorysorter.client;
import net.kyrptonaught.inventorysorter.mixin.RecipeBookScreenAccessor;

/*? if <1.21.5 {*/
/*import com.mojang.blaze3d.systems.RenderSystem;
*//*?}*/

/*? if >=1.21.6 {*/
import net.minecraft.client.gl.RenderPipelines;
/*?}*/
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
/*? if <1.21.6 {*/
/*import net.minecraft.client.render.RenderLayer;
*//*?}*/
/*? if >= 1.21.9 {*/
import net.minecraft.client.input.AbstractInput;
/*?}*/
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.PLAYER_INVENTORY;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.modifierButton;

@Environment(EnvType.CLIENT)
public class SortButtonWidget extends TexturedButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.of(InventorySorterMod.MOD_ID, "textures/gui/button_unfocused.png"),
            Identifier.of(InventorySorterMod.MOD_ID, "textures/gui/button_focused.png"));
    private final boolean playerInv;
    private final TooltipPositioner widgetTooltipPositioner = HoveredTooltipPositioner.INSTANCE;
    private final InputUtil.Key modifierKey;
    private Screen parentScreen;
    // Offset used to align the sort button with the recipe book in the UI.
    // The value 77 was determined based on the default layout of the Minecraft inventory screen.
    private static final int RECIPE_BOOK_OFFSET = 77;
    private int initialX;

    private static final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> debounceTask;

    public SortButtonWidget(int x, int y, boolean playerInv, Screen parent) {
        super(x, y, 10, 9, TEXTURES, null, Text.literal(""));
        this.playerInv = playerInv;
        this.modifierKey = modifierButton;
        this.parentScreen = parent;
        this.initialX = x;
    }

    @Override
    public void onPress(/*? if >= 1.21.9 {*/AbstractInput input/*?}*/) {
        MinecraftClient instance = MinecraftClient.getInstance();
        String screenID = null;
        if (InventoryHelper.canSortInventory(instance.player)) {
            screenID = Registries.SCREEN_HANDLER.getId(instance.player.currentScreenHandler.getType()).toString();
        }
        if (instance.player.currentScreenHandler instanceof PlayerScreenHandler) {
            screenID = PLAYER_INVENTORY.toString();
        }

        if (screenID == null) {
            InventorySortPacket.sendSortPacket(playerInv);
            return;
        }

        if (isModifierPressed()) {
                getConfig().disableButtonForScreen(screenID);
                compatibility.addShouldHideSortButton(screenID);
                getConfig().save();
                compatibility.reload();
                InventorySorterModClient.syncConfig();
                SystemToast.add(instance.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.translatable("inventorysorter.sortButton.toast.hide.success.title"),
                        Text.translatable("inventorysorter.sortButton.toast.hide.success.description", screenID));
                this.visible = false;

        } else {
            InventorySortPacket.sendSortPacket(playerInv);
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int offset = 0;
        if (!this.visible) return;

        if (this.parentScreen != null && this.parentScreen instanceof RecipeBookScreen<?>) {
            RecipeBookScreen<?> s = (RecipeBookScreen<?>) this.parentScreen;
            RecipeBookWidget<?> widget = ((RecipeBookScreenAccessor) s).getRecipeBook();
            offset = widget.isOpen() ? RECIPE_BOOK_OFFSET : 0;
        }

        setX(this.initialX + offset);

        /*? if <1.21.5 {*/
        /*RenderSystem.setShader(ShaderProgramKeys.POSITION);
        RenderSystem.enableDepthTest();
        *//*?}*/
        /*? if >=1.21.6 {*/

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(.5f, .5f);
        context.getMatrices().translate(getX(), getY());
        Identifier identifier = TEXTURES.get(true, isHovered());
        context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, getX(), getY(), 0, 0, 20, 18, 20, 18);
        context.getMatrices().popMatrix();
        /*?} else {*/
        /*context.getMatrices().push();
        context.getMatrices().scale(.5f, .5f, 1);
        context.getMatrices().translate(getX(), getY(), 0);
        Identifier identifier = TEXTURES.get(true, isHovered());
        context.drawTexture(RenderLayer::getGuiTextured, identifier, getX(), getY(), 0, 0, 20, 18, 20, 18);
        context.getMatrices().pop();
        *//*?}*/
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount, double horizontalAmount) {
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

        if (debounceTask != null) {
            debounceTask.cancel(false);
        }

        debounceTask = debounceExecutor.schedule(() -> {
            config.save();
            InventorySorterModClient.syncConfig();
        }, 300, TimeUnit.MILLISECONDS);

        return true;

    }

    private boolean isModifierPressed() {
        /*? if >= 1.21.9 {*/
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), modifierKey.getCode());
        /*?} else {*/
        /*return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), modifierKey.getCode());
        *//*?}*/

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

            if ((config.scrollBehaviour == ScrollBehaviour.MODIFIER) && isModifierPressed()) {
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

            /*? if >=1.21.6 {*/
            context.drawTooltip(
                    textRenderer,
                    lines,
                    widgetTooltipPositioner,
                    mouseX, mouseY, true
            );
            /*?} else {*/
            /*context.drawTooltip(
                    textRenderer,
                    lines,
                    widgetTooltipPositioner,
                    mouseX, mouseY
            );
            *//*?}*/
        }
    }
}
