package org.kaiaccount.account.eco.commands.argument.date;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.function.BiFunction;

public enum DateRange {

    SECOND((date, amount) -> date.plus(Duration.ofSeconds(amount))),
    MINUTE((date, amount) -> date.plus(Duration.ofMinutes(amount))),
    HOUR((date, amount) -> date.plus(Duration.ofHours(amount))),
    DAY((date, amount) -> date.plus(Duration.ofDays(amount))),
    WEEK((date, amount) -> date.plus(Duration.ofDays(7L * amount))),
    MONTH((date, amount) -> date.plus(amount, ChronoUnit.MONTHS)),
    YEAR((date, amount) -> date.plus(amount, ChronoUnit.YEARS));

    private final @NotNull BiFunction<Temporal, Integer, Temporal> plus;

    DateRange(@NotNull BiFunction<Temporal, Integer, Temporal> toDuration) {
        this.plus = toDuration;
    }

    public Duration get(Temporal date) {
        return this.get(date, 1);
    }

    public Duration get(Temporal date, int amount) {
        Temporal result = this.plus.apply(date, amount);
        return Duration.between(date, result);
    }
}
