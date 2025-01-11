package net.kyrptonaught.inventorysorter.client.config;

import blue.endless.jankson.Comment;
import com.google.common.collect.Sets;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.util.HashSet;

public class IgnoreList implements AbstractConfigFile {
    public static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/kyrptonaught/Inventory-Sorter/1.20.5/DownloadableBlacklist.json5";

    @Comment("URL for blacklist to be downloaded from")
    public String blacklistDownloadURL = DOWNLOAD_URL;

    public void downloadList() {
       downloadList(blacklistDownloadURL);
    }

    public void downloadList(String URL) {
        try {
            URL url = new URL(URL);
            String downloaded = IOUtils.toString(url.openStream());
            IgnoreList newList = InventorySorterMod.configManager.getJANKSON().fromJson(downloaded, IgnoreList.class);

            doNotSortList.addAll(newList.doNotSortList);
            hideSortBtnsList.addAll(newList.hideSortBtnsList);
            InventorySorterMod.configManager.save("blacklist");
            SystemToast.add(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("key.inventorysorter.toast.pass"), null);
        } catch (Exception e) {
            SystemToast.add(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("key.inventorysorter.toast.error"), Text.translatable("key.inventorysorter.toast.error2"));
            e.printStackTrace();
        }
    }

    @Comment("The opened inventory's sort button will not be visible, only the player's when this inventory is opened")
    public HashSet<String> doNotSortList = new HashSet<>();

    @Comment("Neither the opened inventory, nor your player inventory will be sortable when this inventory is opened")
    public HashSet<String> hideSortBtnsList = new HashSet<>();

    public transient HashSet<Identifier> defaultHideSortBtnsList = Sets.newHashSet(
            Identifier.of("guild:quest_screen"),
            Identifier.of("ae2:crystal_growth"),
            Identifier.of("ae2:advanced_inscriber"),
            Identifier.of("ae2:item_terminal"),
            Identifier.of("ae2:drive"),
            Identifier.of("ae2:patternterm"),
            Identifier.of("ae2:craftingterm"),
            Identifier.of("dankstorage:portable_dank_1"),
            Identifier.of("dankstorage:portable_dank_2"),
            Identifier.of("dankstorage:portable_dank_3"),
            Identifier.of("dankstorage:portable_dank_4"),
            Identifier.of("dankstorage:portable_dank_5"),
            Identifier.of("dankstorage:portable_dank_6"),
            Identifier.of("dankstorage:portable_dank_7")
    );

    public transient HashSet<Identifier> defaultDoNotSortList = Sets.newHashSet(
            Registries.SCREEN_HANDLER.getId(ScreenHandlerType.CRAFTING),
            Identifier.of("adorn:trading_station"),
            Identifier.of("guild:quest_screen"),
            Identifier.of("conjuring:soulfire_forge"),
            Identifier.of("spectrum:pedestal"),
            Identifier.of("bankstorage:bank_1"),
            Identifier.of("bankstorage:bank_2"),
            Identifier.of("bankstorage:bank_3"),
            Identifier.of("bankstorage:bank_4"),
            Identifier.of("bankstorage:bank_5"),
            Identifier.of("bankstorage:bank_6"),
            Identifier.of("bankstorage:bank_7")
    );

    public boolean isSortBlackListed(Identifier screenHandlerTypeID) {
        return isDisplayBlacklisted(screenHandlerTypeID) || doNotSortList.contains(screenHandlerTypeID.toString()) || defaultDoNotSortList.contains(screenHandlerTypeID);
    }

    public boolean isDisplayBlacklisted(Identifier screenHandlerTypeID) {
        return defaultHideSortBtnsList.contains(screenHandlerTypeID) || hideSortBtnsList.contains(screenHandlerTypeID.toString());
    }
}
