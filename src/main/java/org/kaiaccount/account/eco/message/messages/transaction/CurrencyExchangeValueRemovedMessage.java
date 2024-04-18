package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.currency.Currency;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrencyExchangeValueRemovedMessage extends AbstractMessage {
    @Override
    public @NotNull String getDefaultMessage() {
        return "Removed the exchange value for %" + MessageArgumentTypes.CURRENCY_ID.getDefaultArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return new LinkedList<>(MessageArgumentTypes.getArguments(Currency.class).toList());
    }

    public @NotNull String getProcessedMessage(@NotNull Currency<?> currency) {
        Map<MessageArgument<?>, Object> map = MessageArgumentTypes.<Currency<?>>getArguments(Currency.class).collect(Collectors.toMap(t -> t, t -> currency));
        return getProcessedMessage(map);
    }
}
