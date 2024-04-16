package org.kaiaccount.account.eco.commands.argument.account;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.utils.function.CommandFunction;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.ParseCommandArgument;
import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PlayerBankArgument implements CommandArgument<PlayerBankAccount> {

    private final String id;
    private final ParseCommandArgument<Collection<PlayerBankAccount>> function;

    public PlayerBankArgument(@NotNull String id, ParseCommandArgument<Collection<PlayerBankAccount>> function) {
        this.id = id;
        this.function = function;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull CommandArgumentResult<PlayerBankAccount> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument)
            throws ArgumentException {
        Collection<PlayerBankAccount> banks = this.function.parse(context, argument).getValue();
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

        Optional<PlayerBankAccount> opBank = banks.parallelStream().filter(name -> name.getAccountName().toLowerCase().equals(finalBankName)).filter(name -> {
            if (finalPlayerOwner == null) {
                return true;
            }
            String playerName = name.getAccountHolder().getPlayer().getName();
            if (playerName == null) {
                return true;
            }
            return playerName.equals(finalPlayerOwner);
        }).findAny();
        if (opBank.isEmpty()) {
            throw new ArgumentException("No bank by that name");
        }
        return CommandArgumentResult.from(argument, opBank.get());
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandContext commandContext, @NotNull ArgumentContext argument) {
        Collection<PlayerBankAccount> banks;
        try {
            banks = this.function.parse(commandContext, argument).getValue();
        } catch (ArgumentException e) {
            return Collections.emptyList();
        }
        String peek = argument.getFocusArgument().toLowerCase();
        return banks
                .parallelStream()
                .filter(name -> name.getAccountName().toLowerCase().startsWith(peek) || (name.getAccountHolder().getPlayer().getName() + "."
                        + name.getAccountName()).toLowerCase().startsWith(peek))
                .flatMap(name -> Arrays
                        .asList(name.getAccountName(), name.getAccountHolder().getPlayer().getName() + "." + name.getAccountName())
                        .parallelStream())
                .toList();
    }

    public static @NotNull PlayerBankArgument allPlayerBanks(@NotNull String id) {
        return streamBank(id, (command, argument, stream) -> stream);
    }

    public static @NotNull PlayerBankArgument banksWithPermission(String id, BankPermission permission, Function<CommandSender, ArgumentException> notPlayer) {
        return banksWithPermission(id, new ParseCommandArgument<UUID>() {
            @Override
            public @NotNull CommandArgumentResult<UUID> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument) throws ArgumentException {
                if (context.getSource() instanceof OfflinePlayer player) {
                    return CommandArgumentResult.from(argument, player.getUniqueId());
                }
                throw notPlayer.apply(context.getSource());
            }

        }, permission);
    }

    public static @NotNull PlayerBankArgument banksWithPermission(String id, ParseCommandArgument<UUID> playerIdGetter, BankPermission permission) {
        return streamBank(id, (context, argument, stream) -> {
            UUID playerId = playerIdGetter.parse(context, argument).value();
            return stream.filter((bank) -> bank.getAccountPermissions(playerId).contains(permission));
        });
    }

    @Deprecated(forRemoval = true)
    public static @NotNull PlayerBankArgument senderBanks(@NotNull String id) {
        return new PlayerBankArgument(id, new ParseCommandArgument<Collection<PlayerBankAccount>>() {
            @Override
            public @NotNull CommandArgumentResult<Collection<PlayerBankAccount>> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument)
                    throws ArgumentException {
                if (!(context.getSource() instanceof OfflinePlayer player)) {
                    return CommandArgumentResult.from(argument, Collections.emptySet());
                }
                PlayerAccount account = AccountInterface.getManager().getPlayerAccount(player);
                return CommandArgumentResult.from(argument, account.getBanks());
            }
        });
    }

    private static PlayerBankArgument streamBank(String id, CommandFunction<Stream<PlayerBankAccount>, Stream<PlayerBankAccount>> function) {
        return new PlayerBankArgument(id, new ParseCommandArgument<>() {
            @Override
            public @NotNull CommandArgumentResult<Collection<PlayerBankAccount>> parse(@NotNull CommandContext context, @NotNull ArgumentContext argument)
                    throws ArgumentException {
                Stream<PlayerBankAccount> accounts = AccountInterface.getManager().getPlayerAccounts().stream().flatMap(player -> player.getBanks().stream());
                Stream<PlayerBankAccount> filtered = function.apply(context, argument, accounts);
                return CommandArgumentResult.from(argument, filtered.toList());
            }
        });
    }
}
