package org.kaiaccount.account.eco.permission;

import org.jetbrains.annotations.NotNull;

public enum Permissions {

    BALANCE_SELF("eco.cmd.balance.self", true),
    BALANCE_OTHER("eco.cmd.balance.other", false),
    ADD_CURRENCY("eco.cmd.currency.add", false),
    REMOVE_CURRENCY("eco.cmd.currency.remove", false),
    SET_DEFAULT_CURRENCY("eco.cmd.currency.set.default", false),
    SET_EXCHANGE_CURRENCY("eco.cmd.currency.set.exchange", false),
    EXCHANGE("eco.cmd.exchange.self", true),
    EXCHANGE_OTHER("eco.cmd.exchange.other", false),
    GIVE_ECO("eco.cmd.tools.give", false),
    TAKE_ECO("eco.cmd.tools.take", false),
    PAY_SELF("eco.cmd.pay.self", true),
    PAY_OTHER("eco.cmd.pay.from.self", false),
    CREATE_BANK_ACCOUNT_SELF("eco.cmd.create.player.bank.self", true),
    CREATE_BANK_ACCOUNT_OTHER("eco.cmd.create.player.bank.other", false),
    CREATE_NAMED_ACCOUNT("eco.cmd.create.named", true),
    GRANT_BANK_ACCESS_SELF("eco.cmd.give.player.bank.self", true),
    GRANT_BANK_ACCESS_OTHER("eco.cmd.give.player.bank.other", false),
    HISTORY_SELF("eco.cmd.transactions.self", true),
    HISTORY_OTHER("eco.cmd.transactions.other", false),
    DELETE_BANK_OTHER("eco.cmd.delete.player.bank.other", false);

    private final @NotNull String permissionNode;
    private final boolean onByDefault;

    Permissions(@NotNull String permissionNode, boolean onByDefault) {
        this.onByDefault = onByDefault;
        this.permissionNode = permissionNode;
    }

    public @NotNull String getPermissionNode() {
        return this.permissionNode;
    }

    public boolean isOnByDefault() {
        return this.onByDefault;
    }
}
