package org.kaiaccount.account.eco.bank;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.io.Serializer;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccountBuilder;
import org.kaiaccount.account.inter.type.player.PlayerAccount;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class BankSerializer implements Serializer<EcoBankAccount> {

    public static final String BANK_NAME = "meta.name";
    public static final String BANK_OWNER = "meta.owner";
    public static final String ACCOUNT_BALANCE = "account.balance";
    public static final String ACCOUNT_ACCESSORS = "account.accessors";

    @Override
    public void serialize(@NotNull YamlConfiguration configuration, @NotNull EcoBankAccount value) {
        configuration.set(BANK_NAME, value.getAccountName());
        configuration.set(BANK_OWNER, value.getAccountHolder().getPlayer().getUniqueId().toString());
        Map<UUID, Collection<BankPermission>> accounts = new HashMap<>(value.getAccounts());
        accounts.remove(value.getAccountHolder().getPlayer().getUniqueId());

        accounts
                .forEach((account, permission) -> configuration.set(ACCOUNT_ACCESSORS + "." + account.toString(),
                        permission.parallelStream().map(Enum::name).toList()));
        value.getBalances()
                .forEach((currency, amount) -> configuration.set(
                        ACCOUNT_BALANCE + "." + currency.getPlugin().getName() + "." + currency.getKeyName(),
                        amount.doubleValue()));
    }

    @Override
    public EcoBankAccount deserialize(@NotNull YamlConfiguration configuration) throws IOException {
        String bankName = configuration.getString(BANK_NAME);
        if (bankName == null) {
            throw new IOException("Cannot read bank name");
        }
        String bankOwnerString = configuration.getString(BANK_OWNER);
        if (bankOwnerString == null) {
            throw new IOException("Cannot read bank owner");
        }
        UUID bankOwnerId;
        try {
            bankOwnerId = UUID.fromString(bankOwnerString);
        } catch (NumberFormatException e) {
            throw new IOException("Cannot read UUID of bank owner");
        }
        Map<UUID, Collection<BankPermission>> accounts = new HashMap<>();
        Map<Currency<?>, BigDecimal> balance = new HashMap<>();

        ConfigurationSection accountSection = configuration.getConfigurationSection(ACCOUNT_ACCESSORS);
        if (accountSection != null) {
            for (String accountIdString : accountSection.getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(accountIdString);
                } catch (NumberFormatException e) {
                    EcoToolPlugin.getInstance()
                            .getLogger()
                            .warning("Cannot read account accessor's UUID of '"
                                    + accountIdString
                                    + "' in yaml "
                                    + configuration.getName()
                                    + ". skipping");
                    continue;
                }
                List<String> permissionsString = accountSection.getStringList(accountIdString);
                List<BankPermission> permissions =
                        permissionsString.parallelStream().map(BankPermission::valueOf).toList();
                accounts.put(uuid, permissions);
            }
        }

        ConfigurationSection balanceSection = configuration.getConfigurationSection(ACCOUNT_BALANCE);
        if (balanceSection != null) {
            for (String pluginName : balanceSection.getKeys(false)) {
                ConfigurationSection currencyNameSection = balanceSection.getConfigurationSection(pluginName);
                if (currencyNameSection == null) {
                    EcoToolPlugin.getInstance()
                            .getLogger()
                            .warning("Could not read the currencies of the plugin '"
                                    + pluginName
                                    + "' in yaml "
                                    + configuration.getName() + ". Skipping");
                    continue;
                }
                for (String currencyName : currencyNameSection.getKeys(false)) {
                    double amount = currencyNameSection.getDouble(currencyName);
                    Optional<Currency<?>> opCurrency = AccountInterface.getManager()
                            .getCurrencies()
                            .parallelStream()
                            .filter(cur -> cur.getPlugin().getName().equals(pluginName))
                            .filter(cur -> cur.getKeyName().equals(currencyName))
                            .findAny();
                    if (opCurrency.isEmpty()) {
                        EcoToolPlugin.getInstance()
                                .getLogger()
                                .warning("Could not find the currency of "
                                        + pluginName
                                        + "."
                                        + currencyName
                                        + " in yaml " + configuration.getName() + ". Skipping");
                        continue;
                    }
                    balance.put(opCurrency.get(), BigDecimal.valueOf(amount));
                }
            }
        }
        PlayerAccount owner = AccountInterface.getManager().getPlayerAccount(bankOwnerId);
        return new EcoBankAccount(new PlayerBankAccountBuilder().setAccount(owner)
                .setName(bankName)
                .setAccountHolders(accounts)
                .setInitialBalance(balance));
    }
}
