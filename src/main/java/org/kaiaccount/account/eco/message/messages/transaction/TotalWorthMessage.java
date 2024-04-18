package org.kaiaccount.account.eco.message.messages.transaction;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.Message;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.eco.message.type.generic.GenericMessageArgumentType;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TotalWorthMessage extends AbstractMessage implements Message {

    public static MessageArgument<BigDecimal> TOTAL_WORTH = new MessageArgument<>(new GenericMessageArgumentType<>("worth", BigDecimal.class));

    public TotalWorthMessage(@Nullable String message) {
        super(message);
    }

    @NotNull
    @Override
    public String getDefaultMessage() {
        return "Total Worth: %worth%";
    }

    @NotNull
    @Override
    public Collection<MessageArgument<?>> getArguments() {
        Collection<MessageArgument<?>> list = new LinkedList<>();
        list.add(TOTAL_WORTH);
        list.addAll(MessageArgumentTypes.getArguments(CommandSender.class).toList());
        return list;
    }

    public String getProcessedMessage(@NotNull CommandSender commandSender, @NotNull BigDecimal totalWorth) {
        Map<MessageArgument<?>, Object> assigns = new HashMap<>();
        assigns.put(TOTAL_WORTH, totalWorth);
        MessageArgumentTypes.<CommandSender>getArguments(CommandSender.class).forEach(argument -> assigns.put(argument, commandSender));
        return this.getProcessedMessage(assigns);
    }
}
