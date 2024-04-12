package org.kaiaccount.account.eco.commands;

import org.kaiaccount.account.eco.commands.balance.CheckBalanceCommand;
import org.kaiaccount.account.eco.commands.balance.CheckBankBalanceCommand;
import org.kaiaccount.account.eco.commands.balance.CheckNamedAccountBalanceCommand;
import org.kaiaccount.account.eco.commands.balance.CheckPlayerBalanceCommand;
import org.kaiaccount.account.eco.commands.bank.CreatePlayerBankAccountCommand;
import org.kaiaccount.account.eco.commands.currency.AddCurrencyCommand;
import org.kaiaccount.account.eco.commands.currency.RemoveCurrencyCommand;
import org.kaiaccount.account.eco.commands.currency.SetDefaultCurrencyCommand;
import org.kaiaccount.account.eco.commands.currency.SetExchangeValueCommand;
import org.kaiaccount.account.eco.commands.ecotools.GiveEcoCommand;
import org.kaiaccount.account.eco.commands.ecotools.InfoCommand;
import org.kaiaccount.account.eco.commands.exchange.ExchangeCommand;
import org.kaiaccount.account.eco.commands.named.create.CreateNamedAccountCommand;
import org.kaiaccount.account.eco.commands.named.create.RemoveNamedAccountCommand;
import org.kaiaccount.account.eco.commands.pay.PayBankCommand;
import org.kaiaccount.account.eco.commands.pay.PayNamedCommand;
import org.kaiaccount.account.eco.commands.pay.PayPlayerCommand;
import org.kaiaccount.account.eco.commands.pay.from.PayFromAnyCommand;
import org.kaiaccount.account.eco.commands.transaction.TransactionsRangeCommand;

public final class Commands {

    public static final CheckPlayerBalanceCommand CHECK_PLAYER_BALANCE = new CheckPlayerBalanceCommand();
    public static final CheckBankBalanceCommand CHECK_BANK_BALANCE = new CheckBankBalanceCommand();
    public static final CheckNamedAccountBalanceCommand CHECK_NAMED_ACCOUNT_BALANCE = new CheckNamedAccountBalanceCommand();
    public static final CheckBalanceCommand CHECK_BALANCE = new CheckBalanceCommand();
    public static final AddCurrencyCommand ADD_CURRENCY = new AddCurrencyCommand();
    public static final RemoveCurrencyCommand REMOVE_CURRENCY = new RemoveCurrencyCommand();
    public static final SetDefaultCurrencyCommand SET_DEFAULT_CURRENCY = new SetDefaultCurrencyCommand();
    public static final SetExchangeValueCommand SET_EXCHANGE_CURRENCY = new SetExchangeValueCommand();
    public static final InfoCommand INFO = new InfoCommand();
    public static final GiveEcoCommand GIVE_ECO = new GiveEcoCommand();
    public static final ExchangeCommand EXCHANGE = new ExchangeCommand();
    public static final PayPlayerCommand PAY_PLAYER = new PayPlayerCommand();
    public static final PayBankCommand PAY_BANK = new PayBankCommand();
    public static final PayNamedCommand PAY_NAMED = new PayNamedCommand();
    public static final PayFromAnyCommand PAY_FROM_ANY = new PayFromAnyCommand();
    public static final CreatePlayerBankAccountCommand CREATE_PLAYER_BANK = new CreatePlayerBankAccountCommand();
    public static final CreateNamedAccountCommand CREATE_NAMED_ACCOUNT = new CreateNamedAccountCommand();
    public static final RemoveNamedAccountCommand REMOVE_NAMED_ACCOUNT = new RemoveNamedAccountCommand();
    public static final TransactionsRangeCommand TRANSACTIONS = new TransactionsRangeCommand();

    private Commands() {
        throw new RuntimeException("Dont do that");
    }
}
