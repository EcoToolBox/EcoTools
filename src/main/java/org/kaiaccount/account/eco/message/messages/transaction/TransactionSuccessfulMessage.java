package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;

import java.util.Collection;
import java.util.Collections;

public class TransactionSuccessfulMessage extends AbstractMessage {
    @Override
    public @NotNull String getDefaultMessage() {
        return "Transaction Successful";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return Collections.emptyList();
    }

    public String getProcessedMessage() {
        return this.getProcessedMessage(Collections.emptyMap());
    }
}
