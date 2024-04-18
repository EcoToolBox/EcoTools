package org.kaiaccount.account.eco.message.messages.transaction;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.transfer.Transaction;
import org.kaiaccount.account.inter.transfer.payment.Payment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ReceivedTransactionMessage extends AbstractMessage {
    @Override
    public @NotNull String getDefaultMessage() {
        return "Received %" + MessageArgumentTypes.PAYMENT_AMOUNT.getDefaultArgumentHandler() + "%";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        return new LinkedList<>(MessageArgumentTypes.getArguments(Payment.class).toList());
    }

    public String getProcessedMessage(@NotNull Transaction transaction) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        MessageArgumentTypes.getArguments(Payment.class).forEach(argument -> map.put(argument, transaction.getPayment()));
        return this.getProcessedMessage(map);
    }
}
