package org.kaiaccount.account.eco.message.messages.error;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.AbstractMessage;
import org.kaiaccount.account.eco.message.type.MessageArgument;
import org.kaiaccount.account.eco.message.type.MessageArgumentTypes;
import org.kaiaccount.account.inter.type.Account;

import java.util.*;

public class AccountIsNotOfAccountTypeMessage extends AbstractMessage {

    public static final MessageArgument<String> ACTION = new MessageArgument<>(MessageArgumentTypes.ACTION);
    public static final MessageArgument<String> ACCOUNT_TYPE_NAME = new MessageArgument<>(MessageArgumentTypes.RAW_ACCOUNT_TYPE_NAME,
            "target account type name");

    @Override
    public @NotNull String getDefaultMessage() {
        return "Technical error: Could not %" + ACTION.getArgumentHandler() + "% %action " + MessageArgumentTypes.ACCOUNT_TYPE_NAME.getDefaultArgumentHandler()
                + "% account. % " + ACCOUNT_TYPE_NAME.getArgumentHandler() + "% is not of AccountType";
    }

    @Override
    public @NotNull Collection<MessageArgument<?>> getArguments() {
        List<MessageArgument<Account>> actionAccounts = MessageArgumentTypes
                .<Account>getArguments(Account.class, type -> "action " + type.getDefaultArgumentHandler())
                .toList();
        Collection<MessageArgument<?>> ret = new LinkedList<>(actionAccounts);
        ret.add(ACCOUNT_TYPE_NAME);
        ret.add(ACTION);
        return ret;
    }

    public @NotNull String process(Account account, String action, String accountType) {
        Map<MessageArgument<?>, Object> map = new HashMap<>();
        map.put(ACTION, action);
        map.put(ACCOUNT_TYPE_NAME, accountType);
        MessageArgumentTypes
                .<Account>getArguments(Account.class, type -> "action " + type.getDefaultArgumentHandler())
                .forEach(argument -> map.put(argument, account));
        return getProcessedMessage(map);
    }
}
