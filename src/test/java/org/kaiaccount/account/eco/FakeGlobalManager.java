package org.kaiaccount.account.eco;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.kaiaccount.AccountInterfaceManager;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.currency.ToCurrency;
import org.kaiaccount.account.inter.type.named.NamedAccount;
import org.kaiaccount.account.inter.type.named.ToNamedAccount;
import org.kaiaccount.account.inter.type.named.bank.player.ToBankAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.kaiaccount.account.inter.type.player.ToPlayerAccount;

import java.util.Collection;
import java.util.LinkedList;

public class FakeGlobalManager implements AccountInterfaceManager {

    public final Collection<Currency<?>> currencies = new LinkedList<>();
    public final Collection<PlayerAccount> playerAccounts = new LinkedList<>();
    public final Collection<NamedAccount> namedAccounts = new LinkedList<>();
    public ToCurrency toCurrency;
    public ToBankAccount toBankAccount;

    @Override
    public @NotNull Plugin getVaultPlugin() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public @NotNull ToCurrency getToCurrencies() {
        return this.toCurrency;
    }

    @Override
    public @NotNull ToBankAccount getToBankAccount() {
        return this.toBankAccount;
    }

    @NotNull
    @Override
    public Collection<Currency<?>> getCurrencies() {
        return this.currencies;
    }

    @NotNull
    @Override
    public Collection<PlayerAccount> getPlayerAccounts() {
        return this.playerAccounts;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<NamedAccount> getNamedAccounts() {
        return this.namedAccounts;
    }

    @Override
    public @NotNull ToPlayerAccount getToPlayerAccount() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public @NotNull ToNamedAccount getToNamedAccount() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void registerPlayerAccount(@NotNull PlayerAccount account) {
        throw new RuntimeException("Not implemented yet");

    }

    @Override
    public void registerNamedAccount(@NotNull NamedAccount account) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public @NotNull PlayerAccount loadPlayerAccount(@NotNull OfflinePlayer player) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void deregisterPlayerAccount(@NotNull PlayerAccount account) {
        throw new RuntimeException("Not implemented yet");

    }

    @Override
    public void deregisterNamedAccount(@NotNull NamedAccount account) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void registerCurrency(@NotNull Currency<?> currency) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void deregisterCurrency(@NotNull Currency<?> currency) {
        throw new RuntimeException("Not implemented yet");
    }
}
