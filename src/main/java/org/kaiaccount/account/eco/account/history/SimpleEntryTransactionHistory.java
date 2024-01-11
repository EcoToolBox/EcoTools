package org.kaiaccount.account.eco.account.history;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.eco.account.EcoAccount;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class SimpleEntryTransactionHistory implements EntryTransactionHistory {
    private final @NotNull EcoAccount<?> account;
    private final @NotNull Currency<?> currency;
    private final @NotNull BigDecimal decimal;
    private final @NotNull String pluginName;
    private final @NotNull TransactionType type;
    private final @NotNull LocalDateTime time;
    private final @Nullable String fromName;
    private final @Nullable String reason;

    SimpleEntryTransactionHistory(@NotNull EntryTransactionHistoryBuilder builder) {
        this.account = Objects.requireNonNull(builder.getAccount(), "Account is missing");
        this.currency = Objects.requireNonNull(builder.getCurrency(), "Currency is missing");
        this.decimal = Objects.requireNonNull(builder.getAmount(), "Amount is missing");
        this.pluginName = Objects.requireNonNull(builder.getPluginName(), "Plugin name is missing");
        this.type = Objects.requireNonNull(builder.getType(), "Transaction type is missing");
        this.time = Objects.requireNonNull(builder.getTime(), "Time is missing");
        this.fromName = builder.getFromName();
        this.reason = builder.getReason();
    }

    @Override
    public EcoAccount<?> getAttachedAccount() {
        return this.account;
    }

    @Override
    public @NotNull Currency<?> getCurrency() {
        return this.currency;
    }

    @Override
    public BigDecimal getAmount() {
        return this.decimal;
    }

    @Override
    public @NotNull String getPluginName() {
        return this.pluginName;
    }

    @Override
    public Optional<String> getFromName() {
        return Optional.ofNullable(this.fromName);
    }

    @Override
    public Optional<String> getReason() {
        return Optional.ofNullable(this.reason);
    }

    @Override
    public TransactionType getTransactionType() {
        return this.type;
    }

    @Override
    public @NotNull LocalDateTime getTime() {
        return this.time;
    }
}
