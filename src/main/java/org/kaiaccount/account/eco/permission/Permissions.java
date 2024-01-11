package org.kaiaccount.account.eco.permission;

import org.jetbrains.annotations.NotNull;

public enum Permissions {

    BALANCE_SELF("eco.cmd.balance.self", true),
    BALANCE_OTHER("eco.cmd.balance.other", false),
    ADD_CURRENCY("eco.cmd.currency.add", false),
    REMOVE_CURRENCY("eco.cmd.currency.remove", false),
    SET_DEFAULT_CURRENCY("eco.cmd.currency.set.default", false),
    SET_EXCHANGE_CURRENCY("eco.cmd.currency.set.exchange", false),
    EXCHANGE("eco.cmd.exchange", true),
    GIVE_ECO("eco.cmd.tools.give", false),
    PAY("eco.cmd.pay.self", true),
    PAY_FROM("eco.cmd.pay.from", false),
    CREATE_BANK_ACCOUNT("eco.cmd.create.player.bank.self", true),
    CREATE_NAMED_ACCOUNT("eco.cmd.create.named", true),
    HISTORY_SELF("eco.cmd.transactions.self", true),
    HISTORY_OTHER("eco.cmd.transactions.other", false);


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
