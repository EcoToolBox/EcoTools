package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.currency.Currency;

import java.util.*;

public class CurrencyUnregisteredMessage extends AbstractMessage {
    @Override
    public @NotNull String getDefaultMessage() {
        return "%" + MessageArgumentTypes.CURRENCY_ID.getDefaultArgumentHandler() + "% has been removed";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return new LinkedList<>(MessageArgumentTypes.<Currency<?>>getArguments(Currency.class).toList());
    }

    public @NotNull String getProcessedMessage(@NotNull Currency<?> currency) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        MessageArgumentTypes.<Currency<?>>getArguments(Currency.class).forEach(argument -> map.put(argument, currency));
        return this.getProcessedMessage(map);
    }
}
