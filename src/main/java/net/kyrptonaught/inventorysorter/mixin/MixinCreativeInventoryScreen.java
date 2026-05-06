package net.kyrptonaught.inventorysorter.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.client.SortButtonWidget;
import net.kyrptonaught.inventorysorter.client.SortableContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

@Environment(EnvType.CLIENT)
@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen implements SortableContainerScreen {


    @Shadow
    public abstract boolean isInventoryOpen();

    @Inject(method = "init", at = @At("TAIL"))
    private void invsort$init(CallbackInfo callbackinfo) {
        if (getConfig().showSortButton) {
            SortButtonWidget sortbtn = this.inventorySorter$getPlayerSortButton();
            if (sortbtn != null) {
                sortbtn.visible = this.isInventoryOpen();
            }
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void invsort$extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (getConfig().showSortButton) {
            SortButtonWidget sortbtn = this.inventorySorter$getPlayerSortButton();
            if (sortbtn != null) {
                sortbtn.visible = this.isInventoryOpen();
            }
        }
    }
}

