package org.kaiaccount.account.eco;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.AccountInterfaceManager;
import org.kaiaccount.account.eco.account.player.EcoPlayerAccount;
import org.kaiaccount.account.eco.commands.BukkitCommands;
import org.kaiaccount.account.eco.io.EcoSerializers;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.io.Serializable;
import org.kaiaccount.account.inter.io.Serializer;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.kaiaccount.account.inter.vault.VaultEmulationUtils;
import org.mose.command.BukkitCommandWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class EcoToolPlugin extends JavaPlugin {

    private static EcoToolPlugin plugin;

    public EcoToolPlugin() {
        plugin = this;
    }

    @Deprecated
    public static EcoToolPlugin getPlugin() {
        return getInstance();
    }

    public static EcoToolPlugin getInstance() {
        return plugin;
    }

    @Override
    public void onLoad() {
        VaultEmulationUtils.loadService(this);
        Bukkit.getServicesManager()
                .register(AccountInterfaceManager.class, new EcoManager(), this, ServicePriority.Normal);
        loadCurrencies();
    }

    @Override
    public void onEnable() {
        if (!AccountInterface.getManager().getCurrencies().isEmpty()) {
            if (AccountInterface.getManager().getCurrencies().parallelStream().noneMatch(Currency::isDefault)) {
                AccountInterface.getManager().getCurrencies().iterator().next().setDefault(true);
            }
        }
        loadNamedAccounts();
        loadPlayerAccounts();
        loadBankAccounts();
        registerCommand("balance", BukkitCommands.BALANCE);
        registerCommand("currency", BukkitCommands.CURRENCY);
        registerCommand("ecotools", BukkitCommands.ECOTOOLS);
        registerCommand("exchange", BukkitCommands.EXCHANGE);
        registerCommand("pay", BukkitCommands.PAY);
        registerCommand("bank", BukkitCommands.BANK);
        registerCommand("account", BukkitCommands.ACCOUNT);
        registerCommand("transactions", BukkitCommands.TRANSACTIONS);

        Collection<RegisteredServiceProvider<Economy>> rsp = getServer().getServicesManager().getRegistrations(Economy.class);
        getLogger().warning("Economy found: " + rsp.size());

    }

    private void registerCommand(@NotNull String command, @NotNull BukkitCommandWrapper wrapper) {
        PluginCommand bCommand = this.getCommand(command);
        if (bCommand == null) {
            throw new RuntimeException("Unknown command of " + command);
        }
        bCommand.setExecutor(wrapper);
        bCommand.setTabCompleter(wrapper);
    }

    private void loadBankAccounts() {
        for (PlayerAccount account : AccountInterface.getManager().getPlayerAccounts()) {
            File folder = new File("plugins/eco/players/" + account.getPlayer().getUniqueId() + "/");
            load(folder, EcoSerializers.BANK, account::registerBank);
        }
    }

    private boolean loadPlayerAccounts() {
        File folder = new File("plugins/eco/players/" + this.getName() + "/");
        return load(folder, EcoSerializers.PLAYER,
                (player) -> AccountInterface.getManager().registerPlayerAccount(player));
    }

    private boolean loadNamedAccounts() {
        File folder = new File("plugins/eco/named/" + this.getName() + "/");
        return load(folder, EcoSerializers.NAMED_ACCOUNT, (account) -> AccountInterface.getManager().registerNamedAccount(account));
    }

    public EcoPlayerAccount loadPlayerAccount(@NotNull UUID player) throws IllegalStateException {
        File file = new File("plugins/eco/players/" + this.getName() + "/" + player + ".yml");
        if (!file.exists()) {
            throw new IllegalStateException("No file for player");
        }
        try {
            return loadSingle(file, EcoSerializers.PLAYER);
        } catch (IOException e) {
            throw new RuntimeException("load error", e);
        }
    }

    private boolean loadCurrencies() {
        File folder = new File("plugins/eco/currencies/" + this.getName() + "/");
        return load(folder, EcoSerializers.CURRENCY, (c) -> AccountInterface.getManager().registerCurrency(c));
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Serializable<T>> T loadSingle(@NotNull File file, @NotNull Serializer<T> serializer) throws
            IOException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return serializer.deserialize(config);
    }

    private <T extends Serializable<T>> boolean load(File folder, Serializer<T> serializer, Consumer<T> onEach) {
        File[] files = folder.listFiles();
        if (files == null) {
            return false;
        }
        Arrays
                .stream(files)
                .parallel()
                .map(file -> {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    try {
                        return serializer.deserialize(config);
                    } catch (IOException e) {
                        e.printStackTrace();
                        //noinspection ReturnOfNull
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(onEach);
        return true;
    }
}
