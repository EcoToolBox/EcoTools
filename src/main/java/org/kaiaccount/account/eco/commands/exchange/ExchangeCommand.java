package org.kaiaccount.account.eco.commands.exchange;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.currency.CurrencyArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.kaiaccount.account.inter.type.player.AbstractPlayerAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.simple.number.DoubleArgument;
import org.mose.command.context.CommandContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExchangeCommand implements ArgumentCommand {

    public static final CommandArgument<Currency<?>> FROM = new CurrencyArgument("from", (context, argument) -> {
        if (!(context.getSource() instanceof Player player)) {
            return Collections.emptySet();
        }
        PlayerAccount account = AccountInterface.getManager().getPlayerAccount(player);
        return account.getBalances().keySet().parallelStream().filter(c -> c.getWorth().isPresent()).toList();
    });

    public static final CommandArgument<Currency<?>> TO =
            new CurrencyArgument("to", (context, argument) -> AccountInterface.getManager()
                    .getCurrencies()
                    .parallelStream()
                    .filter(c -> c.getWorth().isPresent())
                    .toList());

    public static final CommandArgument<Double> AMOUNT = new DoubleArgument("amount");

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(FROM, TO, AMOUNT);
    }

    @Override
    public @NotNull String getDescription() {
        return "Transfer one currency to another";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.EXCHANGE.getPermissionNode());
    }

    @Override
    public boolean hasPermission(CommandSender source) {
        if (!(source instanceof Player)) {
            return false;
        }
        return ArgumentCommand.super.hasPermission(source);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) {
        if (!(commandContext.getSource() instanceof OfflinePlayer)) {
            return false;
        }
        Currency<?> from = commandContext.getArgument(this, FROM);
        if (from.getWorth().isEmpty()) {
            commandContext.getSource().sendMessage(from.getKeyName() + " has no exchange value");
            return false;
        }
        Currency<?> to = commandContext.getArgument(this, TO);
        if (to.getWorth().isEmpty()) {
            commandContext.getSource().sendMessage(to.getKeyName() + " has no exchange value");
            return false;
        }
        double amount = commandContext.getArgument(this, AMOUNT);

        PlayerAccount account =
                AccountInterface.getManager().getPlayerAccount((OfflinePlayer) commandContext.getSource());
        if (!(account instanceof AbstractPlayerAccount playerAccount)) {
            commandContext.getSource().sendMessage("Could not exchange: This error should be impossible to get to");
            return false;
        }
        BigDecimal previousFrom = account.getBalance(from);
        BigDecimal previousTo = account.getBalance(to);

        BigDecimal exchange = BigDecimal.valueOf(amount)
                .divide(from.getWorth().get(), RoundingMode.DOWN)
                .multiply(to.getWorth().get());

        BigDecimal newFrom = previousFrom.subtract(BigDecimal.valueOf(amount));
        BigDecimal newTo = previousTo.add(exchange);

        playerAccount.multipleTransaction(a -> a.set(new PaymentBuilder()
                                .setAmount(newFrom)
                                .setCurrency(from)
                                .setReason("Exchange")
                                .setFrom((NamedAccountLike) a)
                                .build(EcoToolPlugin.getPlugin())),
                        a -> a.set(new PaymentBuilder().setAmount(newTo)
                                .setCurrency(to)
                                .setReason("Exchange")
                                .setFrom((NamedAccountLike) a)
                                .build(EcoToolPlugin.getPlugin())))
                .thenAccept(result -> {
                    if (result == null) {
                        commandContext.getSource().sendMessage("Exchanged");
                        return;
                    }
                    commandContext.getSource().sendMessage("Could not exchange: " + result);
                });
        commandContext.getSource().sendMessage("Exchanging " + from.getSymbol() + " to " + to.getSymbol());
        return true;
    }
}
