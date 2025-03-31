package net.kyrptonaught.inventorysorter.mixin;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.interfaces.InvSorterPlayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements InvSorterPlayer {
    private static final String saveKEY = InventorySorterMod.MOD_ID + "invsorter";
    private static final String sortTypeKey = "sorttype";
    private static final String middleClickKey = "middleclick";
    private static final String doubleClickKey = "middleclick";
    private SortCases.SortType sortType = SortCases.SortType.NAME;
    private boolean middleClick = true;
    private boolean doubleClick = true;

    @Override
    public SortCases.SortType getSortType() {
        return sortType;
    }

    @Override
    public void setSortType(SortCases.SortType sortType) {
        this.sortType = sortType;
    }

    @Override
    public boolean getMiddleClick() {
        return middleClick;
    }

    @Override
    public void setMiddleClick(boolean middleClick) {
        this.middleClick = middleClick;
    }

    @Override
    public boolean getDoubleClickSort() {
        return doubleClick;
    }

    @Override
    public void setDoubleClickSort(boolean doubleClick) {
        this.doubleClick = doubleClick;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeSortType(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound invSortNbt = new NbtCompound();
        invSortNbt.putInt(sortTypeKey, sortType.ordinal());
        invSortNbt.putBoolean(middleClickKey, middleClick);
        invSortNbt.putBoolean(doubleClickKey, doubleClick);
        nbt.put(saveKEY, invSortNbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readSortType(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(saveKEY)) {
            /*? if >=1.21.5 {*/
            NbtCompound invSortNbt = nbt.getCompoundOrEmpty(saveKEY);
            if (invSortNbt.contains(sortTypeKey)) sortType = SortCases.SortType.values()[invSortNbt.getInt(sortTypeKey, SortCases.SortType.NAME.ordinal())];
            if (invSortNbt.contains(middleClickKey)) middleClick = invSortNbt.getBoolean(middleClickKey, true);
            if (invSortNbt.contains(doubleClickKey)) doubleClick = invSortNbt.getBoolean(doubleClickKey, true);
            /*?} else {*/
            /*NbtCompound invSortNbt = nbt.getCompound(saveKEY);
            if (invSortNbt.contains(sortTypeKey)) sortType = SortCases.SortType.values()[invSortNbt.getInt(sortTypeKey)];
            if (invSortNbt.contains(middleClickKey)) middleClick = invSortNbt.getBoolean(middleClickKey);
            if (invSortNbt.contains(doubleClickKey)) doubleClick = invSortNbt.getBoolean(doubleClickKey);
            *//*?}*/
        }
    }
}
