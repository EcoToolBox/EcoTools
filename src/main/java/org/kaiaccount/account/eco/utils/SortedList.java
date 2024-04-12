package org.kaiaccount.account.eco.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.UnaryOperator;

public class SortedList<Value> extends LinkedList<Value> {

    private final @NotNull Comparator<? super Value> compare;

    public SortedList(@NotNull Comparator<? super Value> compare) {
        this(Collections.emptyList(), compare);
    }

    public SortedList(@NotNull Collection<? extends Value> c, @NotNull Comparator<? super Value> compare) {
        super(c);
        this.compare = compare;
        super.sort(compare);
    }

    @Override
    public boolean add(Value value) {
        //this can be better with a binary search
        for (int i = 0; i < size(); i++) {
            Value targetValue = this.get(i);
            int current = this.compare.compare(targetValue, value);
            if (current <= 1) {
                super.add(i, value);
                return true;
            }
        }
        return super.add(value);
    }

    @Override
    public boolean offer(Value value) {
        //in a non-queue offer is the same as add
        return add(value);
    }

    @Override
    public void push(Value value) {
        //in a non-queue offer is the same as add
        add(value);
    }

    @Override
    public void replaceAll(UnaryOperator<Value> operator) {
        super.replaceAll(operator);
        super.sort(this.compare);
    }

    @Deprecated
    @Override
    public void sort(Comparator<? super Value> value) {
    }


    @Override
    @Deprecated
    public void addFirst(Value value) {
        this.add(value);
    }

    @Override
    @Deprecated
    public void addLast(Value value) {
        this.add(value);
    }

    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends Value> c) {
        boolean res = super.addAll(index, c);
        if (res) {
            super.sort(this.compare);
        }
        return res;
    }

    @Override
    @Deprecated
    public Value set(int index, Value element) {
        Value v = this.remove(index);
        this.add(element);
        return v;
    }

    @Override
    @Deprecated
    public void add(int index, Value element) {
        this.add(element);
    }

    @Override
    @Deprecated
    public boolean offerFirst(Value value) {
        return this.add(value);
    }

    @Override
    @Deprecated
    public boolean offerLast(Value value) {
        return this.add(value);
    }
}
