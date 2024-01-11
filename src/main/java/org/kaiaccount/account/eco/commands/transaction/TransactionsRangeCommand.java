package org.kaiaccount.account.eco.commands.transaction;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.account.EcoAccount;
import org.kaiaccount.account.eco.account.history.EntryTransactionHistory;
import org.kaiaccount.account.eco.commands.argument.date.DateTimeArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.context.CommandContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TransactionsRangeCommand implements ArgumentCommand {

    private final CommandArgument<OfflinePlayer> player = new OptionalArgument<OfflinePlayer>(
            new PermissionOrArgument<>(
                    "user",
                    source -> source.hasPermission(Permissions.HISTORY_OTHER.getPermissionNode()),
                    new UserArgument("user", u -> true)),
            (cmdContext, argContext) -> {
                if (cmdContext.getSource() instanceof OfflinePlayer user) {
                    return CommandArgumentResult.from(argContext, 0, user);
                }
                throw new IOException("Player needs to be specified");
            });
    private final CommandArgument<LocalDateTime> startDate = new DateTimeArgument("start", (cmdContext, argContext) -> {
        OfflinePlayer user = cmdContext.getArgument(TransactionsRangeCommand.this, player);
        PlayerAccount<? extends PlayerAccount<?>> playerAccount = AccountInterface.getManager().getPlayerAccount(user);
        if (!(playerAccount instanceof EcoAccount<?> ecoAccount)) {
            //no transactions prior to this plugins release date
            return 2024;
        }
        return ecoAccount
                .getTransactionHistory()
                .parallelStream()
                .map(EntryTransactionHistory::getTime)
                .min(Comparator.naturalOrder())
                .map(LocalDateTime::getYear)
                .orElse(2024);
    }, (cmdContext, argContext) -> {
        OfflinePlayer user = cmdContext.getArgument(TransactionsRangeCommand.this, player);
        PlayerAccount<? extends PlayerAccount<?>> playerAccount = AccountInterface.getManager().getPlayerAccount(user);
        if (!(playerAccount instanceof EcoAccount<?> ecoAccount)) {
            return LocalDateTime.now().getYear();
        }
        return ecoAccount
                .getTransactionHistory()
                .parallelStream()
                .map(EntryTransactionHistory::getTime)
                .max(Comparator.naturalOrder())
                .orElseGet(LocalDateTime::now)
                .getYear();
    });

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(player, startDate);
    }

    @Override
    public @NotNull String getDescription() {
        return "History of transactions";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        OfflinePlayer user = commandContext.getArgument(this, player);
        PlayerAccount<? extends PlayerAccount<?>> playerAccount = AccountInterface.getManager().getPlayerAccount(user);
        if (!(playerAccount instanceof EcoAccount<?> ecoAccount)) {
            return false;
        }

        LocalDateTime min = commandContext.getArgument(this, startDate);

        var result = ecoAccount.getTransactionHistory().getBetween(min, LocalDateTime.now());
        commandContext.getSource().sendMessage(result.size() + " transactions");
        return true;
    }
}
