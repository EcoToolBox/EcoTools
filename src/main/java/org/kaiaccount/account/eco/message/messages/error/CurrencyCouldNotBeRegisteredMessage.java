package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CurrencyCouldNotBeRegisteredMessage extends AbstractMessage {

    private static final MessageArgument<String> REASON = new MessageArgument<>(MessageArgumentTypes.REASON);

    @Override
    public @NotNull String getDefaultMessage() {
        return "Currency could not be registered: %" + REASON.getArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return List.of(REASON);
    }

    public @NotNull String getProcessedMessage(@NotNull String reason) {
        return this.getProcessedMessage(Map.of(REASON, reason));
    }
}
