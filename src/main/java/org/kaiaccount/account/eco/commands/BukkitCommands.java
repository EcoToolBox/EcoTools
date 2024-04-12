package org.kaiaccount.account.eco.commands;

import org.mose.command.BukkitCommandWrapper;

public final class BukkitCommands {

    public static final BukkitCommandWrapper BALANCE =
            new BukkitCommandWrapper(Commands.CHECK_BALANCE, Commands.CHECK_PLAYER_BALANCE,
                    Commands.CHECK_BANK_BALANCE, Commands.CHECK_NAMED_ACCOUNT_BALANCE);
    public static final BukkitCommandWrapper CURRENCY =
            new BukkitCommandWrapper(Commands.ADD_CURRENCY, Commands.SET_DEFAULT_CURRENCY,
                    Commands.SET_EXCHANGE_CURRENCY, Commands.REMOVE_CURRENCY);

    public static final BukkitCommandWrapper ECOTOOLS = new BukkitCommandWrapper(Commands.INFO, Commands.GIVE_ECO);
    public static final BukkitCommandWrapper EXCHANGE = new BukkitCommandWrapper(Commands.EXCHANGE);
    public static final BukkitCommandWrapper PAY = new BukkitCommandWrapper(Commands.PAY_PLAYER, Commands.PAY_BANK, Commands.PAY_NAMED, Commands.PAY_FROM_ANY);
    public static final BukkitCommandWrapper BANK = new BukkitCommandWrapper(Commands.CREATE_PLAYER_BANK);
    public static final BukkitCommandWrapper ACCOUNT = new BukkitCommandWrapper(Commands.CREATE_NAMED_ACCOUNT, Commands.REMOVE_NAMED_ACCOUNT);
    public static final BukkitCommandWrapper TRANSACTIONS = new BukkitCommandWrapper(Commands.TRANSACTIONS);

    private BukkitCommands() {
        throw new RuntimeException("Should not run");
    }
}
