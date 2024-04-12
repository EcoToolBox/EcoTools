package org.kaiaccount.account.eco.commands.ecotools;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.event.TransactionCompletedEvent;
import org.kaiaccount.account.inter.event.TransactionEvent;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.context.CommandContext;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InfoCommand implements ArgumentCommand {

    public static final CommandArgument<String> INFO = new ExactArgument("info");

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(INFO);
    }

    @Override
    public @NotNull String getDescription() {
        return "Info about EcoTool";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) {
        CommandSender sender = commandContext.getSource();
        Collection<Currency<?>> currencies = AccountInterface.getManager().getCurrencies();
        RegisteredListener[] transactionEventHooks = TransactionEvent.getHandlerList().getRegisteredListeners();
        RegisteredListener[] transactionCompletedHooks =
                TransactionCompletedEvent.getHandlerList().getRegisteredListeners();
        sender.sendMessage("|===|Info|===|");
        sender.sendMessage("Version: " + EcoToolPlugin.getInstance().getDescription().getVersion());
        sender.sendMessage("Vault service enabled: " + Bukkit.getServicesManager().isProvidedFor(Economy.class));
        sender.sendMessage("Currencies: " + currencies.size());
        if (!(sender instanceof Player)) {
            sender.sendMessage("| Plugin | Key name | Symbol |");
            for (Currency<?> currency : currencies) {
                sender.sendMessage("| "
                        + currency.getPlugin().getName()
                        + " | "
                        + currency.getKeyName()
                        + " | "
                        + currency.getSymbol()
                        + " |");
            }
        }
        sender.sendMessage("TransactionCompleted Hooks: " + transactionCompletedHooks.length);
        if (!(sender instanceof Player)) {
            sender.sendMessage("| Plugin | Listener class |");
            for (RegisteredListener listener : transactionCompletedHooks) {
                sender.sendMessage("| " + listener.getPlugin().getName() + " | " + listener.getListener()
                        .getClass()
                        .getSimpleName() + " |");
            }
        }

        sender.sendMessage("Transaction Hooks: " + transactionEventHooks.length);
        if (!(sender instanceof Player)) {
            sender.sendMessage("| Plugin | Listener class |");
            for (RegisteredListener listener : transactionEventHooks) {
                sender.sendMessage("| " + listener.getPlugin().getName() + " | " + listener.getListener()
                        .getClass()
                        .getSimpleName() + " |");
            }
        }
        return true;
    }
}
