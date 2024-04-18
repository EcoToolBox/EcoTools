package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.currency.Currency;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CurrencyFailedRemovalForReasonMessage extends AbstractMessage {

    private final MessageArgument<String> REASON = new MessageArgument<>(MessageArgumentTypes.REASON);

    @Override
    public @NotNull String getDefaultMessage() {
        return "Failed to remove currency: Failed for %" + REASON.getArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        Collection<MessageArgument<?>> arguments = new LinkedList<>(MessageArgumentTypes.getArguments(Currency.class).toList());
        arguments.add(REASON);
        return arguments;
    }

    public @NotNull String getProcessedMessage(@NotNull Currency<?> currency, @NotNull String reason) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.put(REASON, reason);
        MessageArgumentTypes.<Currency<?>>getArguments(Currency.class).forEach(argument -> map.put(argument, currency));
        return getProcessedMessage(map);
    }
}
