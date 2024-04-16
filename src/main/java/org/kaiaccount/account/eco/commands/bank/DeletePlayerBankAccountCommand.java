package org.kaiaccount.account.eco.commands.bank;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.message.messages.error.SourceOnlyCommandMessage;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.named.bank.BankAccount;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.ParseCommandArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.context.CommandArgumentContext;
import org.mose.command.context.CommandContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DeletePlayerBankAccountCommand implements ArgumentCommand {

    public static final ExactArgument CLOSE = new ExactArgument("close");
    public static final PermissionOrArgument<PlayerBankAccount> BANK;

    static {
        BANK = new PermissionOrArgument<>("bank",
                sender -> (sender.hasPermission(
                        Permissions.BALANCE_OTHER.getPermissionNode())),
                PlayerBankArgument.allPlayerBanks("bank"), PlayerBankArgument.senderBanks("bank"));
    }

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(CLOSE, BANK);
    }

    @Override
    public @NotNull String getDescription() {
        return "Deletes a bank account";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.CREATE_BANK_ACCOUNT.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        PlayerBankAccount bank = commandContext.getArgument(this, BANK);
        if (!(commandContext.getSource() instanceof OfflinePlayer)) {
            commandContext.getSource()
                    .sendMessage(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE));
            return true;
        }
        PlayerAccount account = bank.getAccountHolder();
        if (!(account instanceof AccountType playerAccount)) {
            commandContext.getSource().sendMessage("Technical error: Could not remove Bank account. Owner is not of AccountType");
            return false;
        }

        if (!(bank instanceof AccountType bankAccount)) {
            commandContext.getSource().sendMessage("Technical error: Could not remove Bank account. Bank is not of AccountType");
            return false;
        }

        new IsolatedTransaction((isolatedNamed, isolatedGoingTo) -> isolatedNamed.getBalances().entrySet().stream()
                .<CompletableFuture<? extends TransactionResult>>map(entry -> {
                    Payment payment = new PaymentBuilder()
                            .setAmount(entry.getValue())
                            .setCurrency(entry.getKey())
                            .setFrom(account)
                            .setReason("Account closure")
                            .setPriority(true)
                            .setPlugin(EcoToolPlugin.getInstance())
                            .build();
                    return isolatedGoingTo.deposit(payment);
                }).toList(), bankAccount, playerAccount)
                .start()
                .thenAccept(transactionResult -> {
                    if (transactionResult instanceof FailedTransactionResult failed) {
                        commandContext.getSource()
                                .sendMessage(
                                        "Failed to remove account. No money has been transferred, cancelling transaction: Failed "
                                                + "for "
                                                + failed.getReason());
                        return;
                    }
                    commandContext.getSource().sendMessage("Payment transferred");
                    account.deleteBankAccount(bank);
                });

        return true;
    }
}
