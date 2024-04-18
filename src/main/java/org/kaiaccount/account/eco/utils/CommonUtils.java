package org.kaiaccount.account.eco.utils;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.utils.function.ThrowableSupplier;
import org.kaiaccount.account.inter.transfer.Transaction;
import org.kaiaccount.account.inter.transfer.TransactionBuilder;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.successful.SingleSuccessfulTransactionResult;
import org.kaiaccount.account.inter.type.Account;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CommonUtils {

    private CommonUtils() {
        throw new RuntimeException("Dont do that");
    }

    public static <Value> Value tryElse(ThrowableSupplier<Value, Throwable> getter, Function<Throwable, Value> fail) {
        try {
            return getter.get();
        } catch (Throwable e) {
            return fail.apply(e);
        }
    }

    public static <Value, T extends Throwable> Value tryGet(@NotNull ThrowableSupplier<Value, T> getter) throws T {
        return tryGet(getter::get, t -> (T) t);
    }

    public static <Value, T extends Throwable> Value tryGet(@NotNull ThrowableSupplier<Value, Throwable> getter, @NotNull Function<Throwable, T> map) throws T {
        try {
            return getter.get();
        } catch (Throwable e) {
            throw map.apply(e);
        }
    }

    public static BigDecimal calculate(@NotNull Iterator<BigDecimal> bigDecimals,
                                       @NotNull BiFunction<BigDecimal, BigDecimal, BigDecimal> function) {
        if (!bigDecimals.hasNext()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = bigDecimals.next();
        while (bigDecimals.hasNext()) {
            total = function.apply(total, bigDecimals.next());
        }
        return total;
    }

    public static BigDecimal sumOf(@NotNull Iterator<BigDecimal> bigDecimals) {
        return calculate(bigDecimals, BigDecimal::add);
    }

    public static TransactionResult setOverrideResult(@NotNull Account account, @NotNull Payment payment) {
        BigDecimal originalBalance = account.getBalance(payment.getCurrency());
        BigDecimal newBalance = payment.getAmount();
        BigDecimal balanceDifference = originalBalance.subtract(newBalance);

        TransactionType transactionType = TransactionType.WITHDRAW;
        if (balanceDifference.compareTo(BigDecimal.ZERO) < 0) {
            transactionType = TransactionType.DEPOSIT;
            balanceDifference = BigDecimal.ZERO.subtract(balanceDifference);
        }

        Payment displayPayment = payment.toBuilder().setAmount(balanceDifference).build();

        Transaction transaction = new TransactionBuilder().setType(transactionType).setPayment(displayPayment).build();
        return new SingleSuccessfulTransactionResult(transaction);
    }

    public static <R, A extends Account> R redirectSet(@NotNull A account, Payment payment, BiFunction<A, Payment, R> deposit, BiFunction<A, Payment, R> withdraw) {
        BigDecimal currentBalance = account.getBalance(payment.getCurrency());
        BigDecimal balance = payment.getAmount();

        BigDecimal difference = currentBalance.subtract(balance);
        if (difference.compareTo(BigDecimal.ZERO) < 0) {
            difference = BigDecimal.ZERO.subtract(difference); //minus a minus number
            payment = payment.toBuilder().setAmount(difference).build();
            return deposit.apply(account, payment);
        }
        payment = payment.toBuilder().setAmount(difference).build();
        return withdraw.apply(account, payment);
    }
}
