package org.kaiaccount.account.eco.message.type.account;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.type.MessageArgumentType;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.bank.BankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;

public class AccountTypeNameArgumentType implements MessageArgumentType<Account> {
    @Override
    public @NotNull String getDefaultArgumentHandler() {
        return "Account";
    }

    @Override
    public @NotNull String apply(Account input) {
        if (input instanceof PlayerAccount) {
            return "player";
        }
        if (input instanceof BankAccount) {
            return "bank";
        }
        if (input instanceof NamedAccount) {
            return "named";
        }
        return input.getClass().getSimpleName();
    }

    @Override
    public @NotNull Class<Account> getClassType() {
        return Account.class;
    }
}
