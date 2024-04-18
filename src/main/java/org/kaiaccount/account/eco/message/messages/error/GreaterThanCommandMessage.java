package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreaterThanCommandMessage extends AbstractMessage {

    private static final MessageArgument<String> SOURCE_TYPE = new MessageArgument<>(MessageArgumentTypes.SOURCE_TYPE);
    private static final MessageArgument<Double> GREATER_THAN = new MessageArgument<>(MessageArgumentTypes.NUMBER, "than");

    @NotNull
    @Override
    public String getDefaultMessage() {
        return "%" + SOURCE_TYPE.getArgumentHandler() + "% must be greater than %" + GREATER_THAN.getArgumentHandler() + "%";
    }

    @NotNull
    @Override
    public Collection<MessageArgument<?>> getArguments() {
        return List.of(SOURCE_TYPE, GREATER_THAN);
    }

    public @NotNull String getProcessedMessage(@NotNull String sourceType, double number) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.put(SOURCE_TYPE, sourceType);
        map.put(GREATER_THAN, number);
        return this.getProcessedMessage(map);
    }
}
