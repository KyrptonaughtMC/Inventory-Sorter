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
import net.kyrptonaught.inventorysorter.compat.sources.*;
import net.kyrptonaught.inventorysorter.config.Config;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class InventorySorterMod implements ModInitializer {
    public static final String MOD_ID = "inventorysorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = "VERSION_REPL";

    private static NewConfigOptions CONFIG = Config.load();
    public static final Compatibility compatibility = new Compatibility(
            new ArrayList<>(List.of(
                    new PredefinedLoader(),
                    new LocalLoader(),
                    new OfficialListLoader(),
                    new ConfigLoader(InventorySorterMod::getConfig),
                    new RemoteConfigLoader(() -> InventorySorterMod.getConfig().customCompatibilityListDownloadUrl)
            ))
    );

    public static NewConfigOptions getConfig() {
        return CONFIG;
    }

    public static void reloadConfig() {
        CONFIG = Config.load();
        compatibility.reload();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<SortSettings> SORT_SETTINGS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "sort_settings"),
            builder -> builder
                    .initializer(() -> SortSettings.DEFAULT)
                    .persistent(SortSettings.NBT_CODEC)
                    .copyOnDeath()
    );

    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<PlayerSortPrevention> PLAYER_SORT_PREVENTION = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "player_sort_prevention"),
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
            Identifier.fromNamespaceAndPath(MOD_ID, "client_sync"),
            builder -> builder
                    .persistent(ClientSync.NBT_CODEC)
                    .copyOnDeath()
    );

    @SuppressWarnings("UnstableApiUsage")
    public static final AttachmentType<LastSeenVersionPacket> LAST_SEEN_VERSION = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "last_seen_version"),
            builder -> builder
                    .persistent(LastSeenVersionPacket.NBT_CODEC)
    );

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandRegistry::register);

        PayloadTypeRegistry.serverboundPlay().register(ClientSync.ID, ClientSync.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(LastSeenVersionPacket.ID, LastSeenVersionPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerPresencePacket.ID, ServerPresencePacket.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(HideButton.ID, HideButton.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReloadConfigPacket.ID, ReloadConfigPacket.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(PlayerSortPrevention.ID, PlayerSortPrevention.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerSortPrevention.ID, PlayerSortPrevention.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(SortSettings.ID, SortSettings.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SortSettings.ID, SortSettings.CODEC);

        InventorySortPacket.registerReceivePacket();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var context = new CreativeModeTab.ItemDisplayParameters(server.getWorldData().enabledFeatures(), false, server.registryAccess());
            CreativeModeTabs.allTabs().forEach(group -> {
                if (group.getSearchTabDisplayItems().isEmpty()) group.buildContents(context);
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, server, client) -> {
            ServerPlayer player = handler.getPlayer();
            ServerPlayNetworking.send(player, new ServerPresencePacket());
            if(!player.hasAttached(LAST_SEEN_VERSION)) {
                LastSeenVersionPacket.DEFAULT.send(player);
            } else {
                Objects.requireNonNull(player.getAttached(LAST_SEEN_VERSION)).send(player);
            }

            player.setAttached(LAST_SEEN_VERSION, new LastSeenVersionPacket(VERSION, player.clientInformation().language().toLowerCase()));

            if (client.isDedicatedServer()) {
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
            }
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
