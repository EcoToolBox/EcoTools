package org.kaiaccount.account.eco.commands.pay.from;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.account.AccountArgument;
import org.kaiaccount.account.eco.commands.argument.currency.PaymentArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.context.CommandContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PayFromAnyCommand implements ArgumentCommand {

    public static final AccountArgument<Account> FROM = AccountArgument.allAccounts("fromAccount");

    public static final AccountArgument<Account> TO = AccountArgument.allAccounts("toAccount");

    public static final PaymentArgument PAYMENT = new PaymentArgument("payment", true);

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(new ExactArgument("from"), FROM, TO, PAYMENT);
    }

    @Override
    public @NotNull String getDescription() {
        return "Pay a account from any account";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.PAY_FROM.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        Account from = commandContext.getArgument(this, FROM);
        if (!(from instanceof AccountType fromType && from instanceof NamedAccountLike fromNamed)) {
            commandContext.getSource().sendMessage("Technical error: Account 'From' is not a AccountType");
            return false;
        }

        Account to = commandContext.getArgument(this, TO);
        if (!(to instanceof AccountType toType)) {
            commandContext.getSource().sendMessage("Technical error: Account 'To' is not a AccountType");
            return false;
        }

        PaymentBuilder payment = commandContext.getArgument(this, PAYMENT);

        new IsolatedTransaction((isolatedFrom, isolatedTo) -> {
            CompletableFuture<SingleTransactionResult> deposit = isolatedTo.deposit(payment.setFrom(fromNamed).build(EcoToolPlugin.getPlugin()));
            CompletableFuture<SingleTransactionResult> withdraw = isolatedFrom.withdraw(payment.build(EcoToolPlugin.getPlugin()));
            return List.of(deposit, withdraw);
        }, fromType, toType)
                .start()
                .thenAccept(result -> {
                    if (result instanceof FailedTransactionResult failedTransactionResult) {
                        commandContext.getSource().sendMessage("Transaction failed: No money has left the account: Failed due to " + failedTransactionResult.getReason());
                        return;
                    }
                    commandContext.getSource().sendMessage("Transaction successful");
                });
        commandContext.getSource().sendMessage("Payment request sent");
        return true;
    }
}
