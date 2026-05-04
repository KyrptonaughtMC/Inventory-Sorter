package net.kyrptonaught.inventorysorter.client;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jspecify.annotations.NonNull;

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

@Environment(EnvType.CLIENT)
public class SortButtonWidget extends ImageButton {
    private static final int BUTTON_WIDTH = 10;
    private static final int BUTTON_HEIGHT = 9;
    private static final int TEXTURE_WIDTH = 20;
    private static final int TEXTURE_HEIGHT = 18;
    private static final WidgetSprites TEXTURES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, "textures/gui/button_unfocused.png"),
            Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, "textures/gui/button_focused.png"));
    // Offset used to align the sort button with the recipe book in the UI.
    // The value 77 was determined based on the default layout of the Minecraft inventory screen.
    private static final int RECIPE_BOOK_OFFSET = 77;
    private static final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> debounceTask;
    private final ButtonType buttonType;
    private final boolean playerInv;
    private final InputConstants.Key modifierKey;
    private final Screen parentScreen;
    private final int initialX;

    public SortButtonWidget(ButtonType buttonType, int x, int y, boolean playerInv, Screen parent) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURES, null, net.minecraft.network.chat.Component.literal(""));
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
    public void extractContents(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        int offset = 0;
        if (!this.visible) return;

        if (this.parentScreen != null && this.parentScreen instanceof AbstractRecipeBookScreen<?> s) {
            RecipeBookComponent<?> widget = ((RecipeBookScreenAccessor) s).getRecipeBook();
            offset = widget.isVisible() ? RECIPE_BOOK_OFFSET : 0;
        }

        setX(this.initialX + offset);
        Identifier identifier = TEXTURES.get(true, isHovered());
        context.blit(RenderPipelines.GUI_TEXTURED, identifier, getX(), getY(), 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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

        SortType[] sortTypes = SortType.values();
        int direction = verticalAmount > 0 ? 1 : -1;
        int current = Mth.positiveModulo(config.sortType.ordinal() + direction, sortTypes.length);
        config.sortType = sortTypes[current];

        if (debounceTask != null) {
            debounceTask.cancel(false);
        }

        debounceTask = debounceExecutor.schedule(() -> {
            config.save();
            InventorySorterModClient.syncConfig();
        }, 300, TimeUnit.MILLISECONDS);

        return true;

    }

    public static boolean scrollIfHovered(SortButtonWidget button, double x, double y, double verticalAmount, double horizontalAmount) {
        if (button == null || !button.visible || !button.isHovered()) {
            return false;
        }

        return button.mouseScrolled(x, y, verticalAmount, horizontalAmount);
    }

    public static boolean scrollScreenButtonsIfHovered(Screen screen, double x, double y, double verticalAmount, double horizontalAmount) {
        if (!(screen instanceof SortableContainerScreen innerScreen)) {
            return false;
        }

        boolean inventoryButtonScrolled = scrollIfHovered(innerScreen.inventorySorter$getSortButton(), x, y, verticalAmount, horizontalAmount);
        boolean playerButtonScrolled = scrollIfHovered(innerScreen.inventorySorter$getPlayerSortButton(), x, y, verticalAmount, horizontalAmount);
        return inventoryButtonScrolled || playerButtonScrolled;
    }

    private boolean isModifierPressed() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), modifierKey.getValue());
    }


    public void renderTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        NewConfigOptions config = getConfig();
        if (config.showTooltips && this.isHovered()) {
            boolean modifierPressed = isModifierPressed();
            ScrollBehaviour scrollBehaviour = config.scrollBehaviour;
            Component sortType = Component.translatable(config.sortType.getTranslationKey()).withStyle(ChatFormatting.BOLD);
            List<Component> lines = new ArrayList<>();

            if ((scrollBehaviour == ScrollBehaviour.FREE || scrollBehaviour == ScrollBehaviour.DISABLED) && modifierPressed) {
                lines.add(Component.translatable("inventorysorter.sortButton.tooltip.hide"));
            }

            if (scrollBehaviour == ScrollBehaviour.MODIFIER && modifierPressed) {
                lines.add(Component.translatable("inventorysorter.sortButton.tooltip.sortType", sortType));
                lines.add(Component.translatable("inventorysorter.sortButton.tooltip.help.sortType").withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable("inventorysorter.sortButton.tooltip.hide").withStyle(ChatFormatting.GRAY));
            }

            if (!modifierPressed) {
                lines.add(Component.translatable("inventorysorter.sortButton.tooltip.sortType", sortType));
                if (scrollBehaviour == ScrollBehaviour.MODIFIER) {
                    lines.add(Component.translatable("inventorysorter.sortButton.tooltip.help.sortType.modifier", modifierKey.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY));
                } else if (scrollBehaviour != ScrollBehaviour.DISABLED) {
                    lines.add(Component.translatable("inventorysorter.sortButton.tooltip.help.sortType").withStyle(ChatFormatting.DARK_GRAY));
                }
                lines.add(Component.translatable("inventorysorter.sortButton.tooltip.help.hide", modifierKey.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY));

            }

            context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lines, mouseX, mouseY);

        }
    }
}
