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

public class ExchangeStartedMessage extends AbstractMessage {

    private static final MessageArgument<BigDecimal> AMOUNT = new MessageArgument<>(MessageArgumentTypes.RAW_EXCHANGE_VALUE, "amount");

    @Override
    public @NotNull String getDefaultMessage() {
        return "exchanging %from " + MessageArgumentTypes.CURRENCY_ID.getDefaultArgumentHandler() + "%%" + AMOUNT.getArgumentHandler() + "% to %to "
                + MessageArgumentTypes.CURRENCY_ID.getDefaultArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        Collection<MessageArgument<?>> messageArguments = new LinkedList<>();
        messageArguments.add(AMOUNT);
        messageArguments.addAll(MessageArgumentTypes.<Currency<?>>getArguments(Currency.class, t -> "to " + t.getDefaultArgumentHandler()).toList());
        messageArguments.addAll(MessageArgumentTypes.<Currency<?>>getArguments(Currency.class, t -> "from " + t.getDefaultArgumentHandler()).toList());
        return messageArguments;
    }

    public @NotNull String getProcessedMessage(@NotNull Currency<?> from, @NotNull Currency<?> to, @NotNull BigDecimal amount) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.put(AMOUNT, amount);
        MessageArgumentTypes
                .<Currency<?>>getArguments(Currency.class, t -> "from " + t.getDefaultArgumentHandler())
                .forEach(argument -> map.put(argument, from));
        MessageArgumentTypes.<Currency<?>>getArguments(Currency.class, t -> "to " + t.getDefaultArgumentHandler()).forEach(argument -> map.put(argument, to));
        return getProcessedMessage(map);
    }
}
