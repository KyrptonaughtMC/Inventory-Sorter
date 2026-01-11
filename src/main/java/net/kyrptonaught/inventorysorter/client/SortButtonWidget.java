package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.ButtonType;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.config.ScrollBehaviour;
import net.kyrptonaught.inventorysorter.mixin.RecipeBookScreenAccessor;
import net.kyrptonaught.inventorysorter.network.InventorySortPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.PLAYER_INVENTORY;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.modifierButton;

import com.mojang.blaze3d.platform.InputConstants;

@Environment(EnvType.CLIENT)
public class SortButtonWidget extends ImageButton {
    private static final WidgetSprites TEXTURES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, "textures/gui/button_unfocused.png"),
            Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, "textures/gui/button_focused.png"));
    private final ButtonType buttonType;
    private final boolean playerInv;
    private final ClientTooltipPositioner widgetTooltipPositioner = DefaultTooltipPositioner.INSTANCE;
    private final InputConstants.Key modifierKey;
    private final Screen parentScreen;
    // Offset used to align the sort button with the recipe book in the UI.
    // The value 77 was determined based on the default layout of the Minecraft inventory screen.
    private static final int RECIPE_BOOK_OFFSET = 77;
    private final int initialX;

    private static final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> debounceTask;

    public SortButtonWidget(ButtonType buttonType, int x, int y, boolean playerInv, Screen parent) {
        super(x, y, 10, 9, TEXTURES, null, net.minecraft.network.chat.Component.literal(""));
        this.buttonType = buttonType;
        this.playerInv = playerInv;
        this.modifierKey = modifierButton;
        this.parentScreen = parent;
        this.initialX = x;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Minecraft instance = Minecraft.getInstance();
        String screenID = null;
        if (InventoryHelper.canSortInventory(instance.player)) {
            screenID = BuiltInRegistries.MENU.getKey(instance.player.containerMenu.getType()).toString();
        }
        if (instance.player.containerMenu instanceof InventoryMenu) {
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
            SystemToast.add(instance.getToastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                    net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.toast.hide.success.title"),
                    net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.toast.hide.success.description", screenID));
            this.visible = false;

        } else {
            InventorySortPacket.sendSortPacket(playerInv);
        }
    }

    @Override

    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        int offset = 0;
        if (!this.visible) return;

        if (this.parentScreen != null && this.parentScreen instanceof AbstractRecipeBookScreen<?> s) {
            RecipeBookComponent<?> widget = ((RecipeBookScreenAccessor) s).getRecipeBook();
            offset = widget.isVisible() ? RECIPE_BOOK_OFFSET : 0;
        }

        setX(this.initialX + offset);
        context.pose().pushMatrix();
        context.pose().scale(.5f, .5f);
        context.pose().translate(getX(), getY());
        Identifier identifier = TEXTURES.get(true, isHovered());
        context.blit(RenderPipelines.GUI_TEXTURED, identifier, getX(), getY(), 0, 0, 20, 18, 20, 18);
        context.pose().popMatrix();
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
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), modifierKey.getValue());
    }


    public void renderTooltip(GuiGraphics context, int mouseX, int mouseY) {
        NewConfigOptions config = getConfig();
        if (config.showTooltips && this.isHovered()) {
            Minecraft instance = Minecraft.getInstance();
            Font textRenderer = instance.font;

            List<FormattedCharSequence> lines = new ArrayList<>();

            if ((config.scrollBehaviour == ScrollBehaviour.FREE || config.scrollBehaviour == ScrollBehaviour.DISABLED) && isModifierPressed()) {
                lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.hide").getVisualOrderText());
            }

            if ((config.scrollBehaviour == ScrollBehaviour.MODIFIER) && isModifierPressed()) {
                lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.sortType", net.minecraft.network.chat.Component.translatable(getConfig().sortType.getTranslationKey()).withStyle(ChatFormatting.BOLD)).getVisualOrderText());
                lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.help.sortType").withStyle(ChatFormatting.GRAY).getVisualOrderText());
                lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.hide").withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }

            if (!isModifierPressed()) {
                lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.sortType", net.minecraft.network.chat.Component.translatable(getConfig().sortType.getTranslationKey()).withStyle(ChatFormatting.BOLD)).getVisualOrderText());
                if (config.scrollBehaviour == ScrollBehaviour.MODIFIER) {
                    lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.help.sortType.modifier", modifierKey.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                } else if (config.scrollBehaviour != ScrollBehaviour.DISABLED) {
                    lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.help.sortType").withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                }
                lines.add(net.minecraft.network.chat.Component.translatable("inventorysorter.sortButton.tooltip.help.hide", modifierKey.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());

            }

            context.setTooltipForNextFrame(
                    textRenderer,
                    lines,
                    widgetTooltipPositioner,
                    mouseX, mouseY, true
            );

        }
    }
}
