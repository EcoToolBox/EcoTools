package org.kaiaccount.account.eco.commands.bank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.account.bank.EcoBankAccount;
import org.kaiaccount.account.eco.commands.argument.account.PlayerBankArgument;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.CommandArgumentResult;
import org.mose.command.arguments.collection.source.UserArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.operation.RemainingArgument;
import org.mose.command.arguments.operation.permission.PermissionOrArgument;
import org.mose.command.arguments.simple.EnumArgument;
import org.mose.command.builder.CommandBuilder;
import org.mose.command.exception.ArgumentException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerBankAccessCommands {

    public static ArgumentCommand addPlayerCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<OfflinePlayer> targetUserArgument = targetUserAddArgument();
            CommandArgument<PlayerBankAccount> bankArgument = PlayerBankArgument.banksWithPermission("bank",
                    (context, argument) -> CommandArgumentResult.from(argument, context.getArgument(supplier, targetUserArgument).getUniqueId()),
                    BankPermission.ACCOUNT_OWNER);
            CommandArgument<OfflinePlayer> userArgument = new UserArgument("user", (command, argument) -> {
                OfflinePlayer targetUser = command.getArgument(supplier, targetUserArgument);
                return Arrays.stream(Bukkit.getOfflinePlayers()).filter(user -> !user.equals(targetUser));
            });
            CommandArgument<List<BankPermission>> permissionArgument = permissionArgument();

            return builder
                    .setDescription("Add a player to access your bank account")
                    .setPermissionNode(Permissions.GRANT_BANK_ACCESS_OTHER.getPermissionNode())
                    .addArguments(targetUserArgument, new ExactArgument("access"), new ExactArgument("allow"), bankArgument, userArgument, permissionArgument)
                    .setExecutor((context, raw) -> {
                        PlayerBankAccount bank = context.getArgument(supplier, bankArgument);
                        OfflinePlayer user = context.getArgument(supplier, userArgument);
                        List<BankPermission> permissions = context.getArgument(supplier, permissionArgument);

                        if (user.equals(bank.getAccountHolder().getPlayer())) {
                            context.getSource().sendMessage(ChatColor.RED + "Cannot add bank owner");
                            return false;
                        }

                        Map<UUID, Collection<BankPermission>> accounts = bank.getAccounts();
                        Collection<BankPermission> currentPermissions = accounts.get(user.getUniqueId());
                        if (currentPermissions == null) {
                            bank.addAccount(user, permissions);
                            context
                                    .getSource()
                                    .sendMessage("Added " + user.getName() + " with " + permissions
                                            .stream()
                                            .map(t -> t.name().toLowerCase())
                                            .collect(Collectors.joining(", ")));
                            return true;
                        }
                        bank.removeAccount(user.getUniqueId());
                        bank.addAccount(user.getUniqueId(), permissions);
                        if (bank instanceof EcoBankAccount bankAccount) {
                            try {
                                bankAccount.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        context
                                .getSource()
                                .sendMessage("Updated " + user.getName() + " with " + permissions
                                        .stream()
                                        .map(t -> t.name().toLowerCase())
                                        .collect(Collectors.joining(", ")));

                        return true;
                    })
                    .build();
        });
    }

    private static CommandArgument<List<BankPermission>> permissionArgument() {
        EnumArgument<BankPermission> bankPermissionsArgument = new EnumArgument<>("permission", BankPermission.class);
        RemainingArgument<BankPermission> remainingPermissionsArgument = new RemainingArgument<>(bankPermissionsArgument);
        return new OptionalArgument<>(remainingPermissionsArgument, List.of(BankPermission.SEE, BankPermission.GIVE));
    }

    public static ArgumentCommand removePlayerCommand() {
        return CommandBuilder.build((supplier, builder) -> {
            CommandArgument<OfflinePlayer> targetUserArgument = targetUserRemoveArgument();
            CommandArgument<PlayerBankAccount> bankArgument = PlayerBankArgument.banksWithPermission("bank",
                    (context, argument) -> CommandArgumentResult.from(argument, context.getArgument(supplier, targetUserArgument).getUniqueId()),
                    BankPermission.ACCOUNT_OWNER);
            CommandArgument<OfflinePlayer> userArgument = new UserArgument("user", (command, argument) -> {
                OfflinePlayer target = command.getArgument(supplier, targetUserArgument);
                return Arrays.stream(Bukkit.getOfflinePlayers()).filter(user -> !user.equals(target)).filter(user -> {
                    PlayerBankAccount bank = command.getArgument(supplier, bankArgument);
                    return bank.getAccounts().get(user.getUniqueId()) != null;
                });
            });

            return builder
                    .addArguments(targetUserArgument, new ExactArgument("access"), new ExactArgument("deny"), bankArgument, userArgument)
                    .setDescription("Remove a player to access your bank account")
                    .setPermissionNode(Permissions.GRANT_BANK_ACCESS_SELF.getPermissionNode())
                    .setExecutor((context, raw) -> {
                        PlayerBankAccount bank = context.getArgument(supplier, bankArgument);
                        OfflinePlayer user = context.getArgument(supplier, userArgument);

                        if (user.equals(bank.getAccountHolder().getPlayer())) {
                            context.getSource().sendMessage(ChatColor.RED + "Cannot add bank owner");
                            return false;
                        }

                        Map<UUID, Collection<BankPermission>> accounts = bank.getAccounts();
                        Collection<BankPermission> currentPermissions = accounts.get(user.getUniqueId());
                        if (currentPermissions == null) {
                            return true;
                        }
                        bank.removeAccount(user.getUniqueId());
                        context.getSource().sendMessage("Removed " + user.getName() + " access to bank");
                        return true;
                    })
                    .build();
        });
    }

    private static CommandArgument<OfflinePlayer> targetUserAddArgument() {
        UserArgument targetUserArgument = new UserArgument("target",
                (command, context) -> Arrays
                        .stream(Bukkit.getOfflinePlayers())
                        .filter(player -> !AccountInterface.getManager().getPlayerAccount(player).getAttachedOwningBanks().isEmpty()));
        PermissionOrArgument<OfflinePlayer> targetUserPermissionArgument = new PermissionOrArgument<>("target",
                source -> source.hasPermission(Permissions.GRANT_BANK_ACCESS_OTHER.getPermissionNode()),
                targetUserArgument);
        return new OptionalArgument<OfflinePlayer>(targetUserPermissionArgument, ((context, argument) -> {
            if (!(context.getSource() instanceof OfflinePlayer offline)) {
                throw new ArgumentException("Requires players");
            }
            return CommandArgumentResult.from(argument, offline);
        }));
    }

    private static CommandArgument<OfflinePlayer> targetUserRemoveArgument() {
        UserArgument targetUserArgument = new UserArgument("target",
                (command, context) -> Arrays
                        .stream(Bukkit.getOfflinePlayers())
                        .filter(player -> AccountInterface
                                .getManager()
                                .getPlayerAccount(player)
                                .getAttachedOwningBanks()
                                .stream()
                                .anyMatch(bank -> bank.getAccounts().size() > 1)));
        PermissionOrArgument<OfflinePlayer> targetUserPermissionArgument = new PermissionOrArgument<>("target",
                source -> source.hasPermission(Permissions.GRANT_BANK_ACCESS_OTHER.getPermissionNode()),
                targetUserArgument);
        return new OptionalArgument<OfflinePlayer>(targetUserPermissionArgument, ((context, argument) -> {
            if (!(context.getSource() instanceof OfflinePlayer offline)) {
                throw new ArgumentException("Requires players");
            }
            return CommandArgumentResult.from(argument, offline);
        }));
    }

}
