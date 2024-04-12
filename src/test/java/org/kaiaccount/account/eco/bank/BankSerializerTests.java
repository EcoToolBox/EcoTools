package org.kaiaccount.account.eco.bank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.FakeGlobalManager;
import org.kaiaccount.account.eco.FakePlayerAccount;
import org.kaiaccount.account.eco.currency.EcoCurrency;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.currency.CurrencyBuilder;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccountBuilder;
import org.kaiaccount.account.inter.type.player.PlayerAccount;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankSerializerTests {

    private final FakeGlobalManager manager = new FakeGlobalManager();
    private MockedStatic<Bukkit> bukkitMocked;
    private MockedStatic<AccountInterface> accountMocked;

    @BeforeEach
    public void setup() {
        bukkitMocked = Mockito.mockStatic(Bukkit.class);
        accountMocked = Mockito.mockStatic(AccountInterface.class);
        accountMocked.when(AccountInterface::getManager).thenReturn(manager);
    }

    @AfterEach
    public void close() {
        bukkitMocked.close();
        accountMocked.close();
    }

    @Test
    public void testCanReadBasic() {
        //setup
        String bankName = "Test Bank Name";
        UUID bankOwner = UUID.randomUUID();

        YamlConfiguration configuration = Mockito.mock(YamlConfiguration.class);
        Mockito.when(configuration.getString(BankSerializer.BANK_NAME)).thenReturn(bankName);
        Mockito.when(configuration.getString(BankSerializer.BANK_OWNER)).thenReturn(bankOwner.toString());

        OfflinePlayer mockedPlayer = Mockito.mock(OfflinePlayer.class);
        Mockito.when(mockedPlayer.getUniqueId()).thenReturn(bankOwner);

        Server server = Mockito.mock(Server.class);
        Mockito.when(server.getOfflinePlayer(bankOwner)).thenReturn(mockedPlayer);
        bukkitMocked.when(Bukkit::getServer).thenReturn(server);

        PlayerAccount playerAccount = new FakePlayerAccount(mockedPlayer);
        manager.playerAccounts.add(playerAccount);

        //run
        EcoBankAccount account;
        try {
            account = new BankSerializer().deserialize(configuration);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Failed to read file:");
            return;
        }

        //test
        Assertions.assertEquals(bankName, account.getAccountName());
        Assertions.assertEquals(bankOwner, account.getAccountHolder().getPlayer().getUniqueId());
    }

    @Test
    public void testCanWriteBasic() {
        //setup
        String bankName = "Test Bank Name";
        UUID bankOwner = UUID.randomUUID();

        OfflinePlayer mockedPlayer = Mockito.mock(OfflinePlayer.class);
        Mockito.when(mockedPlayer.getUniqueId()).thenReturn(bankOwner);

        PlayerAccount ownerAccount = new FakePlayerAccount(mockedPlayer);

        YamlConfiguration configuration = Mockito.mock(YamlConfiguration.class);
        EcoBankAccount bankAccount =
                new EcoBankAccount(new PlayerBankAccountBuilder().setName(bankName).setAccount(ownerAccount));

        //run
        new BankSerializer().serialize(configuration, bankAccount);

        //test
        Mockito.verify(configuration, Mockito.times(1)).set(BankSerializer.BANK_OWNER, bankOwner);
        Mockito.verify(configuration, Mockito.times(1)).set(BankSerializer.BANK_NAME, bankName);
    }

    @Test
    public void testCanWriteWithBalances() {
        //setup
        String bankName = "Test Bank Name";
        String pluginName = "plugin";
        String currencyName = "test";
        UUID bankOwner = UUID.randomUUID();
        Map<Currency<?>, BigDecimal> balance = new HashMap<>();

        Plugin plugin = Mockito.mock(Plugin.class);
        Mockito.when(plugin.getName()).thenReturn(pluginName);

        Currency<?> accountCurrency =
                new EcoCurrency(new CurrencyBuilder().setName(currencyName).setPlugin(plugin).setSymbol("t"));
        BigDecimal amount = BigDecimal.TEN;

        balance.put(accountCurrency, amount);

        OfflinePlayer mockedPlayer = Mockito.mock(OfflinePlayer.class);
        Mockito.when(mockedPlayer.getUniqueId()).thenReturn(bankOwner);

        PlayerAccount ownerAccount = new FakePlayerAccount(mockedPlayer);

        YamlConfiguration configuration = Mockito.mock(YamlConfiguration.class);
        EcoBankAccount bankAccount = new EcoBankAccount(
                new PlayerBankAccountBuilder().setName(bankName).setAccount(ownerAccount).setInitialBalance(balance));

        //run
        new BankSerializer().serialize(configuration, bankAccount);

        //test
        Mockito.verify(configuration, Mockito.times(1)).set(BankSerializer.BANK_OWNER, bankOwner);
        Mockito.verify(configuration, Mockito.times(1)).set(BankSerializer.BANK_NAME, bankName);
        Mockito.verify(configuration, Mockito.times(1)).set(
                String.join(".", BankSerializer.ACCOUNT_BALANCE, pluginName, currencyName), amount.doubleValue());
    }
}
