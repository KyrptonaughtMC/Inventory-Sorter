package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.HideButton;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

public class AdminCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        LiteralArgumentBuilder<ServerCommandSource> admin = CommandManager.literal("admin")
                /*
                    To avoid having to give the root admin permission to access just a single command.
                    The alternative would be to either have the admin command show up for people who don't have
                    a single permission or to have to not have granular permissions for the admin commands.

                    This way, the admin command still shows up for people who only have a single admin command
                    permission, but they can't access the other commands.
                 */
                .requires(CommandPermission.hasAny(
                                "admin.nosort",
                                "admin.nosort.add",
                                "admin.nosort.remove",
                                "admin.nosort.list",
                                "admin.hidebutton",
                                "admin.hidebutton.add",
                                "admin.hidebutton.remove",
                                "admin.hidebutton.list"
                        )
                        .or(CommandPermission.require("admin", 2))
                );


        dispatcher.register(rootCommand.then(admin.then(
                CommandManager.literal("nosort")
                        .then(CommandManager.literal("add")
                                .requires(CommandPermission.require("admin.nosort.add", 2))
                                .executes(AdminCommands::nosortAdd))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                CommandManager.literal("nosort")
                        .then(CommandManager.literal("remove")
                                .requires(CommandPermission.require("admin.nosort.remove", 2))
                                .executes(AdminCommands::nosortRemove))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                CommandManager.literal("nosort")
                        .then(CommandManager.literal("list")
                                .requires(CommandPermission.require("admin.nosort.list", 2))
                                .executes(AdminCommands::nosortList))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                CommandManager.literal("hidebutton")
                        .then(CommandManager.literal("add")
                                .requires(CommandPermission.require("admin.hidebutton.add", 2))
                                .executes(AdminCommands::hidebuttonAdd))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                CommandManager.literal("hidebutton")
                        .then(CommandManager.literal("remove")
                                .requires(CommandPermission.require("admin.hidebutton.remove", 2))
                                .executes(AdminCommands::hidebuttonRemove))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                CommandManager.literal("hidebutton")
                        .then(CommandManager.literal("list")
                                .requires(CommandPermission.require("admin.hidebutton.list", 2))
                                .executes(AdminCommands::hidebuttonList))
        )));

    }

    public static int nosortAdd(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();

        InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.disableSortForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        commandContext.getSource().sendFeedback(() -> Text.of("Screen added"), false);
        return 1;
    }

    public static int nosortRemove(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();

        InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.enableSortForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        commandContext.getSource().sendFeedback(() -> Text.of("Screen removed"), false);
        return 1;
    }

    public static int nosortList(CommandContext<ServerCommandSource> commandContext) {
        NewConfigOptions config = getConfig();

        StringBuilder sb = new StringBuilder("Prevented screens: ");
        for (String screen : config.preventSortForScreens) {
            sb.append(screen).append(", ");
        }
        commandContext.getSource().sendFeedback(() -> Text.of(sb.toString()), false);
        return 1;

    }

    public static int hidebuttonAdd(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.disableButtonForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendFeedback(() -> Text.of("Invalid target container"), false);
            return 0;
        }

        HideButton.fromConfig(getConfig()).sync(commandContext.getSource().getServer());
        commandContext.getSource().sendFeedback(() -> Text.of("Button hidden"), false);
        return 1;
    }

    public static int hidebuttonRemove(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.enableButtonForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendFeedback(() -> Text.of("Invalid target container"), false);
            return 0;
        }

        HideButton.fromConfig(getConfig()).sync(commandContext.getSource().getServer());
        commandContext.getSource().sendFeedback(() -> Text.of("Button removed"), false);
        return 1;
    }

    public static int hidebuttonList(CommandContext<ServerCommandSource> commandContext) {
        NewConfigOptions config = getConfig();

        StringBuilder sb = new StringBuilder("Hidden buttons: ");
        for (String screen : config.hideButtonsForScreens) {
            sb.append(screen).append(", ");
        }
        commandContext.getSource().sendFeedback(() -> Text.of(sb.toString()), false);
        return 1;
    }
}
