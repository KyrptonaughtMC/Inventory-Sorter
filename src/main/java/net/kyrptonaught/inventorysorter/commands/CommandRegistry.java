package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandRegistry {
    public static final String SORT = "sort";
    public static final String SORT_ME = "sortme";
    public static final String NO_SORT = "nosort";
    public static final String RELOAD = "reload";
    public static final String SCREEN_ID = "screenid";
    public static final String DOUBLE_CLICK_SORT = "doubleclicksort";
    public static final String SORT_HIGHLIGHTED_INVENTORY = "sorthighlightedinventory";
    public static final String SORT_INTO_BUNDLES = "sortintobundles";
    public static final String SORT_PLAYER_INVENTORY = "sortplayerinventory";
    public static final String PRIORITY = "priority";
    public static final String SORT_TYPE = "sorttype";

    public static final String ADMIN = "admin";
    public static final String ADMIN_RELOAD = "admin.reload";
    public static final String ADMIN_NOSORT = "admin.nosort";
    public static final String ADMIN_NOSORT_ADD = "admin.nosort.add";
    public static final String ADMIN_NOSORT_REMOVE = "admin.nosort.remove";
    public static final String ADMIN_NOSORT_LIST = "admin.nosort.list";
    public static final String ADMIN_HIDEBUTTON = "admin.hidebutton";
    public static final String ADMIN_HIDEBUTTON_ADD = "admin.hidebutton.add";
    public static final String ADMIN_HIDEBUTTON_REMOVE = "admin.hidebutton.remove";
    public static final String ADMIN_HIDEBUTTON_LIST = "admin.hidebutton.list";
    public static final String ADMIN_REMOTE = "admin.remote";
    public static final String ADMIN_REMOTE_SET = "admin.remote.set";
    public static final String ADMIN_REMOTE_CLEAR = "admin.remote.clear";
    public static final String ADMIN_REMOTE_SHOW = "admin.remote.show";

    public static final String[] ADMIN_CHILD_PERMISSIONS = {
            ADMIN_RELOAD,
            ADMIN_NOSORT,
            ADMIN_NOSORT_ADD,
            ADMIN_NOSORT_REMOVE,
            ADMIN_NOSORT_LIST,
            ADMIN_HIDEBUTTON,
            ADMIN_HIDEBUTTON_ADD,
            ADMIN_HIDEBUTTON_REMOVE,
            ADMIN_HIDEBUTTON_LIST,
            ADMIN_REMOTE,
            ADMIN_REMOTE_SET,
            ADMIN_REMOTE_CLEAR,
            ADMIN_REMOTE_SHOW
    };

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext commandRegistryAccess,
            Commands.CommandSelection registrationEnvironment
    ) {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("invsort");

        SortCommand.register(dispatcher, rootCommand);
        DoubleClickSortCommand.register(dispatcher, rootCommand);
        SortPlayerInventoryCommand.register(dispatcher, rootCommand);
        SortHighlightedInventoryCommand.register(dispatcher, rootCommand);
        SortIntoBundlesCommand.register(dispatcher, rootCommand);
        SortMeCommand.register(dispatcher, rootCommand);
        SortTypeCommand.register(dispatcher, rootCommand);
        SortPriorityRulesCommand.register(dispatcher, rootCommand);
        NoSortCommand.register(dispatcher, rootCommand);
        ReloadCommand.register(dispatcher, rootCommand);
        ScreenIDCommand.register(dispatcher, rootCommand);


        if (/*? if fabric {*/registrationEnvironment.includeDedicated/*?} else {*//*registrationEnvironment.equals(Commands.CommandSelection.DEDICATED) || registrationEnvironment.equals(Commands.CommandSelection.ALL)*//*?}*/) {
            AdminCommands.register(dispatcher, rootCommand);
        }
    }
}
