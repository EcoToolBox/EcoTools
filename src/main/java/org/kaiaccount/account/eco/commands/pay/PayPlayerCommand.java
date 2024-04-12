package org.kaiaccount.account.eco.commands.pay;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.currency.CurrencyArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.Transaction;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.player.AbstractPlayerAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.RemainingArgument;
import org.mose.command.arguments.simple.number.DoubleArgument;
import org.mose.command.arguments.simple.text.StringArgument;
import org.mose.command.arguments.simple.text.StringCodeArguments;
import org.mose.command.context.CommandContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PayPlayerCommand implements ArgumentCommand {

    public static final CommandArgument<String> PLAYER = new ExactArgument("player");
    public static final CommandArgument<OfflinePlayer> USER = new UserArgument("user", (player) -> true);
    public static final CommandArgument<Currency<?>> CURRENCY =
            new CurrencyArgument("currency", (context, argument) -> {
                if (!(context.getSource() instanceof Player player)) {
                    return Collections.emptySet();
                }
                PlayerAccount account = AccountInterface.getManager().getPlayerAccount(player);
                return account.getBalances().keySet();
            });
    public static final CommandArgument<Double> AMOUNT = new DoubleArgument("amount");
    public static final CommandArgument<List<String>> REASON =
            new OptionalArgument<>(new RemainingArgument<>(new StringCodeArguments(new StringArgument("reason"))),
                    Collections.emptyList());

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(PLAYER, USER, CURRENCY, AMOUNT, REASON);
    }

    @Override
    public @NotNull String getDescription() {
        return "Pay a player";
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
    public boolean run(CommandContext commandContext, String... args) {
        OfflinePlayer toUser = commandContext.getArgument(this, USER);
        Currency<?> currency = commandContext.getArgument(this, CURRENCY);
        double payment = commandContext.getArgument(this, AMOUNT);
        String reason = String.join(" ", commandContext.getArgument(this, REASON));
        if (!(commandContext.getSource() instanceof Player player)) {
            return false;
        }
        Account account = AccountInterface.getManager().getPlayerAccount(player);
        if (payment <= 0) {
            commandContext.getSource().sendMessage("Payment requires more then 0");
            return false;
        }

        PlayerAccount toPlayer = AccountInterface.getManager().getPlayerAccount(toUser);
        if (!(toPlayer instanceof AbstractPlayerAccount toPlayerAccount)) {
            commandContext.getSource().sendMessage("Could not pay. This error should be impossible to get");
            return false;
        }
        if (!(account instanceof AbstractPlayerAccount toAccount)) {
            commandContext.getSource().sendMessage("Could not pay. This error should be impossible to get");
            return false;
        }
        Payment paymentResult =
                new PaymentBuilder().setAmount(payment).setCurrency(currency).setFrom(toAccount).setReason(reason).build(
                        EcoToolPlugin.getPlugin());
        new IsolatedTransaction(map -> {
            CompletableFuture<TransactionResult> withdraw =
                    toAccount.withdraw(paymentResult).thenApply(single -> single);
            CompletableFuture<TransactionResult> deposit =
                    toPlayerAccount.deposit(paymentResult).thenApply(single -> single);
            return List.of(withdraw, deposit);
        }, toAccount, toPlayerAccount)
                .start()
                .thenAccept(result -> {
                    player.sendMessage("Successfully paid " + toUser.getName());
                    Player onlineTo = toUser.getPlayer();
                    if (onlineTo == null) {
                        return;
                    }
                    Optional<Transaction> opTransaction = result.getTransactions()
                            .parallelStream()
                            .filter(type -> type.getType() == TransactionType.DEPOSIT)
                            .findAny();
                    if (opTransaction.isEmpty()) {
                        return;
                    }

                    onlineTo.sendMessage(
                            player.getName() + " sent you " + currency.formatSymbol(opTransaction.get()
                                    .getNewPaymentAmount()));
                    if (reason.isBlank()) {
                        return;
                    }
                    onlineTo.sendMessage(reason);
                });
        return true;
    }
}
