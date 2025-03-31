package net.kyrptonaught.inventorysorter.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.client.SortButtonWidget;
import net.kyrptonaught.inventorysorter.client.SortableContainerScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen implements SortableContainerScreen {


    @Shadow public abstract boolean isInventoryTabSelected();

    @Inject(method = "init", at = @At("TAIL"))
    private void invsort$init(CallbackInfo callbackinfo) {
        if (getConfig().showSortButton) {
            SortButtonWidget sortbtn = this.getSortButton();
            if (sortbtn != null)
                sortbtn.visible = this.isInventoryTabSelected();
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"))
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        SortButtonWidget sortbtn = this.getSortButton();

        if (sortbtn.visible && sortbtn.isMouseOver(mouseX, mouseY)) {
            sortbtn.mouseScrolled(mouseX, mouseY, verticalAmount, horizontalAmount);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void invsort$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (getConfig().showSortButton) {
            SortButtonWidget sortbtn = this.getSortButton();
            if (sortbtn != null)
                sortbtn.visible = this.isInventoryTabSelected();
        }
    }
}

