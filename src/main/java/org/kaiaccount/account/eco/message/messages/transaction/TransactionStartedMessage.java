package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.type.Account;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TransactionStartedMessage extends AbstractMessage {

    private static final MessageArgument<TransactionType> TRANSACTION_TYPE = new MessageArgument<>(MessageArgumentTypes.RAW_TRANSACTION_TYPE);

    @Override
    public @NotNull String getDefaultMessage() {
        return "%" + TRANSACTION_TYPE.getArgumentHandler() + "%ing %" + MessageArgumentTypes.PAYMENT_AMOUNT.getDefaultArgumentHandler() + "% to %"
                + MessageArgumentTypes.ACCOUNT_NAME + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        Collection<MessageArgument<?>> messageArguments = new LinkedList<>();
        messageArguments.add(TRANSACTION_TYPE);
        messageArguments.addAll(MessageArgumentTypes.<Payment>getArguments(Payment.class).toList());
        messageArguments.addAll(MessageArgumentTypes.getArguments(Account.class, t -> "to " + t.getDefaultArgumentHandler()).toList());
        return messageArguments;
    }

    public @NotNull String getProcessedMessage(@NotNull TransactionType action, @NotNull Payment payment, @NotNull Account to) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.put(TRANSACTION_TYPE, action);
        MessageArgumentTypes.<Payment>getArguments(Payment.class).forEach(argument -> map.put(argument, payment));
        MessageArgumentTypes.<Account>getArguments(Account.class, t -> "to " + t.getDefaultArgumentHandler()).forEach(argument -> map.put(argument, to));
        return getProcessedMessage(map);
    }
}
