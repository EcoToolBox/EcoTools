package org.kaiaccount.account.eco.commands.ecotools;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.account.AccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.NamedAccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.commands.argument.currency.PaymentArgument;
import org.kaiaccount.account.eco.message.Messages;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.successful.SuccessfulTransactionResult;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.MappedArgumentWrapper;
import org.mose.command.builder.CommandBuilder;

import java.math.BigDecimal;
import java.util.Arrays;

public final class EcoCommands {

    private EcoCommands() {
        throw new RuntimeException("Dont do that");
    }

    public static ArgumentCommand createEcoGiveCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Account> accountArgument = targetAccountArgument("account");
            CommandArgument<PaymentBuilder> paymentArgument = new PaymentArgument("payment", true);

            return builder
                    .addArguments(new ExactArgument("eco"), new ExactArgument("give"), accountArgument, paymentArgument)
                    .setDescription("Gives money to a player")
                    .setPermissionNode(Permissions.GIVE_ECO.getPermissionNode())
                    .setExecutor((context, raw) -> {
                        Account account = context.getArgument(supplier, accountArgument);
                        PaymentBuilder paymentBuilder = context.getArgument(supplier, paymentArgument);
                        CommandSender source = context.getSource();
                        if (paymentBuilder.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                            source.sendMessage(Messages.GREATER_THAN.getProcessedMessage("Amount", 0));
                            return false;
                        }
                        Payment payment = paymentBuilder.setPlugin(EcoToolPlugin.getInstance()).build();
                        account.deposit(payment).thenAccept(result -> {
                            if (!(result instanceof SuccessfulTransactionResult)) {
                                return;
                            }
                            source.sendMessage(Messages.TRANSACTION_SUCCESSFUL.getProcessedMessage());

                            if (!(account instanceof PlayerAccount playerAccount)) {
                                return;
                            }
                            OfflinePlayer offlineAccountHolder = playerAccount.getPlayer();
                            if (!offlineAccountHolder.isOnline()) {
                                return;
                            }
                            Player accountHolder = offlineAccountHolder.getPlayer();
                            if (accountHolder == null) {
                                return;
                            }
                            accountHolder.sendMessage(Messages.RECEIVED_TRANSACTION.getProcessedMessage(result.getTransaction()));
                        });
                        source.sendMessage(Messages.TRANSACTION_STARTED.getProcessedMessage(TransactionType.DEPOSIT, payment, account));
                        return true;
                    })
                    .build();
        });
    }

    public static ArgumentCommand createEcoTakeCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<Account> accountArgument = targetAccountArgument("account");
            CommandArgument<PaymentBuilder> paymentArgument = new PaymentArgument("payment", true);

            return builder
                    .addArguments(new ExactArgument("eco"), new ExactArgument("take"), accountArgument, paymentArgument)
                    .setDescription("Takes money to a player")
                    .setPermissionNode(Permissions.TAKE_ECO.getPermissionNode())
                    .setExecutor((context, raw) -> {
                        Account account = context.getArgument(supplier, accountArgument);
                        PaymentBuilder paymentBuilder = context.getArgument(supplier, paymentArgument);
                        CommandSender source = context.getSource();
                        if (paymentBuilder.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                            source.sendMessage(Messages.GREATER_THAN.getProcessedMessage("Amount", 0));
                            return false;
                        }
                        Payment payment = paymentBuilder.setPlugin(EcoToolPlugin.getInstance()).build();
                        account.withdraw(payment).thenAccept(result -> {
                            if (!(result instanceof SuccessfulTransactionResult)) {
                                return;
                            }
                            source.sendMessage(Messages.TRANSACTION_SUCCESSFUL.getProcessedMessage());

                            if (!(account instanceof PlayerAccount playerAccount)) {
                                return;
                            }
                            OfflinePlayer offlineAccountHolder = playerAccount.getPlayer();
                            if (!offlineAccountHolder.isOnline()) {
                                return;
                            }
                            Player accountHolder = offlineAccountHolder.getPlayer();
                            if (accountHolder == null) {
                                return;
                            }
                            accountHolder.sendMessage(Messages.RECEIVED_TRANSACTION.getProcessedMessage(result.getTransaction()));
                        });
                        source.sendMessage(Messages.TRANSACTION_STARTED.getProcessedMessage(TransactionType.WITHDRAW, payment, account));
                        return true;
                    })
                    .build();
        });
    }

    private static CommandArgument<Account> targetAccountArgument(@NotNull String id) {
        PlayerBankArgument bankArgument = PlayerBankArgument.allPlayerBanks(id);
        NamedAccountArgument nameAccountArgument = new NamedAccountArgument(id);
        MappedArgumentWrapper<PlayerAccount, OfflinePlayer> playerAccountArgument = new MappedArgumentWrapper<>(new UserArgument(id,
                (command, argument) -> Arrays.stream(Bukkit.getOfflinePlayers())), user -> AccountInterface.getManager().getPlayerAccount(user));
        return new AccountArgument<>(id, bankArgument, nameAccountArgument, playerAccountArgument);
    }

}
