package org.kaiaccount.account.eco;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.kaiaccount.AccountInterfaceManager;
import org.kaiaccount.account.eco.account.named.EcoNamedAccount;
import org.kaiaccount.account.eco.account.player.EcoPlayerAccount;
import org.kaiaccount.account.eco.bank.EcoBankAccount;
import org.kaiaccount.account.eco.currency.EcoCurrency;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.currency.ToCurrency;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.ToNamedAccount;
import org.kaiaccount.account.inter.type.named.bank.player.ToBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccountBuilder;
import org.kaiaccount.account.inter.type.player.ToPlayerAccount;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

public class EcoManager implements AccountInterfaceManager {
    private final Collection<Currency<?>> currencies = new LinkedTransferQueue<>();
    private final Collection<PlayerAccount> playerAccounts = new LinkedBlockingQueue<>();
    private final Collection<NamedAccount> namedAccounts = new LinkedBlockingQueue<>();

    @Override
    public @NotNull EcoToolPlugin getVaultPlugin() {
        return EcoToolPlugin.getPlugin();
    }

    @Override
    public @NotNull ToCurrency getToCurrencies() {
        return EcoCurrency::new;
    }

    @Override
    public @NotNull ToBankAccount getToBankAccount() {
        return EcoBankAccount::new;
    }

    @Override
    public @NotNull ToPlayerAccount getToPlayerAccount() {
        return EcoPlayerAccount::new;
    }

    @Override
    public @NotNull ToNamedAccount getToNamedAccount() {
        return EcoNamedAccount::new;
    }

    @Override
    public @NotNull Collection<Currency<?>> getCurrencies() {
        return Collections.unmodifiableCollection(this.currencies);
    }

    @Override
    public @NotNull Collection<PlayerAccount> getPlayerAccounts() {
        return Collections.unmodifiableCollection(this.playerAccounts);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<NamedAccount> getNamedAccounts() {
        return Collections.unmodifiableCollection(this.namedAccounts);
    }

    @Override
    public void registerPlayerAccount(@NotNull PlayerAccount account) {
        this.playerAccounts.add(account);
    }

    @Override
    public void registerNamedAccount(@NotNull NamedAccount account) {
        if (this.namedAccounts.contains(account)) {
            throw new IllegalArgumentException("Account is already registered");
        }
        this.namedAccounts.add(account);
    }

    @Override
    public @NotNull PlayerAccount loadPlayerAccount(@NotNull OfflinePlayer player) {
        PlayerAccount account;
        try {
            account = EcoToolPlugin.getPlugin().loadPlayerAccount(player.getUniqueId());
        } catch (IllegalStateException e) {
            account = new EcoPlayerAccount(new PlayerAccountBuilder().setPlayer(player));
        }
        this.registerPlayerAccount(account);
        return account;
    }

    @Override
    public void deregisterPlayerAccount(@NotNull PlayerAccount account) {
        this.playerAccounts.remove(account);
    }

    @Override
    public void deregisterNamedAccount(@NotNull NamedAccount account) {
        this.namedAccounts.remove(account);
    }

    @Override
    public void registerCurrency(@NotNull Currency<?> currency) {
        this.currencies.add(currency);
    }

    @Override
    public void deregisterCurrency(@NotNull Currency<?> currency) {
        this.currencies.remove(currency);
    }
}
