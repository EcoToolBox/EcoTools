package org.kaiaccount.account.eco.commands.argument.date;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.utils.CommonUtils;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.ToIntBiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DateTimeArgument implements CommandArgument<LocalDateTime> {

    private final String id;
    private final ToIntBiFunction<CommandContext, ArgumentContext> maxYear;
    private final ToIntBiFunction<CommandContext, ArgumentContext> minYear;

    public DateTimeArgument(String id) {
        this(id, (a, b) -> LocalDateTime.MIN.getYear(), (a, b) -> LocalDateTime.MAX.getYear());
    }

    public DateTimeArgument(String id, ToIntBiFunction<CommandContext, ArgumentContext> minYear, ToIntBiFunction<CommandContext, ArgumentContext> maxYear) {
        this.id = id;
        this.minYear = minYear;
        this.maxYear = maxYear;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<LocalDateTime> parse(@NotNull CommandContext commandContext, @NotNull ArgumentContext commandArgumentContext) throws ArgumentException {
        String peek = commandArgumentContext.getFocusArgument();
        char splitChar = '/';
        if (peek.contains("\\")) {
            splitChar = '\\';
        }
        if (peek.contains(".")) {
            splitChar = '.';
        }
        String[] peekSplit = peek.split(Pattern.quote(splitChar + ""));
        if (peekSplit.length < 3) {
            throw new ArgumentException("Date required: DD/MM/YYYY");
        }

        int day = CommonUtils.tryGet(() -> Integer.parseInt(peekSplit[0]), ArgumentException::new);
        int month = CommonUtils.tryGet(() -> Integer.parseInt(peekSplit[1]), ArgumentException::new);
        int year = CommonUtils.tryGet(() -> Integer.parseInt(peekSplit[2]), ArgumentException::new);
        int hour = CommonUtils.tryElse(() -> Integer.parseInt(peekSplit[3]), (e) -> 0);
        int min = CommonUtils.tryElse(() -> Integer.parseInt(peekSplit[4]), e -> 0);
        int second = CommonUtils.tryElse(() -> Integer.parseInt(peekSplit[5]), e -> 0);

        return CommandArgumentResult.from(commandArgumentContext, LocalDateTime.of(year, month, day, hour, min, second));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext commandArgumentContext) {
        String peek = commandArgumentContext.getFocusArgument();
        char splitChar = '/';
        if (peek.contains("\\")) {
            splitChar = '\\';
        }
        if (peek.contains(".")) {
            splitChar = '.';
        }

        boolean isEndingCategory = peek.endsWith(splitChar + "");
        String[] peekSplit = peek.split(Pattern.quote(splitChar + ""));
        int peekSplitLength = isEndingCategory ? peekSplit.length + 1 : peekSplit.length;
        int max = switch (peekSplitLength) {
            case 0, 1 -> 32; //day
            case 2 -> 13; //month
            case 3 -> this.maxYear.applyAsInt(commandContext, commandArgumentContext) + 1; //year
            case 4 -> 25; //hour
            case 5 -> 61; //min
            case 6 -> 61; //seconds
            default -> 0;
        };

        String numberPeek = isEndingCategory ? "" : peekSplit[peekSplit.length - 1];
        int min = peekSplitLength == 3 ? this.minYear.applyAsInt(commandContext, commandArgumentContext) : 1;

        final char finalSplitChar = splitChar;
        String currentInput = peek;
        if (!isEndingCategory && peekSplit.length > 1) {
            peekSplit[peekSplit.length - 1] = "";
            currentInput = String.join(splitChar + "", peekSplit);
            if (!currentInput.endsWith(splitChar + "")) {
                currentInput = currentInput + splitChar;
            }
        }
        if (!isEndingCategory && peekSplit.length == 1) {
            currentInput = "";
        }

        String finalCurrentInput = currentInput;

        if (min >= 2000 && max <= 3000) {
            //year override
            return IntStream
                    .range(min, max)
                    .boxed()
                    .map(year -> year + "" + finalSplitChar)
                    .flatMap(year -> Stream.of(year, year.substring(2, 4)))
                    .filter(res -> res.startsWith(numberPeek))
                    .map(res -> finalCurrentInput + res)
                    .collect(Collectors.toList());
        }
        return IntStream
                .range(min, max)
                .boxed()
                .map(num -> num + (peekSplitLength == 6 ? "" : "" + finalSplitChar))
                .filter(res -> res.startsWith(numberPeek))
                .map(res -> finalCurrentInput + res)
                .collect(Collectors.toList());
    }
}
