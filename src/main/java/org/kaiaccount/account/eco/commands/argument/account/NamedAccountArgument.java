package org.kaiaccount.account.eco.commands.argument.account;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import org.mose.command.context.CommandArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.io.IOException;
import java.util.Collection;
import java.util.function.BiFunction;

public class NamedAccountArgument implements CommandArgument<NamedAccount> {

    private final String id;
    private final BiFunction<CommandContext, ArgumentContext, Collection<NamedAccount>> function;


    public NamedAccountArgument(String id) {
        this(id, ((commandContext, namedAccountCommandArgumentContext) -> AccountInterface.getManager().getNamedAccounts()));
    }

    public NamedAccountArgument(String id, BiFunction<CommandContext, ArgumentContext, Collection<NamedAccount>> function) {
        this.id = id;
        this.function = function;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<NamedAccount> parse(@NotNull CommandContext commandContext, @NotNull ArgumentContext commandArgumentContext) throws ArgumentException {
        Collection<NamedAccount> accounts = this.function.apply(commandContext, commandArgumentContext);
        String peek = commandArgumentContext.getFocusArgument().toLowerCase();

        NamedAccount namedAccount = accounts
                .parallelStream()
                .filter(account -> account.getAccountName().equalsIgnoreCase(peek))
                .findAny()
                .orElseThrow(() -> new ArgumentException("No account by that name"));
        return CommandArgumentResult.from(commandArgumentContext, namedAccount);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext argument) {
        Collection<NamedAccount> accounts = this.function.apply(commandContext, argument);
        String peek = argument.getFocusArgument().toLowerCase();
        return accounts.parallelStream()
                .map(NamedAccountLike::getAccountName)
                .filter(name -> name.toLowerCase().startsWith(peek))
                .toList();
    }
}
