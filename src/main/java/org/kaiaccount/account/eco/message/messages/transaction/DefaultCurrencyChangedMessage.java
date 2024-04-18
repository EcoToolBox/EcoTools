package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.currency.Currency;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DefaultCurrencyChangedMessage extends AbstractMessage {


    @Override
    public @NotNull String getDefaultMessage() {
        return "Default currency swapped from %previous " + MessageArgumentTypes.CURRENCY_ID.getDefaultArgumentHandler() + "% to %new "
                + MessageArgumentTypes.CURRENCY_ID.getDefaultArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        Collection<MessageArgument<?>> messageArguments = new LinkedList<>();
        messageArguments.addAll(MessageArgumentTypes.getArguments(Currency.class, type -> "previous " + type.getDefaultArgumentHandler()).toList());
        messageArguments.addAll(MessageArgumentTypes.getArguments(Currency.class, type -> "new " + type.getDefaultArgumentHandler()).toList());
        return messageArguments;
    }

    public @NotNull String getProcessedMessage(@Nullable Currency<?> previous, @NotNull Currency<?> now) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        Collection<String> overrideId = null;
        if (previous == null) {
            overrideId = MessageArgumentTypes
                    .getArguments(Currency.class, type -> "previous " + type.getDefaultArgumentHandler())
                    .map(MessageArgument::getArgumentHandler)
                    .toList();
        } else {
            MessageArgumentTypes
                    .getArguments(Currency.class, type -> "previous " + type.getDefaultArgumentHandler())
                    .forEach(adapter -> map.put(adapter, previous));
        }
        MessageArgumentTypes.getArguments(Currency.class, type -> "new " + type.getDefaultArgumentHandler()).forEach(adapter -> map.put(adapter, previous));
        String message = getProcessedMessage(map);

        if (previous == null) {
            for (String id : overrideId) {
                message = message.replaceAll(id, "Unknown");
            }
        }
        return message;
    }
}
