package org.kaiaccount.account.eco.commands.named.create;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.account.named.EcoNamedAccount;
import org.kaiaccount.account.eco.commands.argument.account.AccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.NamedAccountArgument;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.IsolatedAccount;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.ParseCommandArgument;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.IsolatedAccount;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.MappedArgumentWrapper;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RemoveNamedAccountCommand implements ArgumentCommand {

    public static final CommandArgument<NamedAccount> NAME;
    public static final CommandArgument<NamedAccountLike> INTO;

    static {
        NAME = new NamedAccountArgument("name", (cmdContext, argContext) -> AccountInterface
                .getManager()
                .getNamedAccounts());

        NamedAccountArgument namedAccountArgument = new NamedAccountArgument("", (cmdContext, argContext) -> AccountInterface
                .getManager()
                .getNamedAccounts());
        PlayerBankArgument bankAccountArgument = PlayerBankArgument.allPlayerBanks("");
        MappedArgumentWrapper<PlayerAccount, OfflinePlayer> playerAccountArgument = new MappedArgumentWrapper<>(new UserArgument("", p -> true), user -> AccountInterface
                .getManager()
                .getPlayerAccount(user));
        AccountArgument<NamedAccountLike> anyAccountArgument = new AccountArgument<>("to",
                namedAccountArgument,
                bankAccountArgument,
                playerAccountArgument
        );
        var elseResult = new ParseCommandArgument<NamedAccountLike>() {

            @Override
            public @NotNull CommandArgumentResult<NamedAccountLike> parse(@NotNull CommandContext commandContext, @NotNull ArgumentContext commandArgumentContext) throws ArgumentException {
                if (commandContext.getSource() instanceof Player player) {
                    return CommandArgumentResult.from(commandArgumentContext, 0, AccountInterface.getManager().getPlayerAccount(player));
                }
                throw new ArgumentException("a account must be specified");
            }
        };
        INTO = new OptionalArgument<>(anyAccountArgument, elseResult);
    }


    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return List.of(new ExactArgument("remove"), NAME);
    }

    @Override
    public @NotNull String getDescription() {
        return "Removes a named account";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.CREATE_NAMED_ACCOUNT.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        NamedAccount account = commandContext.getArgument(this, NAME);
        if (!(account instanceof EcoNamedAccount ecoAccount)) {
            commandContext.getSource().sendMessage("Technical error: Could not remove NamedAccount. Wasn't created using " + EcoToolPlugin.getPlugin().getName());
            return false;
        }

        NamedAccountLike goingTo = commandContext.getArgument(this, INTO);
        if (!(goingTo instanceof AccountType goingToType)) {
            commandContext.getSource().sendMessage("Technical error: Could not remove NamedAccount. GoingTo is not of AccountType");
            return false;
        }

        new IsolatedTransaction((accounts) -> {
            IsolatedAccount isolatedNamed = accounts.get(account);
            IsolatedAccount isolatedGoingTo = accounts.get(goingTo);

            return isolatedNamed.getBalances().entrySet().stream()
                    .<CompletableFuture<? extends TransactionResult>>map(entry -> {
                        Payment payment = new PaymentBuilder()
                                .setAmount(entry.getValue())
                                .setCurrency(entry.getKey())
                                .setFrom(account)
                                .setReason("Account closure")
                                .setPriority(true)
                                .setPlugin(EcoToolPlugin.getPlugin())
                                .build();
                        return isolatedGoingTo.deposit(payment);
                    }).toList();
        }, ecoAccount, goingToType)
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
                    if (!ecoAccount.getFile().delete()) {
                        commandContext.getSource().sendMessage("Technical error: Could not delete NamedAccount File.");
                        //this ensures no money dupes
                        ecoAccount.getBalances().keySet().forEach(currency -> ecoAccount.forceSetSynced(new PaymentBuilder()
                                .setCurrency(currency)
                                .setAmount(0)
                                .setPriority(true)
                                .setReason("Account closure")
                                .setFrom(goingTo)
                                .setPlugin(EcoToolPlugin.getPlugin()).build()));
                    }
                    AccountInterface.getManager().deregisterNamedAccount(account);
                });
        return true;
    }
}
