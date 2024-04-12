package org.kaiaccount.account.eco.commands.argument.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaymentArgument implements CommandArgument<PaymentBuilder> {

    private final @NotNull String id;
    private final boolean amountNextArg;
    private final Supplier<Collection<Currency<?>>> toCurrencies;

    public PaymentArgument(@NotNull String id, boolean amountNextArg) {
        this(id, amountNextArg, () -> AccountInterface.getManager().getCurrencies());
    }

    public PaymentArgument(@NotNull String id, boolean amountNextArg,
                           @NotNull Supplier<Collection<Currency<?>>> supplier) {
        this.id = id;
        this.toCurrencies = supplier;
        this.amountNextArg = amountNextArg;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<PaymentBuilder> parse(@NotNull CommandContext commandContext, @NotNull ArgumentContext commandArgumentContext) throws ArgumentException {
        int arg = commandArgumentContext.getArgumentIndex();
        int usedArguments = 1;
        String peek = commandContext.getCommand()[arg];
        StringBuilder symbolBuilder = new StringBuilder();
        for (int i = 0; i < peek.length(); i++) {
            char at = peek.charAt(i);
            if (Character.isDigit(at)) {
                break;
            }
            symbolBuilder.append(at);
        }
        String symbol = symbolBuilder.toString();
        double amount = 0;
        try {
            amount = Double.parseDouble(peek.substring(symbol.length()));
        } catch (NumberFormatException ignored) {
        }
        Optional<Currency<?>> opCurrency = this.toCurrencies.get()
                .parallelStream()
                .filter(currency -> currency.getSymbol().equalsIgnoreCase(symbol))
                .findAny();
        if (opCurrency.isEmpty()) {
            throw new ArgumentException("Unknown currency of " + symbol);
        }

        if (peek.length() == symbol.length() && amountNextArg) {
            String[] command = commandContext.getCommand();
            if ((arg + 1) != command.length) {
                usedArguments = 2;
                try {
                    amount = Double.parseDouble(command[arg + 1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return CommandArgumentResult.from(commandArgumentContext, usedArguments,
                new PaymentBuilder().setAmount(amount).setCurrency(opCurrency.get()));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext commandArgumentContext) {
        String peek = commandArgumentContext.getFocusArgument();
        return this.toCurrencies.get()
                .stream()
                .parallel()
                .map(Currency::getSymbol)
                .filter(currency -> currency.toLowerCase().startsWith(peek.toLowerCase()))
                .collect(Collectors.toSet());
    }
}
