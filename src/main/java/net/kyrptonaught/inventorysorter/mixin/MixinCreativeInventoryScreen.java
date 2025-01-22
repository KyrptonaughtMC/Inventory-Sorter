package net.kyrptonaught.inventorysorter.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.client.SortButtonWidget;
import net.kyrptonaught.inventorysorter.client.SortableContainerScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen implements SortableContainerScreen {


    @Shadow public abstract boolean isInventoryTabSelected();

    @Inject(method = "init", at = @At("TAIL"))
    private void invsort$init(CallbackInfo callbackinfo) {
        if (InventorySorterModClient.getConfig().showSortButton) {
            SortButtonWidget sortbtn = this.getSortButton();
            if (sortbtn != null)
                sortbtn.visible = this.isInventoryTabSelected();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void invsort$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (InventorySorterModClient.getConfig().showSortButton) {
            SortButtonWidget sortbtn = this.getSortButton();
            if (sortbtn != null)
                sortbtn.visible = this.isInventoryTabSelected();
        }
    }
}

