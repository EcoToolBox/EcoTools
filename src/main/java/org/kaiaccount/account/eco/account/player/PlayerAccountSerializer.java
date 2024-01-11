package org.kaiaccount.account.eco.account.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import org.kaiaccount.account.inter.type.player.PlayerAccountBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PlayerAccountSerializer implements Serializer<EcoPlayerAccount> {

    @Override
    public void serialize(@NotNull YamlConfiguration configuration, @NotNull EcoPlayerAccount value) {
        value.getBalances()
                .forEach((currency, amount) -> configuration.set(
                        "balance." + currency.getPlugin().getName() + "." + currency.getKeyName(),
                        amount.doubleValue()));
        configuration.set("id", value.getPlayer().getUniqueId().toString());
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
    public EcoPlayerAccount deserialize(@NotNull YamlConfiguration configuration) throws IOException {
        Map<Currency<?>, BigDecimal> amount =
                AccountInterface.getManager()
                        .getCurrencies()
                        .parallelStream()
                        .map(currency -> {
                            double value = configuration.getDouble(
                                    "balance." + currency.getPlugin().getName() + "." + currency.getKeyName());
                            return new AbstractMap.SimpleImmutableEntry<>(currency, value);
                        })
                        .filter(entry -> entry.getValue() != 0.0)
                        .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey,
                                value -> BigDecimal.valueOf(value.getValue())));
        String accountId = configuration.getString("id");
        if (accountId == null) {
            throw new IOException("Account is missing from file: " + configuration.getName());
        }
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(accountId));

        EcoPlayerAccount ecoPlayer = new EcoPlayerAccount(new PlayerAccountBuilder().setPlayer(player).setInitialBalance(amount));

        int transactionSize = configuration.getInt("transactions.size");
        TransactionHistory transactionHistory = ecoPlayer.getTransactionHistory();
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
            Logger logger = EcoToolPlugin.getPlugin().getLogger();
            String prefixErrorMessage = "Could not load player transaction from '" + player.getName() + "'-" + index + ": ";
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
                    .setAccount(ecoPlayer)
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

        return ecoPlayer;
    }
}
