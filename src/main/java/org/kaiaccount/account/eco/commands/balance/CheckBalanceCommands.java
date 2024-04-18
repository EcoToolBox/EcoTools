package org.kaiaccount.account.eco.commands.balance;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.commands.argument.account.NamedAccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.message.messages.error.SourceOnlyCommandMessage;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.eco.utils.CommonUtils;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.builder.CommandBuilder;
import org.mose.command.exception.ArgumentException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CheckBalanceCommands {

    private CheckBalanceCommands() {
        throw new RuntimeException("Shouldnt do. Stop");
    }

    public static ArgumentCommand checkBalanceBasicCommand() {
        //redirects rather than using optional to prevent a issue where a player is called "bank" and therefore the system getting confused if the source
        // meant the player or the argument
        return CommandBuilder.build((supplier, builder) -> builder
                .setExecutor((context, raw) -> Bukkit.dispatchCommand(context.getSource(), "balance player"))
                .setDescription("Check your own balance")
                .setPermissionSenderCheck((sender) -> {
                    if (!(sender.hasPermission(Permissions.BALANCE_SELF.getPermissionNode()))) {
                        return false;
                    }
                    return sender instanceof OfflinePlayer;
                })
                .build());
    }

    public static ArgumentCommand checkBankBalanceCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<OfflinePlayer> targetArgument = targetUserWithBanks();
            CommandArgument<PlayerBankAccount> bankArgument = PlayerBankArgument.banksWithPermission("bank",
                    (command, argument) -> CommandArgumentResult.from(argument, command.getArgument(supplier, targetArgument).getUniqueId()),
                    BankPermission.SEE);

            return builder
                    .setDescription("See the balance of your bank account")
                    .addArguments(new ExactArgument("type", false, "bank"))
                    .setPermissionNode(Permissions.BALANCE_SELF.getPermissionNode())
                    .setExecutor((context, raw) -> displayInfo(context.getSource(), context.getArgument(supplier, bankArgument)))
                    .build();
        });
    }

    public static ArgumentCommand checkNamedAccountBalance() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<NamedAccount> accountArgument = new NamedAccountArgument("named account");

            return builder
                    .addArguments(new ExactArgument("type", false, "named"))
                    .setDescription("See the balance of a named account")
                    .setPermissionNode(Permissions.BALANCE_OTHER.getPermissionNode())
                    .setExecutor((context, raw) -> displayInfo(context.getSource(), context.getArgument(supplier, accountArgument)))
                    .build();
        });
    }

    public static ArgumentCommand checkPlayerBalanceCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<OfflinePlayer> targetArgument = targetUserWithPlayer();

            return builder
                    .addArguments(new ExactArgument("type", false, "player"), targetArgument)
                    .setDescription("Checks the balance of either yourself or another player")
                    .setPermissionNode(Permissions.BALANCE_SELF.getPermissionNode())
                    .setExecutor((context, raw) -> displayInfo(context.getSource(),
                            AccountInterface.getManager().getPlayerAccount(context.getArgument(supplier, targetArgument))))
                    .build();
        });
    }

    private static boolean displayInfo(@NotNull CommandSender sender, @NotNull Account account) {
        Map<Currency<?>, BigDecimal> balances = account.getBalances();
        balances.forEach(((currency, balance) -> sender.sendMessage("  " + currency.formatSymbol(balance))));

        Optional<Currency<?>> opDefaultCurrency = AccountInterface.getManager().getDefaultCurrency();
        if(opDefaultCurrency.isEmpty()){
            sender.sendMessage("Worth cannot be calculated: No default currency");
            return true;
        }
        Currency<?> defaultCurrency = opDefaultCurrency.get();
        if (defaultCurrency.getWorth().isPresent()) {
            Collection<BigDecimal> collection = balances
                    .entrySet()
                    .parallelStream()
                    .filter(entry -> entry.getKey().getWorth().isPresent())
                    .map(entry -> entry.getValue().divide(entry.getKey().getWorth().get(), RoundingMode.DOWN).multiply(defaultCurrency.getWorth().get()))
                    .collect(Collectors.toSet());

            BigDecimal worth = CommonUtils.sumOf(collection.iterator());
            String message = Messages.TOTAL_WORTH.getProcessedMessage(sender, worth);
            sender.sendMessage(message);
        }
        return true;

    }

    private static CommandArgument<OfflinePlayer> targetUserWithBanks() {
        CommandArgument<OfflinePlayer> userArgument = new UserArgument("user",
                (command, context) -> Arrays
                        .stream(Bukkit.getOfflinePlayers())
                        .filter(player -> !AccountInterface.getManager().getPlayerAccount(player).getBanks().isEmpty()));
        return targetWithPermission(userArgument);
    }

    private static CommandArgument<OfflinePlayer> targetUserWithPlayer() {
        CommandArgument<OfflinePlayer> userArgument = new UserArgument("user", (command, context) -> Arrays.stream(Bukkit.getOfflinePlayers()));
        return targetWithPermission(userArgument);
    }

    private static CommandArgument<OfflinePlayer> targetWithPermission(CommandArgument<OfflinePlayer> targetArgument) {
        PermissionOrArgument<OfflinePlayer> permissionArgument = new PermissionOrArgument<>("user",
                source -> source.hasPermission(Permissions.BALANCE_OTHER.getPermissionNode()),
                targetArgument);
        return new OptionalArgument<OfflinePlayer>(permissionArgument, (command, context) -> {
            if (command.getSource() instanceof OfflinePlayer player) {
                return CommandArgumentResult.from(context, player);
            }
            throw new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE));

        });
    }


}
