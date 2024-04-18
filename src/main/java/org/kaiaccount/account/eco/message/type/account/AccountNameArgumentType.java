package org.kaiaccount.account.eco.message.type.account;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.type.MessageArgumentType;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;

public class AccountNameArgumentType implements MessageArgumentType<Account> {
    @Override
    public @NotNull String getDefaultArgumentHandler() {
        return "Account";
    }

    @Override
    public @NotNull String apply(Account input) {
        if (!(input instanceof NamedAccountLike named)) {
            return "Unknown";
        }
        return named.getAccountName();
    }

    @Override
    public @NotNull Class<Account> getClassType() {
        return Account.class;
    }
}
