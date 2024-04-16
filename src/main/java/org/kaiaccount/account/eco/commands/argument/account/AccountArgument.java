package org.kaiaccount.account.eco.commands.argument.account;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.inter.type.Account;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.MappedArgumentWrapper;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

public class AccountArgument<A extends Account> implements CommandArgument<A> {

    private final String id;
    private final Collection<CommandArgument<? extends A>> arguments = new LinkedTransferQueue<>();

    @Deprecated
    public AccountArgument(String id) {
        throw new RuntimeException("Arguments must be passed in");
    }

    public AccountArgument(String id, CommandArgument<? extends A>... arguments) {
        this(id, List.of(arguments));
    }

    public AccountArgument(String id, Collection<CommandArgument<? extends A>> argument) {
        if (argument.isEmpty()) {
            throw new RuntimeException("Arguments must be passed in");
        }
        this.id = id;
        this.arguments.addAll(argument);
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<A> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument) throws ArgumentException {
        if (context.getCommand().length < argument.getArgumentIndex() + 1) {
            throw new ArgumentException("Not enough arguments");
        }

        String peek = argument.getFocusArgument();

        for (CommandArgument<? extends A> arg : this.arguments) {
            if (!arg.getId().equalsIgnoreCase(peek)) {
                continue;
            }
            CommandArgumentResult<? extends A> result = arg.parse(context, argument);
            return new CommandArgumentResult<>(result.getPosition(), result.value());
        }
        throw new ArgumentException("Unknown account type of " + peek);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext argument) {
        String[] remaining = argument.getRemainingArguments();
        String peek = argument.getFocusArgument().toLowerCase();

        if (remaining.length == 1) {
            return this.arguments.stream().map(CommandArgument::getId).filter(id -> id.toLowerCase().startsWith(peek)).collect(Collectors.toSet());
        }
        Optional<CommandArgument<? extends A>> opArgument = this.arguments.parallelStream().filter(arg -> arg.getId().equalsIgnoreCase(peek)).findAny();
        return opArgument.map(commandArgument -> commandArgument.suggest(commandContext, argument)).orElse(Collections.emptyList());
    }

    public static AccountArgument<Account> allAccounts(String id) {
        return new AccountArgument<>(id,
                new NamedAccountArgument("account"),
                PlayerBankArgument.allPlayerBanks("bank"),
                new MappedArgumentWrapper<>(UserArgument.allButSource("player"), user -> AccountInterface.getManager().getPlayerAccount(user)));
    }
}
