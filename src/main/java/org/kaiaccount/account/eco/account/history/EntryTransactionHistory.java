package org.kaiaccount.account.eco.account.history;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.account.EcoAccount;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EntryTransactionHistory extends Comparable<EntryTransactionHistory> {

    EcoAccount<?> getAttachedAccount();

    Currency<?> getCurrency();

    BigDecimal getAmount();

    String getPluginName();

    Optional<String> getFromName();

    Optional<String> getReason();

    TransactionType getTransactionType();

    LocalDateTime getTime();

    default Optional<Plugin> getPlugin() {
        return Optional.ofNullable(Bukkit.getPluginManager().getPlugin(getPluginName()));
    }

    @Override
    default int compareTo(@NotNull EntryTransactionHistory entryTransactionHistory) {
        return getTime().compareTo(entryTransactionHistory.getTime());
    }


}
