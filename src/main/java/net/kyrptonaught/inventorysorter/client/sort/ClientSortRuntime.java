package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.client.ClientServerSupport;
import net.kyrptonaught.inventorysorter.network.InventorySortPacket;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Stateful client sorting pipeline for one client runtime.
 *
 * Keeps server-presence state, local click execution, and sort request routing together so they
 * evolve as one feature. Loader callbacks are registered elsewhere; UI and mixin code use
 * {@link ClientSorts} as the narrow global bridge.
 */
public final class ClientSortRuntime {
    private final ClientServerSupport serverSupport;
    private final ClientInventoryClickExecutor clickExecutor;
    private final ClientSortRequests sortRequests;

    private ClientSortRuntime(
            ClientServerSupport serverSupport,
            ClientInventoryClickExecutor clickExecutor,
            ClientSortRequests sortRequests
    ) {
        this.serverSupport = serverSupport;
        this.clickExecutor = clickExecutor;
        this.sortRequests = sortRequests;
    }

    public static ClientSortRuntime create(
            Supplier<Minecraft> minecraft,
            Supplier<String> languageCode,
            Supplier<SortType> sortType,
            Supplier<List<SortPriorityRuleSetting>> sortPriorityRules,
            BooleanSupplier sortPlayerInventory
    ) {
        ClientServerSupport serverSupport = new ClientServerSupport();
        ClientInventoryClickExecutor clickExecutor = new ClientInventoryClickExecutor();
        ClientSideInventorySorter fallbackSorter = new ClientSideInventorySorter(
                minecraft,
                languageCode,
                sortType,
                sortPriorityRules,
                sortPlayerInventory,
                clickExecutor,
                new ClientSortClickPlanner()
        );
        ClientSortRequests sortRequests = new ClientSortRequests(
                serverSupport,
                InventorySortPacket::sendSortPacket,
                fallbackSorter::enqueueCurrentScreenSort
        );
        return new ClientSortRuntime(serverSupport, clickExecutor, sortRequests);
    }

    public ClientServerSupport serverSupport() {
        return serverSupport;
    }

    public void clearPendingClicks() {
        clickExecutor.clear();
    }

    public void tickClickExecutor(Minecraft minecraft) {
        clickExecutor.tick(minecraft);
    }

    public boolean requestCurrentScreenSort(SortTarget target) {
        return sortRequests.requestSort(target);
    }
}
