package org.kaiaccount.account.eco.commands.ecotools;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.commands.argument.currency.CurrencyArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.successful.SuccessfulTransactionResult;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.simple.number.DoubleArgument;
import org.mose.command.context.CommandContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class GiveEcoCommand implements ArgumentCommand {

    public static final CommandArgument<String> GIVE = new ExactArgument("give");
    public static final CommandArgument<OfflinePlayer> USER = new UserArgument("user", u -> true);
    public static final CommandArgument<Currency<?>> CURRENCY = new CurrencyArgument("currency");
    public static final CommandArgument<Double> AMOUNT = new DoubleArgument("amount");

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(GIVE, USER, CURRENCY, AMOUNT);
    }

    @Override
    public @NotNull String getDescription() {
        return "Gives money to a player";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.GIVE_ECO.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) {
        OfflinePlayer player = commandContext.getArgument(this, USER);
        PlayerAccount account = AccountInterface.getManager().getPlayerAccount(player);
        Currency<?> currency = commandContext.getArgument(this, CURRENCY);
        double amount = commandContext.getArgument(this, AMOUNT);
        if (amount == 0) {
            commandContext.getSource().sendMessage(ChatColor.YELLOW + "Cannot send 0");
        }
        if (amount < 0) {
            commandContext.getSource().sendMessage(ChatColor.RED + "Amount cannot be negative");
        }
        account.deposit(new PaymentBuilder().setAmount(amount).setCurrency(currency).build(EcoToolPlugin.getPlugin()))
                .thenAccept(result -> {
                    if (!(result instanceof SuccessfulTransactionResult)) {
                        return;
                    }
                    commandContext.getSource().sendMessage("Money have arrived");
                    if (!player.isOnline()) {
                        return;
                    }
                    Player playerOnline = player.getPlayer();
                    if (playerOnline == null) {
                        return;
                    }
                    playerOnline.sendMessage("Received " + currency.formatSymbol(BigDecimal.valueOf(amount)));
                });
        commandContext
                .getSource()
                .sendMessage("Giving " + player.getName() + " " + currency.formatSymbol(BigDecimal.valueOf(amount)));
        return true;
    }
}
