package org.kaiaccount.account.eco.commands.argument.account;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class PlayerBankArgument implements CommandArgument<PlayerBankAccount> {

    private final String id;
    private final BiFunction<CommandContext, ArgumentContext,
            Collection<PlayerBankAccount>>
            function;

    public PlayerBankArgument(@NotNull String id,
                              BiFunction<CommandContext, ArgumentContext, Collection<PlayerBankAccount>> function) {
        this.id = id;
        this.function = function;
    }

    public static @NotNull PlayerBankArgument allPlayerBanks(@NotNull String id) {
        return new PlayerBankArgument(id, (context, argument) -> AccountInterface.getManager()
                .getPlayerAccounts()
                .parallelStream()
                .flatMap(player -> player.getBanks().parallelStream())
                .toList());
    }

    public static @NotNull PlayerBankArgument senderBanks(@NotNull String id) {
        return new PlayerBankArgument(id, (context, argument) -> {
            if (!(context.getSource() instanceof OfflinePlayer player)) {
                return Collections.emptySet();
            }
            PlayerAccount account = AccountInterface.getManager().getPlayerAccount(player);
            return account.getBanks();
        });
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<PlayerBankAccount> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument) throws ArgumentException {
        Collection<PlayerBankAccount> banks = this.function.apply(context, argument);
        String peek = argument.getFocusArgument().toLowerCase();
        String playerOwner = null;
        String bankName = peek;
        if (peek.contains(".")) {
            String[] split = peek.split(Pattern.quote("."));
            if (split.length > 1) {
                playerOwner = split[0];
                bankName = split[1];
            }
        }

        String finalPlayerOwner = playerOwner;
        String finalBankName = bankName;

        Optional<PlayerBankAccount> opBank = banks.parallelStream()
                .filter(name -> name.getAccountName().toLowerCase().equals(finalBankName))
                .filter(name -> {
                    if (finalPlayerOwner == null) {
                        return true;
                    }
                    String playerName = name.getAccountHolder().getPlayer().getName();
                    if (playerName == null) {
                        return true;
                    }
                    return playerName.equals(finalPlayerOwner);
                })
                .findAny();
        if (opBank.isEmpty()) {
            throw new ArgumentException("No bank by that name");
        }
        return CommandArgumentResult.from(argument, opBank.get());
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext argument) {
        Collection<PlayerBankAccount> banks = this.function.apply(commandContext, argument);
        String peek = argument.getFocusArgument().toLowerCase();
        return banks.parallelStream()
                .filter(name -> name.getAccountName().toLowerCase().startsWith(peek) || (name.getAccountHolder()
                        .getPlayer()
                        .getName() + "." + name.getAccountName()).toLowerCase().startsWith(peek))
                .flatMap(name -> Arrays.asList(name.getAccountName(),
                                name.getAccountHolder().getPlayer().getName() + "." + name.getAccountName())
                        .parallelStream())
                .toList();
    }
}
