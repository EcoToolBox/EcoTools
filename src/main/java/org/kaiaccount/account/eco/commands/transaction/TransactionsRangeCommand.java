package org.kaiaccount.account.eco.commands.transaction;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.account.EcoAccount;
import org.kaiaccount.account.eco.account.history.EntryTransactionHistory;
import org.kaiaccount.account.eco.commands.argument.date.DateRangeArgument;
import org.kaiaccount.account.eco.commands.argument.date.DateTimeArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.ParseCommandArgument;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.arguments.simple.number.IntegerArgument;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TransactionsRangeCommand implements ArgumentCommand {

    public static final int PAGE_SIZE = 6;
    public static final char WITHDRAW_ARROW = '→';
    public static final char DEPOSIT_ARROW = '←';
    public static final char SET_ARROW = '⤓';

    private final CommandArgument<OfflinePlayer> player = new OptionalArgument<>(new PermissionOrArgument<>("user",
            source -> source.hasPermission(Permissions.HISTORY_OTHER.getPermissionNode()),
            new UserArgument("user", (command, argument) -> Arrays.stream(Bukkit.getOfflinePlayers()))), new ParseCommandArgument<>() {
        @Override
        public @NotNull CommandArgumentResult<OfflinePlayer> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument)
                throws ArgumentException {
            if (context.getSource() instanceof OfflinePlayer user) {
                return CommandArgumentResult.from(argument, 0, user);
            }
            throw new ArgumentException("Player needs to be specified");
        }
    });
    private final CommandArgument<LocalDateTime> startDate = new DateTimeArgument("start", (cmdContext, argContext) -> {
        OfflinePlayer user = cmdContext.getArgument(TransactionsRangeCommand.this, player);
        PlayerAccount playerAccount = AccountInterface.getManager().getPlayerAccount(user);
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
        PlayerAccount playerAccount = AccountInterface.getManager().getPlayerAccount(user);
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
    private final CommandArgument<Duration> range = new DateRangeArgument("id", startDate, -1);
    private final CommandArgument<Integer> page = new OptionalArgument<>(new IntegerArgument("page"), 1);

    private char arrow(TransactionType type) {
        return switch (type) {
            case SET -> SET_ARROW;
            case DEPOSIT -> DEPOSIT_ARROW;
            case WITHDRAW -> WITHDRAW_ARROW;
        };
    }

    private ChatColor color(TransactionType type) {
        return switch (type) {
            case SET -> ChatColor.YELLOW;
            case DEPOSIT -> ChatColor.GREEN;
            case WITHDRAW -> ChatColor.RED;
        };
    }

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(player, startDate, range, page);
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
        PlayerAccount playerAccount = AccountInterface.getManager().getPlayerAccount(user);
        if (!(playerAccount instanceof EcoAccount<?> ecoAccount)) {
            return false;
        }

        LocalDateTime min = commandContext.getArgument(this, startDate);
        Duration maxDuration = commandContext.getArgument(this, range);
        LocalDateTime max = min.plus(maxDuration);

        List<EntryTransactionHistory> result = ecoAccount.getTransactionHistory().getBetween(min, max);
        int page = commandContext.getArgument(this, this.page);
        long skipEntries = (long) PAGE_SIZE * (page - 1);
        List<EntryTransactionHistory> list = result
                .parallelStream()
                .filter(entry -> entry.getTime().isBefore(max))
                .filter(entry -> entry.getTime().isAfter(min))
                .sorted(Comparator.comparing(EntryTransactionHistory::getTime))
                .skip(skipEntries)
                .limit(PAGE_SIZE)
                .toList();
        commandContext.getSource().sendMessage("|---|Page: " + page + "|---|");
        list.forEach(entry -> {
            String target = entry.getFromName().or(entry::getToName).orElseGet(entry::getPluginName);
            String amount = entry.getCurrency().formatName(entry.getAmount());
            char arrow = arrow(entry.getTransactionType());
            String reason = entry.getReason().map(theReason -> ": " + theReason).orElse("");
            String message = amount + " " + arrow + " " + target + reason;
            ChatColor colour = color(entry.getTransactionType());
            commandContext.getSource().sendMessage(colour + message);
        });
        return true;
    }
}
