package org.kaiaccount.account.eco.message.type.currency.payment;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.type.MessageArgumentType;
import org.kaiaccount.account.inter.transfer.payment.Payment;

public class PaymentAmountArgumentType implements MessageArgumentType<Payment> {
    @Override
    public @NotNull String getDefaultArgumentHandler() {
        return "payment amount";
    }

    @Override
    public @NotNull String apply(Payment input) {
        return input.getCurrency().formatSymbol(input.getAmount());
    }

    @Override
    public @NotNull Class<Payment> getClassType() {
        return Payment.class;
    }
}
