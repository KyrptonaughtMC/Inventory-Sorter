package net.kyrptonaught.inventorysorter.mixin;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.kyrptonaught.inventorysorter.ButtonType;
import net.kyrptonaught.inventorysorter.client.SortButtonDisplayPolicy;
import net.kyrptonaught.inventorysorter.inventory.SortabilityPolicy;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.InventoryScreenId;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.client.SortButtonWidget;
import net.kyrptonaught.inventorysorter.client.SortableContainerScreen;
import net.kyrptonaught.inventorysorter.client.platform.ClientPlatformServices;
import net.kyrptonaught.inventorysorter.client.sort.ClientSorts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

//? if fabric
@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen extends Screen implements SortableContainerScreen {
    @Shadow
    protected int imageWidth;
    @Shadow
    protected int imageHeight;

    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;

    @Shadow
    protected Slot hoveredSlot;

    @Unique
    private SortButtonWidget invsort$SortBtn;
    @Unique
    private SortButtonWidget invsort$PlayerSortBtn;

    protected MixinContainerScreen(Component text_1) {
        super(text_1);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void invsort$init(CallbackInfo callbackinfo) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        if (getConfig().showSortButton && SortButtonDisplayPolicy.shouldDisplayButtons(minecraft.player)) {
            boolean playerOnly = !SortabilityPolicy.canSortInventory(minecraft.player);
            if (playerOnly) {
                invsort$PlayerSortBtn = new SortButtonWidget(ButtonType.PLAYER, this.leftPos + this.imageWidth - 20, this.topPos + imageHeight - 95, SortTarget.PLAYER_INVENTORY);
                invsort$PlayerSortBtn.visible = compatibility.shouldShowSortButton(InventoryScreenId.PLAYER_INVENTORY.value());
                this.addRenderableWidget(invsort$PlayerSortBtn);
            } else {
                invsort$SortBtn = new SortButtonWidget(ButtonType.INVENTORY, this.leftPos + this.imageWidth - 20, this.topPos + 6, SortTarget.CONTAINER);
                this.addRenderableWidget(invsort$SortBtn);

                if (getConfig().separateButton) { // If separate button is enabled, add a player inventory sort button
                    invsort$PlayerSortBtn = new SortButtonWidget(ButtonType.PLAYER, invsort$SortBtn.getX(), this.topPos + getMiddleHeight(), SortTarget.PLAYER_INVENTORY);
                    invsort$PlayerSortBtn.visible = compatibility.shouldShowSortButton(InventoryScreenId.PLAYER_INVENTORY.value());
                    this.addRenderableWidget(invsort$PlayerSortBtn);
                }
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void invsort$mouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        invsort$updateButtonPositions();
        int button = click.button();

        // Keybind check for mouse bindings, client only
        if (minecraft == null || minecraft.player == null) {
            callbackInfoReturnable.setReturnValue(true);
            return;
        }
        if (ClientPlatformServices.KEY_MAPPINGS.sortKeyMapping().matchesMouse(click)) {
            sortInventory(callbackInfoReturnable);
        }


    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void invsort$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        // Keybind check for key bindings, client only
        if (minecraft == null || minecraft.player == null) return;

        if (ClientPlatformServices.KEY_MAPPINGS.sortKeyMapping().matches(input)) {
            sortInventory(callbackInfoReturnable);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("TAIL"), cancellable = true)
    private void invsort$mouseScrolled(double mouseX, double mouseY, double verticalAmount, double horizontalAmount, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (callbackInfoReturnable.getReturnValue()) {
            return;
        }

        boolean inventoryButtonScrolled = SortButtonWidget.scrollIfHovered(invsort$SortBtn, mouseX, mouseY, verticalAmount, horizontalAmount);
        boolean playerButtonScrolled = SortButtonWidget.scrollIfHovered(invsort$PlayerSortBtn, mouseX, mouseY, verticalAmount, horizontalAmount);
        if (inventoryButtonScrolled || playerButtonScrolled) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"))
    private void invsort$updateButtonsBeforeScroll(double mouseX, double mouseY, double verticalAmount, double horizontalAmount, CallbackInfoReturnable<Boolean> ci) {
        invsort$updateButtonPositions();
    }

    @Unique
    private void invsort$updateButtonPositions() {
        int x = this.leftPos + this.imageWidth - 20;
        if (invsort$SortBtn != null) {
            invsort$SortBtn.setPosition(x, this.topPos + 6);
        }
        if (invsort$PlayerSortBtn != null) {
            int y = invsort$SortBtn == null ? this.imageHeight - 95 : getMiddleHeight();
            invsort$PlayerSortBtn.setPosition(x, this.topPos + y);
        }
    }

    @Unique
    private void sortInventory(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        SortTarget target = SortabilityPolicy.canSortInventory(minecraft.player) ? SortTarget.CONTAINER : SortTarget.PLAYER_INVENTORY;
        if (target == SortTarget.CONTAINER && getConfig().sortHighlightedItem) {
            if (hoveredSlot != null)
                target = hoveredSlot.container instanceof Inventory ? SortTarget.PLAYER_INVENTORY : SortTarget.CONTAINER;
        }
        ClientSorts.requestCurrentScreenSort(target);
        callbackInfoReturnable.setReturnValue(true);
    }

    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    private void invsort$updateButtonsBeforeExtraction(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        invsort$updateButtonPositions();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void invsort$extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (minecraft.player == null) {
            return;
        }

        InventoryScreenId screenId = InventoryScreenId.fromMenu(menu).orElse(null);
        boolean containerShouldShow = screenId != null && compatibility.shouldShowSortButton(screenId.value());

        if (invsort$SortBtn != null) {
            invsort$SortBtn.visible = containerShouldShow;
        }

        if (invsort$PlayerSortBtn != null) {
            invsort$PlayerSortBtn.visible = (screenId == null || containerShouldShow)
                    && compatibility.shouldShowSortButton(InventoryScreenId.PLAYER_INVENTORY.value());
        }

        if (screenId == null) {
            InventorySorterMod.LOGGER.debug("Unable to get screen ID for sort button visibility check");
        }
    }

    @Override
    public SortButtonWidget inventorySorter$getSortButton() {
        return invsort$SortBtn;
    }

    public SortButtonWidget inventorySorter$getPlayerSortButton() {
        return invsort$PlayerSortBtn;
    }

    @Override
    public int getMiddleHeight() {
        if (this.menu.slots.size() == 0) return 0;
        return this.menu.getSlot(this.menu.slots.size() - 36).y - 12;
    }
}
