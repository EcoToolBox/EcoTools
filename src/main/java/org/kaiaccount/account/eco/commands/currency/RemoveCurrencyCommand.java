package org.kaiaccount.account.eco.commands.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.currency.CurrencyArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.transfer.result.successful.SuccessfulTransactionResult;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.IsolatedAccount;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.context.CommandContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class RemoveCurrencyCommand implements ArgumentCommand {

    public static final CommandArgument<String> REMOVE = new ExactArgument("remove");
    public static final CommandArgument<Currency<?>> CURRENCY = new CurrencyArgument("currency");
    public static final CommandArgument<Currency<?>> EXCHANGE_TO = new OptionalArgument<>(
            new CurrencyArgument("exchangeTo", (context, argument) -> AccountInterface.getManager()
                    .getCurrencies()
                    .parallelStream()
                    .filter(currency -> currency.getWorth().isPresent())
                    .toList()));

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(REMOVE, CURRENCY);
    }

    @Override
    public @NotNull String getDescription() {
        return "Removes a currency";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.REMOVE_CURRENCY.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        Currency<?> currencyToRemove = commandContext.getArgument(this, CURRENCY);
        Currency<?> currencyToExchange = commandContext.getArgument(this, EXCHANGE_TO);

        List<PlayerAccount> issuePlayerAccounts = AccountInterface.getManager()
                .getPlayerAccounts()
                .parallelStream()
                .filter(p -> p.getBalance(currencyToRemove).compareTo(
                        BigDecimal.ZERO) != 0)
                .toList();
        List<PlayerBankAccount> issueBankAccounts = AccountInterface.getManager()
                .getPlayerAccounts()
                .parallelStream()
                .flatMap(p -> p.getBanks().parallelStream())
                .filter(p -> p.getBalance(currencyToRemove).compareTo(BigDecimal.ZERO) != 0)
                .toList();
        if (issueBankAccounts.isEmpty() && issuePlayerAccounts.isEmpty()) {
            AccountInterface.getManager().deregisterCurrency(currencyToRemove);
            currencyToRemove.delete();
            commandContext.getSource().sendMessage(currencyToRemove.getSymbol() + " has been removed");
            return true;
        }

        if (currencyToExchange != null) {
            if (currencyToExchange.getWorth().isEmpty()) {
                commandContext.getSource().sendMessage("Exchange currency does not have a exchange value");
                return true;
            }
            if (currencyToRemove.getWorth().isEmpty()) {
                commandContext.getSource().sendMessage("Removing currency does not have a exchange value");
                return true;
            }
            if (issueBankAccounts.parallelStream().anyMatch(bank -> bank instanceof AccountType)) {
                commandContext.getSource()
                        .sendMessage("Not all banks are of the correct type. Another currency plugin found?");
                return true;
            }
            if (issuePlayerAccounts.parallelStream().anyMatch(player -> player instanceof AccountType)) {
                commandContext.getSource()
                        .sendMessage("Not all players are of the correct type. Another currency plugin found?");
                return true;
            }

            List<AccountType> accountType = new LinkedList<>();
            accountType.addAll(issuePlayerAccounts.parallelStream().map(p -> (AccountType) p).toList());
            accountType.addAll(issueBankAccounts.parallelStream().map(p -> (AccountType) p).toList());
            new IsolatedTransaction(map -> exchange(map, currencyToRemove, currencyToExchange, accountType),
                    accountType)
                    .start()
                    .thenAccept(result -> {
                        if (result instanceof SuccessfulTransactionResult) {
                            AccountInterface.getManager().deregisterCurrency(currencyToRemove);
                            currencyToRemove.delete();
                            commandContext.getSource()
                                    .sendMessage(currencyToRemove.getSymbol() + " removed. Exchanged all balances");
                            return;
                        }

                        commandContext.getSource().sendMessage("Could not exchange all values, reset all balances.");
                        if (result instanceof FailedTransactionResult failed) {
                            commandContext.getSource().sendMessage("Failed reason: " + failed.getReason());
                        }
                    });
        }

        commandContext.getSource().sendMessage("Could not remove currency. Players still have money in this currency");
        return true;
    }

    private Collection<CompletableFuture<? extends TransactionResult>> exchange(
            Map<AccountType, IsolatedAccount> toAccount, Currency<?> toRemove, Currency<?> toExchange,
            Collection<AccountType> accountType) {
        Stream<CompletableFuture<? extends TransactionResult>> stream =
                accountType.parallelStream().map(account -> {
                    IsolatedAccount iso = toAccount.get(account);
                    return iso.multipleTransaction(
                            map1 -> exchangeTo(map1, toRemove, toExchange),
                            map1 -> setToZero(map1, toRemove));
                });
        return stream.toList();
    }

    private CompletableFuture<SingleTransactionResult> setToZero(@NotNull Account account,
                                                                 @NotNull Currency<?> remove) {
        return account.set(new PaymentBuilder().setCurrency(remove)
                .setAmount(BigDecimal.ZERO)
                .build(EcoToolPlugin.getPlugin()));
    }

    private CompletableFuture<SingleTransactionResult> exchangeTo(@NotNull Account account,
                                                                  @NotNull Currency<?> toRemove, @NotNull Currency<?> toExchange) {
        BigDecimal currentBalance = account.getBalance(toRemove);
        currentBalance = currentBalance.divide(
                        toRemove.getWorth().orElseThrow(() -> new RuntimeException("Worth not found in to remove")),
                        RoundingMode.DOWN)
                .multiply(toExchange.getWorth()
                        .orElseThrow(() -> new RuntimeException("Worth not found in to exchange")));
        return account.deposit(new PaymentBuilder().setCurrency(toExchange)
                .setAmount(currentBalance)
                .build(EcoToolPlugin.getPlugin()));
    }
}
