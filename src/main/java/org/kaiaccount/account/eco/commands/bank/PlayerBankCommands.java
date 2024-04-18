package org.kaiaccount.account.eco.commands.bank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.kaiaccount.AccountInterface;
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
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.arguments.simple.text.StringArgument;
import org.mose.command.builder.CommandBuilder;
import org.mose.command.exception.ArgumentException;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public final class PlayerBankCommands {

    private PlayerBankCommands() {
        throw new RuntimeException("Dont do that");
    }

    public static ArgumentCommand createPlayerBankCloseCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            PermissionOrArgument<PlayerBankAccount> bankArgument = new PermissionOrArgument<>("bank",
                    sender -> sender.hasPermission(Permissions.DELETE_BANK_OTHER.getPermissionNode()),
                    PlayerBankArgument.allPlayerBanks("bank"),
                    PlayerBankArgument.banksWithPermission("bank",
                            BankPermission.ACCOUNT_OWNER,
                            (sender) -> new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(sender.getName()))));

            return builder
                    .setPermissionNode(Permissions.CREATE_BANK_ACCOUNT_SELF.getPermissionNode())
                    .addArguments(new ExactArgument("close"), bankArgument)
                    .setDescription("Deletes a bank account")
                    .setExecutor((context, raw) -> {
                        PlayerBankAccount bank = context.getArgument(supplier, bankArgument);
                        if (!(bank instanceof AccountType bankAccountType)) {
                            context.getSource().sendMessage(Messages.ACCOUNT_IS_NOT_OF_ACCOUNT_TYPE.process(bank, "remove", "Bank"));
                            return false;
                        }
                        PlayerAccount holder = bank.getAccountHolder();
                        if (!(holder instanceof AccountType holderAccountType)) {
                            context.getSource().sendMessage(Messages.ACCOUNT_IS_NOT_OF_ACCOUNT_TYPE.process(bank, "remove", "Holder"));
                            return false;
                        }

                        new IsolatedTransaction((isolatedNamed, isolatedGoingTo) -> isolatedNamed
                                .getBalances()
                                .entrySet()
                                .stream()
                                .<CompletableFuture<? extends TransactionResult>>map(entry -> {
                                    Payment payment = new PaymentBuilder()
                                            .setAmount(entry.getValue())
                                            .setCurrency(entry.getKey())
                                            .setFrom(holder)
                                            .setReason("Account closure")
                                            .setPriority(true)
                                            .setPlugin(EcoToolPlugin.getInstance())
                                            .build();
                                    return isolatedGoingTo.deposit(payment);
                                })
                                .toList(), bankAccountType, holderAccountType).start().thenAccept(transactionResult -> {
                            if (transactionResult instanceof FailedTransactionResult failed) {
                                context.getSource().sendMessage(Messages.ACCOUNT_FAILED_REMOVAL_FOR_REASON.getProcessedMessage(bank, failed.getReason()));
                                return;
                            }
                            context.getSource().sendMessage(Messages.TRANSACTION_SUCCESSFUL.getProcessedMessage());
                            holder.deleteBankAccount(bank);
                        });

                        return true;

                    })
                    .build();
        });
    }

    public static ArgumentCommand createPlayerBankCreateCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<OfflinePlayer> targetPlayerArgument = targetCreateUser();
            CommandArgument<String> bankNameArgument = new StringArgument("name");

            return builder
                    .setDescription("Creates a new bank account")
                    .setPermissionNode(Permissions.CREATE_BANK_ACCOUNT_SELF.getPermissionNode())
                    .setExecutor((context, raw) -> {
                        OfflinePlayer targetPlayer = context.getArgument(supplier, targetPlayerArgument);
                        PlayerAccount targetPlayerAccount = AccountInterface.getManager().getPlayerAccount(targetPlayer);
                        String newBankName = context.getArgument(supplier, bankNameArgument);

                        if (targetPlayerAccount.getBank(newBankName).isPresent()) {
                            context.getSource().sendMessage(Messages.BANK_WITH_NAME_ALREADY_CREATED.getProcessedMessage(newBankName));
                            return true;
                        }
                        PlayerBankAccount bankAccount = targetPlayerAccount.createBankAccount(newBankName);
                        context.getSource().sendMessage(Messages.CREATED_ACCOUNT.getProcessedMessage("bank", bankAccount.getAccountName()));
                        return true;
                    })
                    .build();

        });
    }

    private static CommandArgument<OfflinePlayer> targetCreateUser() {
        CommandArgument<OfflinePlayer> target = new UserArgument("user", (command, argument) -> Arrays.stream(Bukkit.getOfflinePlayers()));
        return wrapTarget(target);
    }

    private static CommandArgument<OfflinePlayer> wrapTarget(CommandArgument<OfflinePlayer> targetArgument) {
        PermissionOrArgument<OfflinePlayer> permissionArgument = new PermissionOrArgument<>("user",
                source -> source.hasPermission(Permissions.CREATE_BANK_ACCOUNT_OTHER.getPermissionNode()),
                targetArgument);
        return new OptionalArgument<OfflinePlayer>(permissionArgument, (command, context) -> {
            if (command.getSource() instanceof OfflinePlayer player) {
                return CommandArgumentResult.from(context, player);
            }
            throw new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE));
        });
    }

}
