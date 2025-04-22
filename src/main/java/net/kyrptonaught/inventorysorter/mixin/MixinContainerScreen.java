package net.kyrptonaught.inventorysorter.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.client.SortButtonWidget;
import net.kyrptonaught.inventorysorter.client.SortableContainerScreen;
import net.kyrptonaught.inventorysorter.network.InventorySortPacket;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
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

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class MixinContainerScreen extends Screen implements SortableContainerScreen {
    @Shadow
    protected int backgroundWidth;
    @Shadow
    protected int backgroundHeight;

    @Shadow
    @Final
    protected ScreenHandler handler;

    @Shadow
    protected int x;
    @Shadow
    protected int y;

    @Shadow
    protected Slot focusedSlot;

    @Unique
    private SortButtonWidget invsort$SortBtn;
    @Unique
    private SortButtonWidget invsort$PlayerSortBtn;

    protected MixinContainerScreen(Text text_1) {
        super(text_1);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"))
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (client == null || client.player == null) {
            return;
        }

        if (invsort$SortBtn != null && invsort$SortBtn.isMouseOver(mouseX, mouseY)) {
            invsort$SortBtn.mouseScrolled(mouseX, mouseY, verticalAmount, horizontalAmount);
        }

        if (invsort$PlayerSortBtn != null && invsort$PlayerSortBtn.isMouseOver(mouseX, mouseY)) {
            invsort$PlayerSortBtn.mouseScrolled(mouseX, mouseY, verticalAmount, horizontalAmount);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void invsort$init(CallbackInfo callbackinfo) {
        if (client == null || client.player == null) {
            return;
        }

        if (getConfig().showSortButton && InventoryHelper.shouldDisplayButtons(client.player)) {
            boolean playerOnly = !InventoryHelper.canSortInventory(client.player);
            this.addDrawableChild(invsort$SortBtn = new SortButtonWidget(this.x + this.backgroundWidth - 20, this.y + (playerOnly ? (backgroundHeight - 95) : 6), playerOnly));
            if (!playerOnly && getConfig().separateButton)
                this.addDrawableChild(invsort$PlayerSortBtn = new SortButtonWidget(invsort$SortBtn.getX(), this.y + ((SortableContainerScreen) (this)).getMiddleHeight(), true));
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void invsort$mouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        // Keybind check for mouse bindings, client only
        if (client == null || client.player == null) {
            callbackInfoReturnable.setReturnValue(true);
            return;
        }
        if (InventorySorterModClient.isKeybindPressed(button, 0, InputUtil.Type.MOUSE)) {
            sortInventory(callbackInfoReturnable);
        }

    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void invsort$keyPressed(int keycode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        // Keybind check for key bindings, client only
        if (client == null || client.player == null)
            return;
        if (InventorySorterModClient.isKeybindPressed(keycode, scancode, InputUtil.Type.KEYSYM)) {
            sortInventory(callbackInfoReturnable);
        }
    }

    @Unique
    private void sortInventory(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        boolean playerOnlyInv = !InventoryHelper.canSortInventory(client.player);
        SortSettings settings = client.player.getAttachedOrCreate(InventorySorterMod.SORT_SETTINGS);
        if (!playerOnlyInv && settings.sortHighlightedItem()) {
            if (focusedSlot != null)
                playerOnlyInv = focusedSlot.inventory instanceof PlayerInventory;
        }
        InventorySortPacket.sendSortPacket(playerOnlyInv);
        callbackInfoReturnable.setReturnValue(true);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void invsort$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (invsort$SortBtn != null) {
            invsort$SortBtn.setX(this.x + this.backgroundWidth - 20);

            try {
                boolean shouldShow = compatibility.shouldShowSortButton(Registries.SCREEN_HANDLER.getId(client.player.currentScreenHandler.getType()));
                if (!shouldShow) {
                    invsort$SortBtn.visible = false;
                }
            } catch (UnsupportedOperationException e) {
                InventorySorterMod.LOGGER.debug("Unable to get screen ID for sort button visibility check", e);
            }
        }
    }

    @Override
    public SortButtonWidget getSortButton() {
        return invsort$SortBtn;
    }

    @Override
    public int getMiddleHeight() {
        if (this.handler.slots.size() == 0) return 0;
        return this.handler.getSlot(this.handler.slots.size() - 36).y - 12;
    }
}
