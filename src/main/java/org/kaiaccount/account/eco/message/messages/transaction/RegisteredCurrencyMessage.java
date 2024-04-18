package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;

import java.util.Collection;
import java.util.Collections;

public class RegisteredCurrencyMessage extends AbstractMessage {
    @Override
    public @NotNull String getDefaultMessage() {
        return "Registered Currency";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return Collections.emptyList();
    }

    public @NotNull String getProcessedMessage() {
        return this.getProcessedMessage(Collections.emptyMap());
    }
}
