package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.HideButton;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class AdminCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        LiteralArgumentBuilder<CommandSourceStack> admin = Commands.literal("admin")
                /*
                    To avoid having to give the root admin permission to access just a single command.
                    The alternative would be to either have the admin command show up for people who don't have
                    a single permission or to have to not have granular permissions for the admin commands.

                    This way, the admin command still shows up for people who only have a single admin command
                    permission, but they can't access the other commands.
                 */
                .requires(CommandPermission.hasAny(
                                        "admin.reload",
                                        "admin.nosort",
                                        "admin.nosort.add",
                                        "admin.nosort.remove",
                                        "admin.nosort.list",
                                        "admin.hidebutton",
                                        "admin.hidebutton.add",
                                        "admin.hidebutton.remove",
                                        "admin.hidebutton.list",
                                        "admin.remote",
                                        "admin.remote.set",
                                        "admin.remote.clear",
                                        "admin.remote.show"

                                )
                                .or(CommandPermission.require("admin", 2))
                );

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("nosort")
                        .then(Commands.literal("add")
                                .requires(CommandPermission.require("admin.nosort.add", 2))
                                .executes(AdminCommands::nosortAdd))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("nosort")
                        .then(Commands.literal("remove")
                                .requires(CommandPermission.require("admin.nosort.remove", 2))
                                .executes(AdminCommands::nosortRemove))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("nosort")
                        .then(Commands.literal("list")
                                .requires(CommandPermission.require("admin.nosort.list", 2))
                                .executes(AdminCommands::nosortList))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("hidebutton")
                        .then(Commands.literal("add")
                                .requires(CommandPermission.require("admin.hidebutton.add", 2))
                                .executes(AdminCommands::hidebuttonAdd))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("hidebutton")
                        .then(Commands.literal("remove")
                                .requires(CommandPermission.require("admin.hidebutton.remove", 2))
                                .executes(AdminCommands::hidebuttonRemove))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("hidebutton")
                        .then(Commands.literal("list")
                                .requires(CommandPermission.require("admin.hidebutton.list", 2))
                                .executes(AdminCommands::hidebuttonList))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("reload")
                        .requires(CommandPermission.require("admin.reload", 2))
                        .executes(AdminCommands::reload))
        ));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("remote")
                        .then(Commands.literal("set")
                                .then(Commands.argument("url", StringArgumentType.string())
                                        .requires(CommandPermission.require("admin.remote.set", 2))
                                        .executes(AdminCommands::remoteSet))
                        ))));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("remote")
                        .then(Commands.literal("clear")
                                .requires(CommandPermission.require("admin.remote.clear", 2))
                                .executes(AdminCommands::remoteClear))
        )));

        dispatcher.register(rootCommand.then(admin.then(
                Commands.literal("remote")
                        .then(Commands.literal("show")
                                .requires(CommandPermission.require("admin.remote.show", 2))
                                .executes(AdminCommands::remoteShow))
        )));

    }

    public static int nosortAdd(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.disableSortForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.add.fail"), false);
            return 0;
        }

        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.add.success"), false);
        return 1;
    }

    public static int nosortRemove(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.enableSortForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.remove.fail"), false);
            return 0;
        }

        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.remove.success"), false);
        return 1;
    }

    public static int nosortList(CommandContext<CommandSourceStack> commandContext) {
        NewConfigOptions config = getConfig();

        commandContext.getSource().sendSuccess(() -> Component.translatable(
                "inventorysorter.cmd.nosort.list",
                String.join(",", config.preventSortForScreens)
        ), false);
        return 1;

    }

    public static int hidebuttonAdd(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.disableButtonForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.hideButton.add.fail"), false);
            return 0;
        }

        HideButton.fromConfig(getConfig()).sync(commandContext.getSource().getServer());
        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.hideButton.add.success"), false);
        return 1;
    }

    public static int hidebuttonRemove(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            NewConfigOptions config = getConfig();
            config.enableButtonForScreen(context.screenId().toString());
            config.save();
            compatibility.reload();
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.hideButton.remove.fail"), false);
            return 0;
        }

        HideButton.fromConfig(getConfig()).sync(commandContext.getSource().getServer());
        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.hideButton.remove.success"), false);
        return 1;
    }

    public static int hidebuttonList(CommandContext<CommandSourceStack> commandContext) {
        NewConfigOptions config = getConfig();

        commandContext.getSource().sendSuccess(() -> Component.translatable(
                "inventorysorter.cmd.hideButton.list",
                String.join(",", config.hideButtonsForScreens)
        ), false);

        return 1;
    }

    public static int remoteSet(CommandContext<CommandSourceStack> commandContext) {
        String url = StringArgumentType.getString(commandContext, "url");
        NewConfigOptions config = getConfig();
        config.customCompatibilityListDownloadUrl = url;
        config.save();
        reloadConfig();
        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.remote.set.success", url), false);
        return 1;
    }

    public static int remoteClear(CommandContext<CommandSourceStack> commandContext) {
        NewConfigOptions config = getConfig();
        config.customCompatibilityListDownloadUrl = "";
        config.save();
        reloadConfig();
        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.remote.clear.success"), false);
        return 1;
    }

    public static int remoteShow(CommandContext<CommandSourceStack> commandContext) {
        NewConfigOptions config = getConfig();
        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.remote.show", config.customCompatibilityListDownloadUrl), false);
        return 1;
    }

    public static int reload(CommandContext<CommandSourceStack> commandContext) {
        reloadConfig();
        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.reload.success"), false);
        return 1;
    }
}
