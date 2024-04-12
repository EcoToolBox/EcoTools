package org.kaiaccount.account.eco.utils.function;

public interface ThrowableSupplier<Value, T extends Throwable> {

    Value get() throws T;
}
