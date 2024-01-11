package org.kaiaccount.account.eco.utils;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.utils.function.ThrowableSupplier;

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
}
