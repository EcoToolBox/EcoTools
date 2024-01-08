package org.kaiaccount.account.eco.commands.argument.account;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.context.CommandArgumentContext;
import org.mose.command.context.CommandContext;

import java.io.IOException;
import java.util.Collection;
import java.util.function.BiFunction;

public class NamedAccountArgument implements CommandArgument<NamedAccount> {

    private final String id;
    private final BiFunction<CommandContext, CommandArgumentContext<NamedAccount>, Collection<NamedAccount>> function;


    public NamedAccountArgument(String id) {
        this(id, ((commandContext, namedAccountCommandArgumentContext) -> AccountInterface.getManager().getNamedAccounts()));
    }

    public NamedAccountArgument(String id, BiFunction<CommandContext, CommandArgumentContext<NamedAccount>, Collection<NamedAccount>> function) {
        this.id = id;
        this.function = function;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<NamedAccount> parse(@NotNull CommandContext commandContext, @NotNull CommandArgumentContext<NamedAccount> commandArgumentContext) throws IOException {
        Collection<NamedAccount> accounts = this.function.apply(commandContext, commandArgumentContext);
        String peek = commandArgumentContext.getFocusArgument().toLowerCase();

        NamedAccount namedAccount = accounts
                .parallelStream()
                .filter(account -> account.getAccountName().equalsIgnoreCase(peek))
                .findAny()
                .orElseThrow(() -> new IOException("No account by that name"));
        return CommandArgumentResult.from(commandArgumentContext, namedAccount);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull CommandArgumentContext<NamedAccount> commandArgumentContext) {
        Collection<NamedAccount> accounts = this.function.apply(commandContext, commandArgumentContext);
        String peek = commandArgumentContext.getFocusArgument().toLowerCase();
        return accounts.parallelStream()
                .map(NamedAccountLike::getAccountName)
                .filter(name -> name.toLowerCase().startsWith(peek))
                .toList();
    }
}
