package org.kaiaccount.account.eco.commands;

import org.kaiaccount.account.eco.commands.balance.CheckBalanceCommands;
import org.kaiaccount.account.eco.commands.bank.PlayerBankAccessCommands;
import org.kaiaccount.account.eco.commands.bank.PlayerBankCommands;
import org.kaiaccount.account.eco.commands.currency.CurrencyCommands;
import org.kaiaccount.account.eco.commands.ecotools.EcoCommands;
import org.kaiaccount.account.eco.commands.ecotools.EcoToolsCommands;
import org.kaiaccount.account.eco.commands.exchange.ExchangeCommands;
import org.kaiaccount.account.eco.commands.named.create.CreateNamedAccountCommand;
import org.kaiaccount.account.eco.commands.named.create.RemoveNamedAccountCommand;
import org.kaiaccount.account.eco.commands.pay.PayCommands;
import org.kaiaccount.account.eco.commands.transaction.TransactionsRangeCommands;
import org.mose.command.ArgumentCommand;

public final class Commands {

    public static final ArgumentCommand CHECK_PLAYER_BALANCE = CheckBalanceCommands.checkPlayerBalanceCommand();
    public static final ArgumentCommand CHECK_BANK_BALANCE = CheckBalanceCommands.checkBalanceBasicCommand();
    public static final ArgumentCommand CHECK_NAMED_ACCOUNT_BALANCE = CheckBalanceCommands.checkNamedAccountBalance();
    public static final ArgumentCommand CHECK_BALANCE = CheckBalanceCommands.checkBalanceBasicCommand();
    public static final ArgumentCommand ADD_CURRENCY = CurrencyCommands.createCurrencyCreateCommand();
    public static final ArgumentCommand REMOVE_CURRENCY = CurrencyCommands.createCurrencyRemoveCommand();
    public static final ArgumentCommand SET_DEFAULT_CURRENCY = CurrencyCommands.createDefaultCurrencyCommand();
    public static final ArgumentCommand SET_EXCHANGE_CURRENCY = CurrencyCommands.createSetExchangeValueCommand();
    public static final ArgumentCommand INFO = EcoToolsCommands.createInfoCommand();
    public static final ArgumentCommand GIVE_ECO = EcoCommands.createEcoGiveCommand();
    public static final ArgumentCommand TAKE_ECO = EcoCommands.createEcoTakeCommand();
    public static final ArgumentCommand EXCHANGE = ExchangeCommands.createExchangeCommand();
    public static final ArgumentCommand PAY = PayCommands.createPayCommand();
    public static final ArgumentCommand PAY_FROM = PayCommands.createPayFromCommand();
    public static final ArgumentCommand CREATE_PLAYER_BANK = PlayerBankCommands.createPlayerBankCreateCommand();
    public static final ArgumentCommand CLOSE_PLAYER_BANK = PlayerBankCommands.createPlayerBankCloseCommand();
    public static final CreateNamedAccountCommand CREATE_NAMED_ACCOUNT = new CreateNamedAccountCommand();
    public static final RemoveNamedAccountCommand REMOVE_NAMED_ACCOUNT = new RemoveNamedAccountCommand();
    public static final ArgumentCommand TRANSACTIONS = TransactionsRangeCommands.transactionRangeCommand();
    public static final ArgumentCommand GRANT_BANK_PERMISSION = PlayerBankAccessCommands.addPlayerCommand();
    public static final ArgumentCommand REMOVE_BANK_PERMISSION = PlayerBankAccessCommands.removePlayerCommand();

    private Commands() {
        throw new RuntimeException("Dont do that");
    }
}
