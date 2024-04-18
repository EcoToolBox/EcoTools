package org.kaiaccount.account.eco.commands.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.currency.CurrencyArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.currency.CurrencyBuilder;
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
import org.mose.command.arguments.simple.number.DoubleArgument;
import org.mose.command.arguments.simple.text.StringArgument;
import org.mose.command.builder.CommandBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class CurrencyCommands {

    private CurrencyCommands() {
        throw new RuntimeException("Dont do");
    }

    public static ArgumentCommand createCurrencyCreateCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<String> nameArgument = new StringArgument("name");
            CommandArgument<String> symbolArgument = new StringArgument("symbol");
            CommandArgument<String> singleNameArgument = new StringArgument("single");
            CommandArgument<String> multipleNameArgument = new StringArgument("multi");
            CommandArgument<String> shortNameArgument = new StringArgument("short");
            CommandArgument<Double> worthArgument = new DoubleArgument("worth");

            return builder
                    .setDescription("Adds a new currency to the system")
                    .setPermissionNode(Permissions.ADD_CURRENCY.getPermissionNode())
                    .addArguments(new ExactArgument("add"),
                            symbolArgument,
                            nameArgument,
                            singleNameArgument,
                            multipleNameArgument,
                            shortNameArgument,
                            worthArgument)
                    .setExecutor((context, raw) -> {
                        String symbol = context.getArgument(supplier, symbolArgument);
                        String name = context.getArgument(supplier, nameArgument);
                        String singleDisplay = context.getArgument(supplier, singleNameArgument);
                        String multiDisplay = context.getArgument(supplier, multipleNameArgument);
                        String shortDisplay = context.getArgument(supplier, shortNameArgument);
                        Double worth = context.getArgument(supplier, worthArgument);
                        boolean isDefault = AccountInterface.getManager().getCurrencies().isEmpty();
                        Currency<?> currency = new CurrencyBuilder()
                                .setName(name)
                                .setPlugin(EcoToolPlugin.getInstance())
                                .setSymbol(symbol)
                                .setDisplayNameMultiple(multiDisplay)
                                .setDisplayNameSingle(singleDisplay)
                                .setDisplayNameShort(shortDisplay)
                                .setWorth(worth)
                                .setDefault(isDefault)
                                .build();

                        Optional<Currency<?>> alreadyRegistered = AccountInterface.getManager().getCurrencies().parallelStream().filter(search -> {
                            if (search.getKeyName().equalsIgnoreCase(name) && search.getPlugin().equals(EcoToolPlugin.getInstance())) {
                                return true;
                            }
                            return search.getSymbol().equalsIgnoreCase(symbol);
                        }).findAny();
                        if (alreadyRegistered.isPresent()) {
                            Currency<?> alreadyRegisteredCurrency = alreadyRegistered.get();
                            context.getSource().sendMessage(Messages.CURRENCY_ALREADY_REGISTERED.getProcessedMessage());
                            context.getSource().sendMessage("\t- " + alreadyRegisteredCurrency.getSymbol());
                            context.getSource().sendMessage("\t- " + alreadyRegisteredCurrency.getPlugin().getName());
                            context.getSource().sendMessage("\t- " + alreadyRegisteredCurrency.getKeyName());
                            return true;
                        }

                        try {
                            currency.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                            context.getSource().sendMessage(Messages.CURRENCY_COULD_NOT_BE_REGISTERED.getProcessedMessage(e.getMessage()));
                            return true;
                        }

                        context.getSource().sendMessage(Messages.CURRENCY_REGISTERED.getProcessedMessage());
                        context.getSource().sendMessage(currency.formatSymbol(BigDecimal.ONE));
                        context.getSource().sendMessage(currency.formatName(BigDecimal.ONE));
                        context.getSource().sendMessage(currency.formatName(BigDecimal.TEN));
                        AccountInterface.getManager().registerCurrency(currency);
                        return true;
                    })
                    .build();
        });
    }

    public static ArgumentCommand createCurrencyRemoveCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Currency<?>> currencyArgument = new CurrencyArgument("currency");
            CommandArgument<Currency<?>> exchangeToArgument = new OptionalArgument<>(new CurrencyArgument("exchangeTo", (command, argument) -> {
                Currency<? extends Currency<?>> targetCurrency = command.getArgument(supplier, currencyArgument);
                if (targetCurrency.getWorth().isEmpty()) {
                    return Collections.emptyList();
                }

                return AccountInterface.getManager().getCurrencies().stream().filter(currency -> currency.getWorth().isPresent()).toList();
            }));

            return builder
                    .setPermissionNode(Permissions.REMOVE_CURRENCY.getPermissionNode())
                    .setDescription("Removes a currency")
                    .addArguments(new ExactArgument("remove"), currencyArgument, exchangeToArgument)
                    .setExecutor((context, raw) -> {
                        Currency<?> currencyToRemove = context.getArgument(supplier, currencyArgument);
                        Currency<?> currencyToExchange = context.getArgument(supplier, exchangeToArgument);

                        List<PlayerAccount> issuePlayerAccounts = AccountInterface
                                .getManager()
                                .getPlayerAccounts()
                                .parallelStream()
                                .filter(p -> p.getBalance(currencyToRemove).compareTo(BigDecimal.ZERO) != 0)
                                .toList();
                        List<PlayerBankAccount> issueBankAccounts = AccountInterface
                                .getManager()
                                .getPlayerAccounts()
                                .parallelStream()
                                .flatMap(p -> p.getBanks().parallelStream())
                                .filter(p -> p.getBalance(currencyToRemove).compareTo(BigDecimal.ZERO) != 0)
                                .toList();
                        if (issueBankAccounts.isEmpty() && issuePlayerAccounts.isEmpty()) {
                            AccountInterface.getManager().deregisterCurrency(currencyToRemove);
                            currencyToRemove.delete();
                            context.getSource().sendMessage(Messages.CURRENCY_UNREGISTERED.getProcessedMessage(currencyToRemove));
                            return true;
                        }

                        if (currencyToExchange == null) {
                            context.getSource().sendMessage(Messages.CURRENCY_FAILED_REMOVAL_FOR_REASON.getProcessedMessage(currencyToRemove, ""));
                            return true;
                        }
                        Optional<PlayerBankAccount> opNoneAccount = issueBankAccounts.parallelStream().filter(bank -> !(bank instanceof AccountType)).findAny();
                        if (opNoneAccount.isPresent()) {
                            context.getSource().sendMessage(Messages.ACCOUNT_IS_NOT_OF_ACCOUNT_TYPE.process(opNoneAccount.get(), "deposit", "bank"));
                            return true;
                        }
                        Optional<PlayerAccount> opNonePlayer = issuePlayerAccounts
                                .parallelStream()
                                .filter(player -> !(player instanceof AccountType))
                                .findAny();
                        if (opNonePlayer.isPresent()) {
                            context.getSource().sendMessage(Messages.ACCOUNT_IS_NOT_OF_ACCOUNT_TYPE.process(opNonePlayer.get(), "deposit", "bank"));
                            return true;
                        }

                        List<AccountType> accountType = new LinkedList<>();
                        accountType.addAll(issuePlayerAccounts.parallelStream().map(p -> (AccountType) p).toList());
                        accountType.addAll(issueBankAccounts.parallelStream().map(p -> (AccountType) p).toList());
                        new IsolatedTransaction(map -> exchange(map, currencyToRemove, currencyToExchange, accountType), accountType)
                                .start()
                                .thenAccept(result -> {
                                    if (result instanceof SuccessfulTransactionResult) {
                                        AccountInterface.getManager().deregisterCurrency(currencyToRemove);
                                        currencyToRemove.delete();
                                        context.getSource().sendMessage(Messages.CURRENCY_UNREGISTERED.getProcessedMessage(currencyToRemove));
                                        return;
                                    }

                                    context.getSource().sendMessage("Could not exchange all values, reset all balances.");
                                    if (result instanceof FailedTransactionResult failed) {
                                        context
                                                .getSource()
                                                .sendMessage(Messages.CURRENCY_FAILED_REMOVAL_FOR_REASON.getProcessedMessage(currencyToRemove,
                                                        failed.getReason()));
                                    }
                                });
                        return true;
                    })
                    .build();
        });
    }

    public static ArgumentCommand createDefaultCurrencyCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Currency<?>> currencyArgument = new CurrencyArgument("currency");

            return builder
                    .setDescription("Sets the default currency")
                    .setPermissionNode(Permissions.SET_DEFAULT_CURRENCY.getPermissionNode())
                    .addArguments(new ExactArgument("set"), new ExactArgument("default"), currencyArgument)
                    .setExecutor((context, raw) -> {
                        Currency<?> newDefault = context.getArgument(supplier, currencyArgument);
                        Optional<Currency<?>> opPreviousDefault = AccountInterface.getManager().getDefaultCurrency();
                        if(!(opPreviousDefault.isPresent() && opPreviousDefault.get().equals(newDefault))) {
                            //if both previous and new are same, ignore this step
                            newDefault.setDefault(true);
                            opPreviousDefault.ifPresent(previousDefault -> previousDefault.setDefault(false));
                        }
                        context.getSource().sendMessage(Messages.DEFAULT_CURRENCY_CHANGED.getProcessedMessage(opPreviousDefault.orElse(null), newDefault));
                        return true;
                    })
                    .build();
        });
    }

    public static ArgumentCommand createSetExchangeValueCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Currency<?>> currencyArgument = new CurrencyArgument("currency");
            CommandArgument<Double> valueArgument = new OptionalArgument<>(new DoubleArgument("value"), 0.0);

            return builder
                    .setDescription("Sets the exchange value of the specified currency")
                    .setPermissionNode(Permissions.SET_EXCHANGE_CURRENCY.getPermissionNode())
                    .addArguments(new ExactArgument("set"), new ExactArgument("exchange"), currencyArgument, valueArgument)
                    .setExecutor((context, raw) -> {
                        Currency<?> currency = context.getArgument(supplier, currencyArgument);
                        double amount = context.getArgument(supplier, valueArgument);

                        if (amount == 0) {
                            currency.removeWorth();
                            context.getSource().sendMessage(Messages.EXCHANGE_VALUE_REMOVED.getProcessedMessage(currency));
                            return true;
                        }
                        if (amount < 0) {
                            context.getSource().sendMessage(Messages.GREATER_THAN.getProcessedMessage("worth", 0));
                            return false;
                        }
                        BigDecimal decimal = BigDecimal.valueOf(amount);
                        currency.setWorth(decimal);
                        context.getSource().sendMessage(Messages.EXCHANGE_VALUE_SET.getProcessedMessage(currency, decimal));
                        return true;
                    })
                    .build();
        });
    }

    private static Collection<CompletableFuture<? extends TransactionResult>> exchange(Map<AccountType, IsolatedAccount> toAccount,
                                                                                       Currency<?> toRemove,
                                                                                       Currency<?> toExchange,
                                                                                       Collection<AccountType> accountType) {
        Stream<CompletableFuture<? extends TransactionResult>> stream = accountType.parallelStream().map(account -> {
            IsolatedAccount iso = toAccount.get(account);
            return iso.multipleTransaction(map1 -> exchangeTo(map1, toRemove, toExchange), map1 -> setToZero(map1, toRemove));
        });
        return stream.toList();
    }

    private static CompletableFuture<SingleTransactionResult> exchangeTo(@NotNull Account account,
                                                                         @NotNull Currency<?> toRemove,
                                                                         @NotNull Currency<?> toExchange) {
        BigDecimal currentBalance = account.getBalance(toRemove);
        currentBalance = currentBalance
                .divide(toRemove.getWorth().orElseThrow(() -> new RuntimeException("Worth not found in to remove")), RoundingMode.DOWN)
                .multiply(toExchange.getWorth().orElseThrow(() -> new RuntimeException("Worth not found in to exchange")));
        return account.deposit(new PaymentBuilder().setCurrency(toExchange).setAmount(currentBalance).setPlugin(EcoToolPlugin.getInstance()).build());
    }

    private static CompletableFuture<SingleTransactionResult> setToZero(@NotNull Account account, @NotNull Currency<?> remove) {
        return account.set(new PaymentBuilder().setCurrency(remove).setAmount(BigDecimal.ZERO).setPlugin(EcoToolPlugin.getInstance()).build());
    }

}
