package org.kaiaccount.account.eco.commands.argument.date;

import org.jetbrains.annotations.NotNull;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.ParseCommandArgument;
import org.mose.command.arguments.operation.SuggestionArgument;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateRangeArgument implements CommandArgument<Duration> {

    private final String id;
    private final ParseCommandArgument<? extends Temporal> compare;
    private final ParseCommandArgument<? extends Temporal> acceptance;
    private final DateRange[] ranges;

    public <T extends Temporal> DateRangeArgument(String id, ParseCommandArgument<T> compare, int offset) {
        this(id, compare, offset, DateRange.values());
    }

    public <T extends Temporal> DateRangeArgument(String id, ParseCommandArgument<T> compare, int offset, DateRange... ranges) {
        this(id, compare, offset, new DateTimeArgument(id), ranges);
    }

    public <T extends Temporal> DateRangeArgument(String id, ParseCommandArgument<T> compare, int argumentOffset, ParseCommandArgument<? extends Temporal> acceptance, DateRange... ranges) {
        this(id, new ParseCommandArgument<T>() {

            @Override
            public @NotNull CommandArgumentResult<T> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument) throws ArgumentException {
                return compare.parse(context, new ArgumentContext(argument.getArgumentIndex() + argumentOffset, context.getCommand()));
            }
        }, acceptance, ranges);
    }

    public DateRangeArgument(String id, ParseCommandArgument<? extends Temporal> compare, ParseCommandArgument<? extends Temporal> acceptance, DateRange... ranges) {
        this.id = id;
        this.compare = compare;
        this.acceptance = acceptance;
        this.ranges = ranges;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<Duration> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument) throws ArgumentException {
        String peek = argument.getFocusArgument();
        Temporal compare = this.compare.parse(context, argument).getValue();
        Optional<DateRange> opRange = Stream.of(this.ranges).filter(range -> range.name().equalsIgnoreCase(peek)).findAny();
        if (opRange.isPresent()) {
            return CommandArgumentResult.from(argument, opRange.get().get(compare));
        }
        @NotNull CommandArgumentResult<? extends Temporal> ending = this.acceptance.parse(context, argument);
        return new CommandArgumentResult<>(ending.getPosition(), Duration.between(compare, ending.getValue()));

    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext argument) {
        Collection<String> suggestions = new ArrayList<>();
        if (this.acceptance instanceof SuggestionArgument<? extends Temporal> suggestionArgument) {
            suggestions.addAll(suggestionArgument.suggest(commandContext, argument));
        }
        String peek = argument.getFocusArgument().toUpperCase();
        List<String> rangeSuggestions = Stream.of(this.ranges).filter(range -> range.name().startsWith(peek)).map(range -> range.name().toLowerCase()).collect(Collectors.toList());
        suggestions.addAll(rangeSuggestions);

        return suggestions;
    }
}
