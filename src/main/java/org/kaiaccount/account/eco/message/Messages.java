package org.kaiaccount.account.eco.message;

import org.kaiaccount.account.eco.message.messages.account.CreatedAccountMessage;
import org.kaiaccount.account.eco.message.messages.error.*;
import org.kaiaccount.account.eco.message.messages.transaction.*;

public final class Messages {

    public static final TotalWorthMessage TOTAL_WORTH = new TotalWorthMessage(null);
    public static final SourceOnlyCommandMessage SOURCE_ONLY = new SourceOnlyCommandMessage(null);
    public static final CreatedAccountMessage CREATED_ACCOUNT = new CreatedAccountMessage();
    public static final AccountIsNotOfAccountTypeMessage ACCOUNT_IS_NOT_OF_ACCOUNT_TYPE = new AccountIsNotOfAccountTypeMessage();
    public static final BankWithNameAlreadyCreatedMessage BANK_WITH_NAME_ALREADY_CREATED = new BankWithNameAlreadyCreatedMessage();
    public static final BankAccountFailedRemovalForReasonMessage ACCOUNT_FAILED_REMOVAL_FOR_REASON = new BankAccountFailedRemovalForReasonMessage();
    public static final TransactionSuccessfulMessage TRANSACTION_SUCCESSFUL = new TransactionSuccessfulMessage();
    public static final CurrencyAlreadyRegisteredMessage CURRENCY_ALREADY_REGISTERED = new CurrencyAlreadyRegisteredMessage();
    public static final RegisteredCurrencyMessage CURRENCY_REGISTERED = new RegisteredCurrencyMessage();
    public static final CurrencyUnregisteredMessage CURRENCY_UNREGISTERED = new CurrencyUnregisteredMessage();
    public static final CurrencyCouldNotBeRegisteredMessage CURRENCY_COULD_NOT_BE_REGISTERED = new CurrencyCouldNotBeRegisteredMessage();
    public static final CurrencyFailedRemovalForReasonMessage CURRENCY_FAILED_REMOVAL_FOR_REASON = new CurrencyFailedRemovalForReasonMessage();
    public static final DefaultCurrencyChangedMessage DEFAULT_CURRENCY_CHANGED = new DefaultCurrencyChangedMessage();
    public static final CurrencyExchangeValueRemovedMessage EXCHANGE_VALUE_REMOVED = new CurrencyExchangeValueRemovedMessage();
    public static final CurrencyExchangeValueSetMessage EXCHANGE_VALUE_SET = new CurrencyExchangeValueSetMessage();
    public static final GreaterThanCommandMessage GREATER_THAN = new GreaterThanCommandMessage();
    public static final TransactionStartedMessage TRANSACTION_STARTED = new TransactionStartedMessage();
    public static final ReceivedTransactionMessage RECEIVED_TRANSACTION = new ReceivedTransactionMessage();
    public static final ExchangeStartedMessage EXCHANGE_STARTED = new ExchangeStartedMessage();
    public static final TransactionFailedForReasonMessage TRANSACTION_FAILED = new TransactionFailedForReasonMessage();

    private Messages() {
        throw new RuntimeException("Dont do that");
    }
}
