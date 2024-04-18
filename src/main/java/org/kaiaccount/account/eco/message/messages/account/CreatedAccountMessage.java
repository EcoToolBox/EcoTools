package org.kaiaccount.account.eco.message.messages.account;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CreatedAccountMessage extends AbstractMessage {

    public static final String BANK = "Bank";
    public static final String NAMED = "Named";
    private static final String SOURCE_TYPE_ID = "source type";
    private static final String ACCOUNT_NAME_ID = "account name";
    private static final MessageArgument<String> SOURCE_TYPE = new MessageArgument<>(MessageArgumentTypes.SOURCE_TYPE, SOURCE_TYPE_ID);
    private static final MessageArgument<String> ACCOUNT_NAME = new MessageArgument<>(MessageArgumentTypes.NAME, ACCOUNT_NAME_ID);

    @Override
    public @NotNull String getDefaultMessage() {
        return "Created " + SOURCE_TYPE_ID + " with the name of " + ACCOUNT_NAME_ID;
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return List.of(SOURCE_TYPE, ACCOUNT_NAME);
    }

    public @NotNull String getProcessedMessage(@NotNull String sourceType, @NotNull String accountName) {
        return this.getProcessedMessage(Map.of(SOURCE_TYPE, sourceType, ACCOUNT_NAME, accountName));
    }
}
