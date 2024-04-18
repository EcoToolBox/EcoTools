package org.kaiaccount.account.eco.commands.exchange;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.account.AccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.NamedAccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.commands.argument.currency.CurrencyArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.transfer.result.successful.SuccessfulTransactionResult;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.MappedArgumentWrapper;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.arguments.simple.number.DoubleArgument;
import org.mose.command.builder.CommandBuilder;
import org.mose.command.exception.ArgumentException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public final class ExchangeCommands {

    public ExchangeCommands(){
        throw new RuntimeException("No dont");
    }

    public static ArgumentCommand createExchangeCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Account> targetAccountArgument = targetAccountCommand("target");
            CommandArgument<Currency<?>> fromCurrencyArgument = new CurrencyArgument("from", (context, argument) -> {
                Account account = context.getArgument(supplier, targetAccountArgument);
                return account.getBalances().keySet().stream().filter(currency -> currency.getWorth().isPresent()).toList();
            });
            CommandArgument<Double> amountArgument = new DoubleArgument("amount");
            CommandArgument<Currency<?>> toCurrencyArgument = new CurrencyArgument("to",
                    (context, argument) -> AccountInterface.getManager().getCurrencies().stream().filter(currency -> currency.getWorth().isPresent()).toList());
            return builder
                    .setPermissionNode(Permissions.EXCHANGE.getPermissionNode())
                    .addArguments(targetAccountArgument, fromCurrencyArgument, amountArgument)
                    .setDescription("Transfer one currency to another")
                    .setExecutor((context, raw) -> {
                        Account account = context.getArgument(supplier, targetAccountArgument);
                        if (!(account instanceof AccountType accountType)) {
                            context.getSource().sendMessage(Messages.ACCOUNT_IS_NOT_OF_ACCOUNT_TYPE.process(account, "exchange", "EcoToolsAccount"));
                            return false;
                        }
                        Currency<?> from = context.getArgument(supplier, fromCurrencyArgument);
                        Currency<?> to = context.getArgument(supplier, toCurrencyArgument);
                        Double amount = context.getArgument(supplier, amountArgument);
                        BigDecimal amountDecimal = BigDecimal.valueOf(amount);

                        BigDecimal previousFrom = account.getBalance(from);
                        BigDecimal previousTo = account.getBalance(to);

                        BigDecimal exchange = amountDecimal.divide(from.getWorth().orElseThrow(), RoundingMode.DOWN).multiply(to.getWorth().orElseThrow());

                        BigDecimal newFrom = previousFrom.subtract(amountDecimal);
                        BigDecimal newTo = previousTo.add(exchange);

                        accountType
                                .multipleTransaction(a -> a.set(new PaymentBuilder()
                                                .setAmount(newFrom)
                                                .setCurrency(from)
                                                .setReason("Exchange")
                                                .setFrom((NamedAccountLike) a)
                                                .setPlugin(EcoToolPlugin.getInstance())
                                                .build()),
                                        a -> a.set(new PaymentBuilder()
                                                .setAmount(newTo)
                                                .setCurrency(to)
                                                .setReason("Exchange")
                                                .setFrom((NamedAccountLike) a)
                                                .setPlugin(EcoToolPlugin.getInstance())
                                                .build()))
                                .thenAccept(result -> {
                                    if (result == null || result instanceof SuccessfulTransactionResult) {
                                        context.getSource().sendMessage(Messages.TRANSACTION_SUCCESSFUL.getProcessedMessage());
                                        return;
                                    }
                                    String reason = "Unknown";
                                    if (result instanceof FailedTransactionResult failed) {
                                        reason = failed.getReason();
                                    }
                                    context.getSource().sendMessage(Messages.TRANSACTION_FAILED.getProcessedMessage(reason));
                                });

                        context.getSource().sendMessage(Messages.EXCHANGE_STARTED.getProcessedMessage(from, to, amountDecimal));
                        return true;
                    })
                    .build();
        });
    }

    private static CommandArgument<Account> targetAccountCommand(String id) {
        CommandArgument<Account> allPlayerAccounts = new MappedArgumentWrapper<>(new UserArgument(id,
                (command, argument) -> Arrays.stream(Bukkit.getOfflinePlayers())), user -> AccountInterface.getManager().getPlayerAccount(user));
        CommandArgument<PlayerBankAccount> allBankAccounts = PlayerBankArgument.allPlayerBanks(id);
        NamedAccountArgument allNamedAccounts = new NamedAccountArgument(id);

        CommandArgument<Account> allAccounts = new AccountArgument<>(id, allBankAccounts, allNamedAccounts, allPlayerAccounts);

        PlayerBankArgument selfBanks = PlayerBankArgument.banksWithAllPermissions(id, (command, argument) -> {
            if (command.getSource() instanceof OfflinePlayer player) {
                return CommandArgumentResult.from(argument, player.getUniqueId());
            }
            throw new RuntimeException("Player only command");
        }, BankPermission.GIVE, BankPermission.TAKE);
        CommandArgument<Account> selfBankAccounts = new AccountArgument<>(id, selfBanks);

        PermissionOrArgument<Account> permissionArgument = new PermissionOrArgument<>(id,
                source -> source.hasPermission(Permissions.EXCHANGE_OTHER.getPermissionNode()),
                allAccounts,
                selfBankAccounts);
        return new OptionalArgument<Account>(permissionArgument, (command, argument) -> {
            if (command.getSource() instanceof OfflinePlayer player) {
                return CommandArgumentResult.from(argument, AccountInterface.getManager().getPlayerAccount(player));
            }
            throw new ArgumentException("Account must be specified");
        });
    }


}
