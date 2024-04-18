package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TransactionFailedForReasonMessage extends AbstractMessage {

    private final MessageArgument<String> REASON = new MessageArgument<>(MessageArgumentTypes.REASON);

    @Override
    public @NotNull String getDefaultMessage() {
        return "Transaction failed for %" + REASON.getArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        Collection<MessageArgument<?>> arguments = new LinkedList<>();
        arguments.add(REASON);
        return arguments;
    }

    public @NotNull String getProcessedMessage(@NotNull String reason) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.put(REASON, reason);
        return getProcessedMessage(map);
    }
}
