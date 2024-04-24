package org.kaiaccount.account.eco.commands.transaction;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.account.EcoAccount;
import org.kaiaccount.account.eco.account.history.EntryTransactionHistory;
import org.kaiaccount.account.eco.commands.argument.account.AccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.NamedAccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.commands.argument.date.DateRangeArgument;
import org.kaiaccount.account.eco.commands.argument.date.DateTimeArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.message.messages.error.SourceOnlyCommandMessage;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.MappedArgumentWrapper;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.arguments.simple.number.IntegerArgument;
import org.mose.command.builder.CommandBuilder;
import org.mose.command.exception.ArgumentException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class TransactionsRangeCommands {

    public static final int PAGE_SIZE = 6;
    public static final char WITHDRAW_ARROW = '→';
    public static final char DEPOSIT_ARROW = '←';
    public static final char SET_ARROW = '⤓';

    private static char arrow(TransactionType type) {
        return switch (type) {
            case SET -> SET_ARROW;
            case DEPOSIT -> DEPOSIT_ARROW;
            case WITHDRAW -> WITHDRAW_ARROW;
        };
    }

    private static ChatColor color(TransactionType type) {
        return switch (type) {
            case SET -> ChatColor.YELLOW;
            case DEPOSIT -> ChatColor.GREEN;
            case WITHDRAW -> ChatColor.RED;
        };
    }

    private static CommandArgument<LocalDateTime> startDateArgument(String id, Supplier<ArgumentCommand> supplier, CommandArgument<Account> accountArgument) {
        return new DateTimeArgument(id, (context, argument) -> {
            Account account = context.getArgument(supplier, accountArgument);
            if (!(account instanceof EcoAccount<?> ecoAccount)) {
                return 2024;
            }
            return ecoAccount
                    .getTransactionHistory()
                    .parallelStream()
                    .map(EntryTransactionHistory::getTime)
                    .min(Comparator.naturalOrder())
                    .map(LocalDateTime::getYear)
                    .orElse(2024);
        }, (context, argument) -> {
            Account account = context.getArgument(supplier, accountArgument);
            if (!(account instanceof EcoAccount<?> ecoAccount)) {
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
    }

    private static CommandArgument<Account> targetAccountArgument(String id) {
        CommandArgument<PlayerBankAccount> allBanks = PlayerBankArgument.allPlayerBanks("bank");
        CommandArgument<NamedAccount> allNamedAccounts = new NamedAccountArgument("named");
        CommandArgument<PlayerAccount> allPlayers = new MappedArgumentWrapper<>(new UserArgument("player",
                (context, argument) -> Arrays.stream(Bukkit.getOfflinePlayers())), user -> AccountInterface.getManager().getPlayerAccount(user));

        AccountArgument<Account> allAccounts = new AccountArgument<>(id, allBanks, allNamedAccounts, allPlayers);

        CommandArgument<PlayerBankAccount> selfBanks = PlayerBankArgument.banksWithPermission("bank",
                BankPermission.SEE,
                (source) -> new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE)));

        CommandArgument<Account> selfAccounts = new AccountArgument<>(id, selfBanks);

        PermissionOrArgument<Account> permissionAccount = new PermissionOrArgument<>(id,
                source -> source.hasPermission(Permissions.HISTORY_OTHER.getPermissionNode()),
                allAccounts,
                selfAccounts);

        return new OptionalArgument<Account>(permissionAccount, (context, argument) -> {
            if (!(context.getSource() instanceof OfflinePlayer player)) {
                throw new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE));
            }
            PlayerAccount account = AccountInterface.getManager().getPlayerAccount(player);
            return CommandArgumentResult.from(argument, account);
        });
    }

    public static ArgumentCommand transactionRangeCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Account> accountArgument = targetAccountArgument("target");
            CommandArgument<LocalDateTime> startDateArgument = startDateArgument("start", supplier, accountArgument);
            CommandArgument<Duration> rangeArgument = new DateRangeArgument("range", startDateArgument, -1);
            CommandArgument<Integer> pageArgument = new OptionalArgument<>(new IntegerArgument("page"), 1);

            return builder
                    .addArguments(accountArgument, startDateArgument, rangeArgument, pageArgument)
                    .setDescription("History of transactions")
                    .setPermissionNode(Permissions.HISTORY_SELF.getPermissionNode())
                    .setExecutor((context, raw) -> {
                        Account account = context.getArgument(supplier, accountArgument);
                        if (!(account instanceof EcoAccount<?> ecoAccount)) {
                            return false;
                        }
                        LocalDateTime min = context.getArgument(supplier, startDateArgument);
                        Duration maxDuration = context.getArgument(supplier, rangeArgument);
                        LocalDateTime max = min.plus(maxDuration);

                        List<EntryTransactionHistory> result = ecoAccount.getTransactionHistory().getBetween(min, max);
                        int page = context.getArgument(supplier, pageArgument);
                        long skipEntries = (long) PAGE_SIZE * (page - 1);
                        List<EntryTransactionHistory> list = result
                                .parallelStream()
                                .filter(entry -> entry.getTime().isBefore(max))
                                .filter(entry -> entry.getTime().isAfter(min))
                                .sorted(Comparator.comparing(EntryTransactionHistory::getTime))
                                .skip(skipEntries)
                                .limit(PAGE_SIZE)
                                .toList();
                        context.getSource().sendMessage("|---|Page: " + page + "|---|");
                        list.forEach(entry -> {
                            String target = entry.getFromName().or(entry::getToName).orElseGet(entry::getPluginName);
                            String amount = entry.getCurrency().formatName(entry.getAmount());
                            char arrow = arrow(entry.getTransactionType());
                            String reason = entry.getReason().map(theReason -> ": " + theReason).orElse("");
                            String message = amount + " " + arrow + " " + target + reason;
                            ChatColor colour = color(entry.getTransactionType());
                            context.getSource().sendMessage(colour + message);
                        });
                        return true;
                    })
                    .build();
        });
    }

}
