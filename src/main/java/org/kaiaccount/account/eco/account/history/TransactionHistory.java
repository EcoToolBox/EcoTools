package org.kaiaccount.account.eco.account.history;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.kaiaccount.account.eco.account.EcoAccount;
import org.kaiaccount.account.eco.utils.SortedList;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionHistory extends SortedList<EntryTransactionHistory> {

    private final EcoAccount<?> target;

    public TransactionHistory(EcoAccount<?> target) {
        this(target, Collections.emptyList());
    }

    public TransactionHistory(EcoAccount<?> target, Collection<EntryTransactionHistory> collection) {
        super(collection, Comparator.naturalOrder());
        this.target = target;
    }

    @CheckReturnValue
    public @NotNull EcoAccount<?> getAccount() {
        return this.target;
    }


    @CheckReturnValue
    @UnmodifiableView
    public List<EntryTransactionHistory> getBetween(@NotNull ChronoLocalDateTime<LocalDate> start, @NotNull ChronoLocalDateTime<LocalDate> end) {
        return this
                .parallelStream()
                .filter(history -> history.getTime().isAfter(start))
                .filter(history -> history.getTime().isBefore(end))
                .toList();
    }

    @Override
    public boolean add(EntryTransactionHistory entryTransactionHistory) {
        if (!this.getAccount().equals(entryTransactionHistory.getAttachedAccount())) {
            return false;
        }
        return super.add(entryTransactionHistory);
    }

    @Override
    public boolean addAll(Collection<? extends EntryTransactionHistory> c) {
        if (!c.stream().map(EntryTransactionHistory::getAttachedAccount).allMatch(account -> account.equals(getAccount()))) {
            return false;
        }
        return super.addAll(c);
    }
}
