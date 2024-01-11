package org.kaiaccount.account.eco.account.history;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.eco.account.EcoAccount;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.Transaction;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EntryTransactionHistoryBuilder {

    private EcoAccount<?> account;
    private Currency<?> currency;
    private BigDecimal decimal;
    private String pluginName;
    private TransactionType type;
    private LocalDateTime time;
    private @Nullable String fromName;
    private @Nullable String reason;

    @CheckReturnValue
    public SimpleEntryTransactionHistory build() {
        return new SimpleEntryTransactionHistory(this);
    }

    public EcoAccount<?> getAccount() {
        return account;
    }

    public EntryTransactionHistoryBuilder setAccount(@NotNull EcoAccount<?> account) {
        this.account = account;
        return this;
    }

    public Currency<?> getCurrency() {
        return currency;
    }

    public EntryTransactionHistoryBuilder setCurrency(@NotNull Currency<?> currency) {
        this.currency = currency;
        return this;
    }

    public BigDecimal getAmount() {
        return decimal;
    }

    public EntryTransactionHistoryBuilder setAmount(double amount) {
        return this.setAmount(BigDecimal.valueOf(amount));
    }

    public EntryTransactionHistoryBuilder setAmount(@NotNull BigDecimal decimal) {
        this.decimal = decimal;
        return this;
    }

    public String getPluginName() {
        return pluginName;
    }

    public EntryTransactionHistoryBuilder setPluginName(@NotNull String pluginName) {
        this.pluginName = pluginName;
        return this;
    }

    public TransactionType getType() {
        return type;
    }

    public EntryTransactionHistoryBuilder setType(@NotNull TransactionType type) {
        this.type = type;
        return this;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public EntryTransactionHistoryBuilder setTime(@NotNull LocalDateTime time) {
        this.time = time;
        return this;
    }

    public @Nullable String getFromName() {
        return fromName;
    }

    public EntryTransactionHistoryBuilder setFromName(@Nullable String fromName) {
        this.fromName = fromName;
        return this;
    }

    public @Nullable String getReason() {
        return reason;
    }

    public EntryTransactionHistoryBuilder setReason(@Nullable String reason) {
        this.reason = reason;
        return this;
    }

    public EntryTransactionHistoryBuilder fromPayment(@NotNull Payment payment) {
        this.fromName = payment.getFrom().map(NamedAccountLike::getAccountName).orElse(null);
        this.reason = payment.getReason().orElse(null);
        this.pluginName = payment.getPlugin().getName();
        this.decimal = payment.getAmount();
        this.currency = payment.getCurrency();
        return this;
    }

    public EntryTransactionHistoryBuilder fromTransaction(@NotNull Transaction transaction) {
        fromPayment(transaction.getPayment());
        this.time = transaction.getTime();
        this.type = transaction.getType();
        if (transaction.getTarget() instanceof EcoAccount<?> eco) {
            this.account = eco;
        }
        return this;
    }
}
