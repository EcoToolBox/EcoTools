package org.kaiaccount.account.eco.commands.balance;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.permission.Permissions;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.ParseCommandArgument;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.util.List;
import java.util.Optional;

public class CheckPlayerBalanceCommand implements ArgumentCommand {

    public static final CommandArgument<String> PLAYER = new ExactArgument("player");
    public static final CommandArgument<OfflinePlayer> USER;
    private static final CommandArgument<OfflinePlayer> PERMISSION_USER = new PermissionOrArgument<>(
            "user",
            (sender) -> sender.hasPermission(Permissions.BALANCE_OTHER.getPermissionNode()),
            new UserArgument("user", (user) -> true));

    static {
        var elseArg = new ParseCommandArgument<OfflinePlayer>() {
            @Override
            public @NotNull CommandArgumentResult<OfflinePlayer> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument) throws ArgumentException {
                if (context.getSource() instanceof OfflinePlayer player) {
                    return CommandArgumentResult.from(argument, 0, player);
                }
                throw new ArgumentException("A player is needed");
            }
        };
        USER = new OptionalArgument<>(PERMISSION_USER, elseArg);
    }

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(PLAYER, USER);
    }

    @Override
    public @NotNull String getDescription() {
        return "Checks the balance of either yourself or another player";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.BALANCE_SELF.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        OfflinePlayer player = commandContext.getArgument(this, USER);
        if (player == null) {
            if (!(commandContext.getSource() instanceof OfflinePlayer)) {
                commandContext.getSource().sendMessage("You are required to specify a player");
                return false;
            }
            player = (OfflinePlayer) commandContext.getSource();
        }
        return CheckBalanceCommand.displayInfo(commandContext.getSource(),
                AccountInterface.getManager().getPlayerAccount(player));
    }
}
