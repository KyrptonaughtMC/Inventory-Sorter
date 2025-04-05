package net.kyrptonaught.inventorysorter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.commands.CommandRegistry;
import net.kyrptonaught.inventorysorter.compat.Compatibility;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.compat.sources.LocalLoader;
import net.kyrptonaught.inventorysorter.compat.sources.OfficialListLoader;
import net.kyrptonaught.inventorysorter.config.Config;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class InventorySorterMod implements ModInitializer {
    public static final String MOD_ID = "inventorysorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final NewConfigOptions CONFIG = Config.load();
    public static final Compatibility compatibility = new Compatibility(
            new ArrayList<>(List.of(
                    new LocalLoader(),
                    new OfficialListLoader(),
                    new ConfigLoader(CONFIG)
            ))
    );

    public static NewConfigOptions getConfig() {
        return CONFIG;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<SortSettings> SORT_SETTINGS = AttachmentRegistry.create(
            Identifier.of(MOD_ID, "sort_settings"),
            builder -> builder
                    .initializer(() -> SortSettings.DEFAULT)
                    .persistent(SortSettings.NBT_CODEC)
                    .copyOnDeath()
    );

    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<PlayerSortPrevention> PLAYER_SORT_PREVENTION = AttachmentRegistry.create(
            Identifier.of(MOD_ID, "player_sort_prevention"),
            builder -> builder
                    .initializer(() -> PlayerSortPrevention.DEFAULT)
                    .persistent(PlayerSortPrevention.NBT_CODEC)
                    .copyOnDeath()
    );

    /**
     * This attachment is used to tell if the user has used a modded client before. Helps us with figuring
     * out if we need to send configuration to the client or accept the client's settings on the server instead.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<ClientSync> CLIENT_SYNC = AttachmentRegistry.create(
            Identifier.of(MOD_ID, "client_sync"),
            builder -> builder
                    .initializer(() -> ClientSync.DEFAULT)
                    .persistent(ClientSync.NBT_CODEC)
                    .copyOnDeath()
    );

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandRegistry::register);

        PayloadTypeRegistry.playC2S().register(ClientSync.ID, ClientSync.CODEC);


        PayloadTypeRegistry.playS2C().register(HideButton.ID, HideButton.CODEC);

        PayloadTypeRegistry.playC2S().register(PlayerSortPrevention.ID, PlayerSortPrevention.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerSortPrevention.ID, PlayerSortPrevention.CODEC);

        PayloadTypeRegistry.playC2S().register(SortSettings.ID, SortSettings.CODEC);
        PayloadTypeRegistry.playS2C().register(SortSettings.ID, SortSettings.CODEC);

        InventorySortPacket.registerReceivePacket();
        SyncBlacklistPacket.registerSyncOnPlayerJoin();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var context = new ItemGroup.DisplayContext(server.getSaveProperties().getEnabledFeatures(), false, server.getRegistryManager());
            ItemGroups.getGroups().forEach(group -> {
                if (group.getSearchTabStacks().isEmpty()) group.updateEntries(context);
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, server, client) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!player.hasAttached(SORT_SETTINGS)) {
                player.setAttached(SORT_SETTINGS, SortSettings.DEFAULT);
            }

            if (!player.hasAttached(PLAYER_SORT_PREVENTION)) {
                player.setAttached(PLAYER_SORT_PREVENTION, PlayerSortPrevention.DEFAULT);
            }

            if (!player.hasAttached(CLIENT_SYNC)) {
                player.setAttached(CLIENT_SYNC, ClientSync.DEFAULT);
            }

            if (!player.getAttached(CLIENT_SYNC).seenClient()) {
                /*
                  If we haven't seen the client MOD before, we need to send the config we have for the player.
                  This is for the case when a player hasn't used the mod on the client before but has settings stored
                  for them on the server.

                  When the client connects for the first time, we send them the config we have for them.
                 */
                PlayerSortPrevention sortPrevention = player.getAttached(PLAYER_SORT_PREVENTION);
                if (sortPrevention != PlayerSortPrevention.DEFAULT) {
                    sortPrevention.sync(player);
                }

                SortSettings sortSettings = player.getAttached(SORT_SETTINGS);
                if (sortSettings != SortSettings.DEFAULT) {
                    sortSettings.sync(player);
                }
            }

            HideButton.fromConfig(getConfig()).sync(player);
        });

        ServerPlayNetworking.registerGlobalReceiver(SortSettings.ID, (payload, context) -> {
            context.player().setAttached(SORT_SETTINGS, payload);
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerSortPrevention.ID, (payload, context) -> {
            context.player().setAttached(PLAYER_SORT_PREVENTION, payload);
        });

        ServerPlayNetworking.registerGlobalReceiver(ClientSync.ID, (payload, context) -> {
            context.player().setAttached(CLIENT_SYNC, new ClientSync(true));
        });
    }
}
