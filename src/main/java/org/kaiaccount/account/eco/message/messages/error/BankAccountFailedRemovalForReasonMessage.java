package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.type.Account;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BankAccountFailedRemovalForReasonMessage extends AbstractMessage {

    private final MessageArgument<String> REASON = new MessageArgument<>(MessageArgumentTypes.REASON);

    @Override
    public @NotNull String getDefaultMessage() {
        return "Failed to remove account. No money has been transferred, cancelling transaction: Failed for %" + REASON.getArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        Collection<MessageArgument<?>> arguments = new LinkedList<>(MessageArgumentTypes.getArguments(Account.class).toList());
        arguments.add(REASON);
        return arguments;
    }

    public @NotNull String getProcessedMessage(@NotNull Account account, @NotNull String reason) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.put(REASON, reason);
        MessageArgumentTypes.<Account>getArguments(Account.class).forEach(argument -> map.put(argument, account));
        return getProcessedMessage(map);
    }
}
