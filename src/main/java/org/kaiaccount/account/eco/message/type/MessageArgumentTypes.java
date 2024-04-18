package org.kaiaccount.account.eco.message.type;

import org.kaiaccount.account.eco.message.type.account.AccountNameArgumentType;
import org.kaiaccount.account.eco.message.type.account.AccountTypeNameArgumentType;
import org.kaiaccount.account.eco.message.type.currency.CurrencyIdMessageArgumentType;
import org.kaiaccount.account.eco.message.type.currency.CurrencyShortNameMessageArgumentType;
import org.kaiaccount.account.eco.message.type.currency.payment.PaymentAmountArgumentType;
import org.kaiaccount.account.eco.message.type.generic.GenericMessageArgumentType;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.type.Account;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MessageArgumentTypes {

    public static final MessageArgumentType<String> SOURCE_TYPE = new GenericMessageArgumentType<>("source type", String.class);
    public static final MessageArgumentType<String> NAME = new GenericMessageArgumentType<>("name", String.class);
    public static final MessageArgumentType<String> ACTION = new GenericMessageArgumentType<>("action", String.class);
    public static final CurrencyIdMessageArgumentType CURRENCY_ID = new CurrencyIdMessageArgumentType();
    public static final CurrencyShortNameMessageArgumentType CURRENCY_SHORT_NAME = new CurrencyShortNameMessageArgumentType();
    public static final AccountTypeNameArgumentType ACCOUNT_TYPE_NAME = new AccountTypeNameArgumentType();
    public static final MessageArgumentType<String> REASON = new GenericMessageArgumentType<>("reason", String.class);
    public static final MessageArgumentType<String> RAW_ACCOUNT_TYPE_NAME = new GenericMessageArgumentType<>("account type name", String.class);
    public static final MessageArgumentType<BigDecimal> RAW_EXCHANGE_VALUE = new GenericMessageArgumentType<>("exchange value",
            BigDecimal.class,
            BigDecimal::toPlainString);
    public static final MessageArgumentType<TransactionType> RAW_TRANSACTION_TYPE = new GenericMessageArgumentType<>("transaction type",
            TransactionType.class,
            t -> t.name().toLowerCase());
    public static final MessageArgumentType<Double> NUMBER = new GenericMessageArgumentType<>("number", double.class, value -> {
        if (value.intValue() == value) {
            return value.intValue() + "";
        }
        return value.doubleValue() + "";
    });
    public static final MessageArgumentType<Payment> PAYMENT_AMOUNT = new PaymentAmountArgumentType();
    public static final MessageArgumentType<Account> ACCOUNT_NAME = new AccountNameArgumentType();
    private static final List<MessageArgumentType<?>> cache = new LinkedList<>();

    private MessageArgumentTypes() {
        throw new RuntimeException("Dont do that");
    }

    public static <T> Stream<MessageArgumentType<T>> getArgumentTypes(Class<?> clazz) {
        return getArgumentTypes().parallelStream().filter(type -> type.getClassType().isAssignableFrom(clazz)).map(type -> (MessageArgumentType<T>) type);
    }

    public static Collection<MessageArgumentType<?>> getArgumentTypes() {
        if (!cache.isEmpty()) {
            return Collections.unmodifiableCollection(cache);
        }
        Collection<MessageArgumentType<?>> collection = Arrays
                .stream(MessageArgumentTypes.class.getDeclaredFields())
                .parallel()
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .filter(field -> MessageArgumentType.class.isAssignableFrom(field.getType()))
                .map(field -> {
                    try {
                        return (MessageArgumentType<?>) field.get(null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        cache.addAll(collection);
        return Collections.unmodifiableCollection(collection);
    }

    public static <T> Stream<MessageArgument<T>> getArguments(Class<?> clazz, Function<MessageArgumentType<T>, String> function) {
        Stream<MessageArgumentType<T>> stream = getArgumentTypes(clazz);
        return stream.map(type -> new MessageArgument<>(type, function.apply(type)));
    }

    public static <T> Stream<MessageArgument<T>> getArguments(Class<?> clazz) {
        Stream<MessageArgumentType<T>> stream = getArgumentTypes(clazz);
        return stream.map(type -> new MessageArgument<>(type, null));
    }
}
