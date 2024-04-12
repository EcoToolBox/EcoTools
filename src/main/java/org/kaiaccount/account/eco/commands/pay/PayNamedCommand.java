package org.kaiaccount.account.eco.commands.pay;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.account.NamedAccountArgument;
import org.kaiaccount.account.eco.commands.argument.currency.PaymentArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.message.messages.error.SourceOnlyCommandMessage;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.IsolatedAccount;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.context.CommandContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PayNamedCommand implements ArgumentCommand {

    public static final CommandArgument<String> NAMED = new ExactArgument("account");
    public static final CommandArgument<NamedAccount> NAMED_ACCOUNT = new NamedAccountArgument("namedAccount", (cmdContext, argContext) -> AccountInterface.getManager().getNamedAccounts());
    public static final PaymentArgument VALUE = new PaymentArgument("payment", true);

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return Arrays.asList(NAMED, NAMED_ACCOUNT, VALUE);
    }

    @Override
    public @NotNull String getDescription() {
        return "pay a named account";
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        if (!(source instanceof Player)) {
            return false;
        }
        return ArgumentCommand.super.hasPermission(source);
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.PAY.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        if (!(commandContext.getSource() instanceof OfflinePlayer player)) {
            String message = Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE);
            commandContext.getSource().sendMessage(message);
            return true;
        }
        NamedAccount namedAccount = commandContext.getArgument(this, NAMED_ACCOUNT);
        if (!(namedAccount instanceof AccountType namedAccountType)) {
            commandContext.getSource()
                    .sendMessage(
                            "technical error: NamedAccount is not of the correct type. No money has been transferred, "
                                    + "cancelling payment");
            return true;
        }
        PaymentBuilder value = commandContext.getArgument(this, VALUE);
        PlayerAccount playerAccount = AccountInterface.getManager().getPlayerAccount(player);
        if (!(playerAccount instanceof AccountType playerAccountType)) {
            commandContext.getSource()
                    .sendMessage(
                            "technical error: PlayerAccount is not of the correct type. No money has been transferred,"
                                    + " cancelling payment");
            return true;
        }

        CompletableFuture<TransactionResult> result = new IsolatedTransaction(accounts -> {
            IsolatedAccount isolatedPlayer = accounts.get(playerAccount);
            IsolatedAccount isolatedBank = accounts.get(namedAccount);

            Payment withdrawPayment = value.build(EcoToolPlugin.getPlugin());
            Payment depositPayment = value.setFrom(playerAccount).build(EcoToolPlugin.getPlugin());

            CompletableFuture<SingleTransactionResult> withdraw = isolatedPlayer.withdraw(withdrawPayment);
            CompletableFuture<SingleTransactionResult> deposit = isolatedBank.deposit(depositPayment);

            return List.of(withdraw, deposit);
        }, playerAccountType, namedAccountType).start();

        result.thenAccept(transactionResult -> {
            if (transactionResult instanceof FailedTransactionResult failed) {
                commandContext.getSource()
                        .sendMessage(
                                "Failed to pay account. No money has been transferred, cancelling transaction: Failed "
                                        + "for "
                                        + failed.getReason());
                return;
            }
            commandContext.getSource().sendMessage("Payment complete");
        });

        return true;
    }
}
