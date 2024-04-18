package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.currency.Currency;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrencyExchangeValueSetMessage extends AbstractMessage {

    private static final MessageArgument<BigDecimal> EXCHANGE_VALUE = new MessageArgument<>(MessageArgumentTypes.RAW_EXCHANGE_VALUE, "new exchange value");

    @Override
    public @NotNull String getDefaultMessage() {
        return "Set the exchange value for %" + MessageArgumentTypes.CURRENCY_ID.getDefaultArgumentHandler() + "% as %" + EXCHANGE_VALUE.getArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return new LinkedList<>(MessageArgumentTypes.getArguments(Currency.class).toList());
    }

    public @NotNull String getProcessedMessage(@NotNull Currency<?> currency, BigDecimal newValue) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.putAll(MessageArgumentTypes.<Currency<?>>getArguments(Currency.class).collect(Collectors.toMap(t -> t, t -> currency)));
        map.put(EXCHANGE_VALUE, newValue);
        return getProcessedMessage(map);
    }
}
