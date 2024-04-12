package org.kaiaccount.account.eco.account.bank;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.account.history.EntryTransactionHistory;
import org.kaiaccount.account.eco.account.history.EntryTransactionHistoryBuilder;
import org.kaiaccount.account.eco.account.history.SimpleEntryTransactionHistory;
import org.kaiaccount.account.eco.account.history.TransactionHistory;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.io.Serializer;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccountBuilder;
import org.kaiaccount.account.inter.type.player.PlayerAccount;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

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

        TransactionHistory history = value.getTransactionHistory();
        configuration.set("transactions.size", history.size());
        for (int index = 0; index < history.size(); index++) {
            EntryTransactionHistory entry = history.get(index);
            String initialKey = "transactions.index" + index;
            LocalDateTime time = entry.getTime();
            configuration.set(initialKey + ".currency.plugin", entry.getCurrency().getPlugin().getName());
            configuration.set(initialKey + ".currency.name", entry.getCurrency().getKeyName());
            configuration.set(initialKey + ".amount", entry.getAmount().doubleValue());
            configuration.set(initialKey + ".plugin", entry.getPluginName());
            configuration.set(initialKey + ".from", entry.getFromName().orElse(null));
            configuration.set(initialKey + ".reason", entry.getReason().orElse(null));
            configuration.set(initialKey + ".type", entry.getTransactionType().name());
            configuration.set(initialKey + ".time.year", time.getYear());
            configuration.set(initialKey + ".time.month", time.getMonthValue());
            configuration.set(initialKey + ".time.day", time.getDayOfMonth());
            configuration.set(initialKey + ".time.hour", time.getHour());
            configuration.set(initialKey + ".time.minute", time.getMinute());
            configuration.set(initialKey + ".time.seconds", time.getSecond());
        }
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
        EcoBankAccount account = new EcoBankAccount(new PlayerBankAccountBuilder().setAccount(owner)
                .setName(bankName)
                .setAccountHolders(accounts)
                .setInitialBalance(balance));

        int transactionSize = configuration.getInt("transactions.size");
        TransactionHistory transactionHistory = account.getTransactionHistory();
        for (int index = 0; index < transactionSize; index++) {
            String initialKey = "transactions.index" + index;

            String currencyPluginName = configuration.getString(initialKey + ".currency.plugin");
            String currencyKeyName = configuration.getString(initialKey + ".currency.name");
            double transactionAmount = configuration.getDouble(initialKey + ".amount");
            String transactionPluginName = configuration.getString(initialKey + ".plugin");
            String from = configuration.getString(initialKey + ".from");
            String reason = configuration.getString(initialKey + ".reason");
            String typeName = configuration.getString(initialKey + ".type");
            int year = configuration.getInt(initialKey + ".time.year");
            int month = configuration.getInt(initialKey + ".time.month");
            int day = configuration.getInt(initialKey + ".time.day");
            int hours = configuration.getInt(initialKey + ".time.hour");
            int minutes = configuration.getInt(initialKey + ".time.minute");
            int seconds = configuration.getInt(initialKey + ".time.seconds");

            //This is horrible.... Why hasn't Spigot allowed predefined parsers yet?
            Logger logger = EcoToolPlugin.getInstance().getLogger();
            String prefixErrorMessage = "Could not load player bank transaction from '" + account.getAccountName() + "'-" + index + ": ";
            LocalDateTime time = LocalDateTime.of(year, month, day, hours, minutes, seconds);

            if (currencyPluginName == null) {
                logger.warning(prefixErrorMessage + "Currency plugin is invalid");
                continue;
            }
            if (currencyKeyName == null) {
                logger.warning(prefixErrorMessage + "Currency keyname is invalid");
                continue;
            }
            Plugin currencyPlugin = Bukkit.getPluginManager().getPlugin(currencyPluginName);
            if (currencyPlugin == null) {
                logger.warning(prefixErrorMessage + "Currency plugin is no longer installed");
                continue;
            }
            Optional<Currency<?>> opCurrency = AccountInterface.getManager().getCurrency(currencyPlugin, currencyKeyName);
            if (opCurrency.isEmpty()) {
                logger.warning(prefixErrorMessage + "Currency is no longer active");
                continue;
            }
            if (typeName == null) {
                logger.warning(prefixErrorMessage + "type is invalid");
                continue;
            }
            TransactionType type;
            try {
                type = TransactionType.valueOf(typeName);
            } catch (Exception e) {
                logger.warning(prefixErrorMessage + e.getMessage());
                continue;
            }
            if (transactionPluginName == null) {
                logger.warning(prefixErrorMessage + "plugin name is invalid");
                continue;
            }
            SimpleEntryTransactionHistory history = new EntryTransactionHistoryBuilder()
                    .setAccount(account)
                    .setAmount(transactionAmount)
                    .setCurrency(opCurrency.get())
                    .setReason(reason)
                    .setFromName(from)
                    .setTime(time)
                    .setPluginName(transactionPluginName)
                    .setType(type)
                    .build();
            transactionHistory.add(history);
        }

        return account;
    }
}
