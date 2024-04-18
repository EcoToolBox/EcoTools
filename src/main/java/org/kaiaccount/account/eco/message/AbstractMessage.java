package org.kaiaccount.account.eco.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.eco.message.type.MessageArgument;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractMessage implements Message {

    private @Nullable String message;

    public AbstractMessage() {
        this(null);
    }

    public AbstractMessage(@Nullable String message) {
        this.message = message;
    }

    protected <O, R extends O> @NotNull Stream<MessageArgument<R>> getMessageArgumentIdFor(O value) {
        Collection<MessageArgument<?>> arguments = this.getArguments();
        String message = this.getOverridingMessageElse();
        return arguments
                .stream()
                .filter(argument -> message.contains("%" + argument.getArgumentHandler() + "%"))
                .filter(argument -> argument.getClassType().isInstance(value))
                .map(argument -> (MessageArgument<R>) argument);
    }

    @NotNull
    @Override
    public Optional<String> getOverridingMessage() {
        return Optional.ofNullable(this.message);
    }

    @Override
    public void setOverridingMessage(@Nullable String message) {
        this.message = message;
    }

    protected @NotNull String getProcessedMessage(@NotNull String message, @NotNull Map<MessageArgument<?>, Object> values) {
        for (Map.Entry<MessageArgument<?>, Object> entry : values.entrySet()) {
            String targetId = "%" + entry.getKey().getArgumentHandler() + "%";
            if (message.contains(targetId)) {
                message = replace(message, entry.getKey(), entry.getValue());
            }
        }
        return message;
    }

    protected @NotNull String getProcessedMessage(@NotNull Map<MessageArgument<?>, Object> values) {
        return this.getProcessedMessage(this.getOverridingMessageElse(), values);
    }

    private <T> @NotNull String process(MessageArgument<T> argument, Object value) {
        return argument.apply((T) value);
    }

    private <T> String replace(String message, MessageArgument<T> argument, Object value) {
        return message.replaceAll("%" + argument.getArgumentHandler() + "%", argument.apply((T) value));
    }
}
