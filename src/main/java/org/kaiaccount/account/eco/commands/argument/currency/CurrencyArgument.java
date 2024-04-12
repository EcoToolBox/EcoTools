package org.kaiaccount.account.eco.commands.argument.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.inter.currency.Currency;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class CurrencyArgument implements CommandArgument<Currency<?>> {

    private final @NotNull String id;
    private final @NotNull BiFunction<CommandContext, ArgumentContext, Collection<Currency<?>>>
            currencies;

    public CurrencyArgument(@NotNull String id) {
        this(id, (c, a) -> AccountInterface.getManager().getCurrencies());
    }

    public CurrencyArgument(@NotNull String id, @NotNull Collection<Currency<?>> currencies) {
        this(id, (c, a) -> currencies);
    }

    public CurrencyArgument(@NotNull String id,
                            @NotNull BiFunction<CommandContext, ArgumentContext, Collection<Currency<?>>> currencies) {
        this.id = id;
        this.currencies = currencies;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<Currency<?>> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument) throws ArgumentException {
        Collection<Currency<?>> currencies = this.currencies.apply(context, argument);
        String peek = argument.getFocusArgument();
        Optional<Currency<?>> opCurrency = currencies.parallelStream()
                .filter(cur -> (cur.getPlugin().getName() + "." + cur.getKeyName()).equalsIgnoreCase(peek))
                .findAny();
        if (opCurrency.isPresent()) {
            return CommandArgumentResult.from(argument, opCurrency.get());
        }
        opCurrency = currencies.parallelStream().filter(cur -> cur.getSymbol().equalsIgnoreCase(peek)).findAny();
        if (opCurrency.isPresent()) {
            return CommandArgumentResult.from(argument, opCurrency.get());
        }
        throw new ArgumentException("Cannot find currency of " + peek);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext argument) {
        String peek = argument.getFocusArgument();
        Collection<Currency<?>> currencies = this.currencies.apply(commandContext, argument);
        List<String> symbolCurrencies = currencies.parallelStream()
                .map(Currency::getSymbol)
                .filter(currency -> currency.toLowerCase().startsWith(peek.toLowerCase()))
                .sorted()
                .toList();
        List<String> nameCurrencies = currencies.parallelStream()
                .map(cur -> cur.getPlugin().getName() + "." + cur.getKeyName())
                .filter(name -> name.toLowerCase().startsWith(peek.toLowerCase()))
                .sorted()
                .toList();
        Collection<String> ret = new ArrayList<>(nameCurrencies);
        ret.addAll(symbolCurrencies);
        return ret;
    }
}
