package org.kaiaccount.account.eco.commands.pay;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.account.AccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.NamedAccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.commands.argument.currency.PaymentArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.message.messages.error.SourceOnlyCommandMessage;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.Transaction;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.MappedArgumentWrapper;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.arguments.simple.text.StringArgument;
import org.mose.command.builder.CommandBuilder;
import org.mose.command.exception.ArgumentException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class PayCommands {

    public PayCommands() {
        throw new RuntimeException("No dont do that");
    }

    public static ArgumentCommand createPayCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Account> payingAccountArgument = payingAccount("paying");
            PaymentArgument payArgument = new PaymentArgument("payment", true);
            CommandArgument<String> reasonArgument = new StringArgument("reason");

            return builder
                    .addArguments(payingAccountArgument, payArgument)
                    .setPermissionNode(Permissions.PAY_SELF.getPermissionNode())
                    .setDescription("Pay another account")
                    .setExecutor((context, raw) -> {
                        CommandSender source = context.getSource();
                        if (!(source instanceof OfflinePlayer offlinePlayer)) {
                            source.sendMessage(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE));
                            return false;
                        }
                        PlayerAccount playerAccount = AccountInterface.getManager().getPlayerAccount(offlinePlayer);
                        if (!(playerAccount instanceof AccountType targetType)) {
                            return false;
                        }
                        Account payingAccount = context.getArgument(supplier, payingAccountArgument);
                        String reason = context.getArgument(supplier, reasonArgument);
                        Payment payment = context.getArgument(supplier, payArgument).setReason(reason).setPlugin(EcoToolPlugin.getInstance()).build();
                        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                            context.getSource().sendMessage(Messages.GREATER_THAN.getProcessedMessage("payment", 0));
                            return false;
                        }
                        if (!(payingAccount instanceof AccountType payType)) {
                            return false;
                        }

                        new IsolatedTransaction((isolatedTarget, isolatedPay) -> {
                            CompletableFuture<SingleTransactionResult> withdraw = isolatedTarget.withdraw(payment);
                            CompletableFuture<SingleTransactionResult> deposit = isolatedPay.deposit(payment);
                            return List.of(withdraw, deposit);
                        }, targetType, payType).start().thenAccept(result -> {
                            if (result instanceof FailedTransactionResult failedResult) {
                                source.sendMessage(Messages.TRANSACTION_FAILED.getProcessedMessage(failedResult.getReason()));
                                return;
                            }
                            source.sendMessage(Messages.TRANSACTION_SUCCESSFUL.getProcessedMessage());

                            Transaction transaction = result
                                    .getTransactions()
                                    .stream()
                                    .filter(trans -> trans.getType() == TransactionType.DEPOSIT)
                                    .findAny()
                                    .orElseThrow(() -> new RuntimeException("Deposit is missing from transaction"));

                            OfflinePlayer target;
                            if (payType instanceof PlayerAccount playerPayAccount) {
                                target = playerPayAccount.getPlayer();
                            } else if (payType instanceof PlayerBankAccount playerBankAccount) {
                                target = playerBankAccount.getAccountHolder().getPlayer();
                            } else {
                                return;
                            }

                            if (!target.isOnline()) {
                                return;
                            }
                            Player player = target.getPlayer();
                            if (player == null) {
                                return;
                            }
                            player.sendMessage(Messages.RECEIVED_TRANSACTION.getProcessedMessage(transaction));
                        });
                        return true;
                    })
                    .build();
        });
    }

    public static ArgumentCommand createPayFromCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Account> accountArgument = targetSelfAccount("account");
            CommandArgument<Account> payingAccountArgument = payingAccount("paying");
            PaymentArgument payArgument = new PaymentArgument("payment", true);
            CommandArgument<String> reasonArgument = new StringArgument("reason");

            return builder
                    .addArguments(new ExactArgument("from"), accountArgument, payingAccountArgument, payArgument)
                    .setPermissionNode(Permissions.PAY_SELF.getPermissionNode())
                    .setDescription("Pay from another account")
                    .setExecutor((context, raw) -> {
                        Account targetAccount = context.getArgument(supplier, accountArgument);
                        Account payingAccount = context.getArgument(supplier, payingAccountArgument);
                        String reason = context.getArgument(supplier, reasonArgument);
                        Payment payment = context.getArgument(supplier, payArgument).setReason(reason).setPlugin(EcoToolPlugin.getInstance()).build();
                        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                            context.getSource().sendMessage(Messages.GREATER_THAN.getProcessedMessage("payment", 0));
                            return false;
                        }
                        if (!(targetAccount instanceof AccountType targetType)) {
                            return false;
                        }
                        if (!(payingAccount instanceof AccountType payType)) {
                            return false;
                        }

                        new IsolatedTransaction((isolatedTarget, isolatedPay) -> {
                            CompletableFuture<SingleTransactionResult> withdraw = isolatedTarget.withdraw(payment);
                            CompletableFuture<SingleTransactionResult> deposit = isolatedPay.deposit(payment);
                            return List.of(withdraw, deposit);
                        }, targetType, payType).start().thenAccept(result -> {
                            CommandSender source = context.getSource();
                            if (result instanceof FailedTransactionResult failedResult) {
                                source.sendMessage(Messages.TRANSACTION_FAILED.getProcessedMessage(failedResult.getReason()));
                                return;
                            }
                            source.sendMessage(Messages.TRANSACTION_SUCCESSFUL.getProcessedMessage());

                            Transaction transaction = result
                                    .getTransactions()
                                    .stream()
                                    .filter(trans -> trans.getType() == TransactionType.DEPOSIT)
                                    .findAny()
                                    .orElseThrow(() -> new RuntimeException("Deposit is missing from transaction"));

                            OfflinePlayer target;
                            if (payType instanceof PlayerAccount playerAccount) {
                                target = playerAccount.getPlayer();
                            } else if (payType instanceof PlayerBankAccount playerBankAccount) {
                                target = playerBankAccount.getAccountHolder().getPlayer();
                            } else {
                                return;
                            }

                            if (!target.isOnline()) {
                                return;
                            }
                            Player player = target.getPlayer();
                            if (player == null) {
                                return;
                            }
                            player.sendMessage(Messages.RECEIVED_TRANSACTION.getProcessedMessage(transaction));
                        });
                        return true;
                    })
                    .build();
        });
    }

    private static CommandArgument<Account> payingAccount(String id) {
        CommandArgument<PlayerAccount> allPlayers = new MappedArgumentWrapper<>(UserArgument.allButSource("player"),
                player -> AccountInterface.getManager().getPlayerAccount(player));
        CommandArgument<PlayerBankAccount> allBanks = PlayerBankArgument.banksWithPermission("bank",
                BankPermission.GIVE,
                (source) -> new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE)));
        return new AccountArgument<>(id, allPlayers, allBanks);
    }

    private static CommandArgument<Account> targetSelfAccount(String id) {
        CommandArgument<PlayerAccount> allPlayers = new MappedArgumentWrapper<>(new UserArgument("player",
                (context, argument) -> Arrays.stream(Bukkit.getOfflinePlayers())), user -> AccountInterface.getManager().getPlayerAccount(user));
        CommandArgument<PlayerBankAccount> allBanks = PlayerBankArgument.allPlayerBanks("bank");
        NamedAccountArgument allNamed = new NamedAccountArgument("named");

        AccountArgument<Account> allAccounts = new AccountArgument<>(id, allBanks, allPlayers, allNamed);

        CommandArgument<PlayerBankAccount> selfBanks = PlayerBankArgument.banksWithPermission("bank",
                BankPermission.TAKE,
                (source) -> new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE)));
        AccountArgument<Account> selfAccounts = new AccountArgument<>(id, selfBanks);

        PermissionOrArgument<Account> permissionArgument = new PermissionOrArgument<>(id,
                source -> source.hasPermission(Permissions.PAY_OTHER.getPermissionNode()),
                allAccounts,
                selfAccounts);

        return new OptionalArgument<Account>(permissionArgument, (command, argument) -> {
            if (!(command.getSource() instanceof OfflinePlayer player)) {
                throw new ArgumentException(Messages.SOURCE_ONLY.getProcessedMessage(SourceOnlyCommandMessage.PLAYER_SOURCE));
            }
            PlayerAccount account = AccountInterface.getManager().getPlayerAccount(player);
            return CommandArgumentResult.from(argument, account);
        });

    }

}
