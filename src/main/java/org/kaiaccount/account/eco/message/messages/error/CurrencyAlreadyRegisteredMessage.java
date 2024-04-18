package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CurrencyAlreadyRegisteredMessage extends AbstractMessage {
    @Override
    public @NotNull String getDefaultMessage() {
        return "Currency already registered";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return Collections.emptyList();
    }

    public @NotNull String getProcessedMessage() {
        return this.getProcessedMessage(Map.of());
    }
}
