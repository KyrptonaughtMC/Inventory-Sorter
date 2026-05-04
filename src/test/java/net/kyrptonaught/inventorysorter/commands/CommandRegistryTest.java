package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.kyrptonaught.inventorysorter.SortType;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.flag.FeatureFlags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandRegistryTest {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void registerAddsCommonInventorySorterCommands() {
        CommandNode<CommandSourceStack> invsort = registeredInvsortCommand(Commands.CommandSelection.INTEGRATED);

        assertHasChildren(invsort,
                "sort",
                "doubleClickSort",
                "sortPlayerInventory",
                "sortHighlightedInventory",
                "sortme",
                "sortType",
                "nosort",
                "reload",
                "screenID"
        );
        Assertions.assertNull(invsort.getChild("admin"));
    }

    @Test
    public void standaloneCommandsAreExecutable() {
        CommandNode<CommandSourceStack> invsort = registeredInvsortCommand(Commands.CommandSelection.INTEGRATED);

        assertExecutable(invsort.getChild("sort"));
        assertExecutable(invsort.getChild("sortme"));
        assertExecutable(invsort.getChild("reload"));
        assertExecutable(invsort.getChild("screenID"));
    }

    @Test
    public void toggleCommandsExposeShowOnAndOffActions() {
        CommandNode<CommandSourceStack> invsort = registeredInvsortCommand(Commands.CommandSelection.INTEGRATED);

        assertToggleCommand(invsort.getChild("doubleClickSort"));
        assertToggleCommand(invsort.getChild("sortPlayerInventory"));
        assertToggleCommand(invsort.getChild("sortHighlightedInventory"));
    }

    @Test
    public void playerNoSortCommandExposesAddRemoveAndListActions() {
        CommandNode<CommandSourceStack> nosort = registeredInvsortCommand(Commands.CommandSelection.INTEGRATED)
                .getChild("nosort");

        assertHasChildren(nosort, "add", "remove", "list");
        assertExecutable(nosort.getChild("add"));
        assertExecutable(nosort.getChild("remove"));
        assertExecutable(nosort.getChild("list"));
    }

    @Test
    public void sortTypeCommandIncludesEverySortType() {
        CommandNode<CommandSourceStack> sortType = registeredInvsortCommand(Commands.CommandSelection.INTEGRATED)
                .getChild("sortType");

        assertHasChildren(sortType, Arrays.stream(SortType.values())
                .map(SortType::name)
                .toArray(String[]::new));
    }

    @Test
    public void sortTypeCommandOnlyRegistersUppercaseEnumNames() {
        CommandNode<CommandSourceStack> sortType = registeredInvsortCommand(Commands.CommandSelection.INTEGRATED)
                .getChild("sortType");

        Assertions.assertNotNull(sortType.getChild("NAME"));
        Assertions.assertNull(sortType.getChild("name"));
    }

    @Test
    public void dedicatedRegistrationIncludesAdminCommands() {
        CommandNode<CommandSourceStack> admin = registeredInvsortCommand(Commands.CommandSelection.DEDICATED)
                .getChild("admin");

        Assertions.assertNotNull(admin);
        assertHasChildren(admin, "nosort", "hidebutton", "reload", "remote");
    }

    @Test
    public void adminCommandsExposeExpectedActions() {
        CommandNode<CommandSourceStack> admin = registeredInvsortCommand(Commands.CommandSelection.DEDICATED)
                .getChild("admin");

        assertHasChildren(admin.getChild("nosort"), "add", "remove", "list");
        assertHasChildren(admin.getChild("hidebutton"), "add", "remove", "list");
        assertExecutable(admin.getChild("reload"));
        assertHasChildren(admin.getChild("remote"), "set", "clear", "show");
        assertExecutable(admin.getChild("remote").getChild("clear"));
        assertExecutable(admin.getChild("remote").getChild("show"));
    }

    @Test
    public void adminRemoteSetAcceptsOneQuotedStringArgument() {
        CommandNode<CommandSourceStack> set = registeredInvsortCommand(Commands.CommandSelection.DEDICATED)
                .getChild("admin")
                .getChild("remote")
                .getChild("set");

        CommandNode<CommandSourceStack> url = set.getChild("url");

        Assertions.assertInstanceOf(ArgumentCommandNode.class, url);
        StringArgumentType argumentType = (StringArgumentType) ((ArgumentCommandNode<?, ?>) url).getType();
        Assertions.assertEquals(StringArgumentType.StringType.QUOTABLE_PHRASE, argumentType.getType());
        assertExecutable(url);
    }

    @Test
    public void allSelectionAlsoIncludesDedicatedAdminCommands() {
        CommandNode<CommandSourceStack> invsort = registeredInvsortCommand(Commands.CommandSelection.ALL);

        Assertions.assertNotNull(invsort.getChild("admin"));
    }

    private static CommandNode<CommandSourceStack> registeredInvsortCommand(Commands.CommandSelection selection) {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        CommandBuildContext buildContext = CommandBuildContext.simple(RegistryAccess.EMPTY, FeatureFlags.DEFAULT_FLAGS);

        CommandRegistry.register(dispatcher, buildContext, selection);

        CommandNode<CommandSourceStack> invsort = dispatcher.getRoot().getChild("invsort");
        Assertions.assertNotNull(invsort);
        return invsort;
    }

    private static void assertToggleCommand(CommandNode<CommandSourceStack> command) {
        assertExecutable(command);
        assertHasChildren(command, "on", "off");
        assertExecutable(command.getChild("on"));
        assertExecutable(command.getChild("off"));
    }

    private static void assertExecutable(CommandNode<CommandSourceStack> command) {
        Assertions.assertNotNull(command);
        Assertions.assertNotNull(command.getCommand(), () -> command.getName() + " should be executable");
    }

    private static void assertHasChildren(CommandNode<CommandSourceStack> command, String... expectedChildren) {
        Set<String> actualChildren = command.getChildren().stream()
                .map(CommandNode::getName)
                .collect(Collectors.toSet());

        Assertions.assertTrue(actualChildren.containsAll(Set.of(expectedChildren)),
                () -> "Expected children " + Set.of(expectedChildren) + " but found " + actualChildren);
    }
}
