package org.kaiaccount.account.eco.commands.balance;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.context.CommandContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CheckBankBalanceCommand implements ArgumentCommand {

    public static final CommandArgument<String> BANK = new ExactArgument("bank");
    public static final CommandArgument<PlayerBankAccount> BANK_ACCOUNT = new PermissionOrArgument<>("value",
            sender -> (sender.hasPermission(Permissions.BALANCE_OTHER.getPermissionNode())),
            new PlayerBankArgument("value", (context, argument) -> {
                List<PlayerBankAccount> allBanks = AccountInterface
                        .getManager()
                        .getPlayerAccounts()
                        .parallelStream()
                        .flatMap(account -> account.getBanks().parallelStream())
                        .toList();
                return CommandArgumentResult.from(argument, allBanks);
            }),
            new PlayerBankArgument("value", (context, argument) -> {
                if (!(context.getSource() instanceof OfflinePlayer player)) {
                    throw new RuntimeException("Player only command");
                }
                Collection<PlayerBankAccount> viewableBanks = AccountInterface
                        .getManager()
                        .getPlayerAccount(player)
                        .getAttachedBankByPermission(BankPermission.SEE);
                return CommandArgumentResult.from(argument, viewableBanks);
            }));

    @Override
    public @NotNull List<CommandArgument<?>> getArguments() {
        return Arrays.asList(BANK, BANK_ACCOUNT);
    }

    @Override
    public @NotNull String getDescription() {
        return "See the balance of your bank account";
    }

    @Override
    public @NotNull Optional<String> getPermissionNode() {
        return Optional.of(Permissions.BALANCE_SELF.getPermissionNode());
    }

    @Override
    public boolean run(CommandContext commandContext, String... strings) {
        PlayerBankAccount bankAccount = commandContext.getArgument(this, BANK_ACCOUNT);
        return CheckBalanceCommand.displayInfo(commandContext.getSource(), bankAccount);
    }
}
