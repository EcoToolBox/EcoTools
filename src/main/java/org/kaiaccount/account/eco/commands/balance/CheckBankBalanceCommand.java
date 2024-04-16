package org.kaiaccount.account.eco.commands.balance;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.context.CommandContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CheckBankBalanceCommand implements ArgumentCommand {

    public static final ExactArgument BANK = new ExactArgument("bank");
    public static final PermissionOrArgument<PlayerBankAccount> BANK_ACCOUNT = new PermissionOrArgument<>("value",
            sender -> (sender.hasPermission(
                    Permissions.BALANCE_OTHER.getPermissionNode())),
            PlayerBankArgument.allPlayerBanks("value"), PlayerBankArgument.senderBanks("value"));

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
