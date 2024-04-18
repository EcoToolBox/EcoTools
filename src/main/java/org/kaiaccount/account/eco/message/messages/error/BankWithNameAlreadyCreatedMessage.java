package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BankWithNameAlreadyCreatedMessage extends AbstractMessage {
    private static final String BANK_NAME_ID = "bank name";

    private static final MessageArgument<String> BANK_NAME = new MessageArgument<>(MessageArgumentTypes.NAME, BANK_NAME_ID);

    @Override
    public @NotNull String getDefaultMessage() {
        return BANK_NAME_ID + " has already been created";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return List.of(BANK_NAME);
    }

    public @NotNull String getProcessedMessage(@NotNull String bankName) {
        return this.getProcessedMessage(Map.of(BANK_NAME, bankName));
    }
}
