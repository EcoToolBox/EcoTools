package org.kaiaccount.account.eco.payment;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.account.named.EcoNamedAccount;
import org.kaiaccount.account.eco.currency.EcoCurrency;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.currency.CurrencyBuilder;
import org.kaiaccount.account.inter.transfer.TransactionType;
import org.kaiaccount.account.inter.transfer.payment.KaiPayment;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.successful.SuccessfulTransactionResult;
import org.kaiaccount.account.inter.type.AccountSynced;
import org.kaiaccount.account.inter.type.named.NamedAccountBuilder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class NamedAccountPaymentTests {

    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<EcoToolPlugin> ecoToolPluginMock;

    @BeforeEach
    public void setup() {
        PluginManager pluginManager = Mockito.mock(PluginManager.class);


        //mock bukkit
        bukkitMock = Mockito.mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getPluginManager).thenReturn(pluginManager);

        //mock EcoTool
        ecoToolPluginMock = Mockito.mockStatic(EcoToolPlugin.class);
    }

    @AfterEach
    public void close() {
        bukkitMock.close();
        ecoToolPluginMock.close();
    }


    @Test
    public void testDeposit() {
        EcoToolPlugin plugin = Mockito.mock(EcoToolPlugin.class);
        Mockito.when(plugin.getName()).thenReturn("plugin");
        ecoToolPluginMock.when(EcoToolPlugin::getInstance).thenReturn(plugin);
        Currency<EcoCurrency> currency = new EcoCurrency(new CurrencyBuilder()
                .setName("gbp")
                .setDefault(true)
                .setDisplayNameMultiple("pounds")
                .setDisplayNameShort("pound")
                .setSymbol("£")
                .setPlugin(plugin));
        AccountSynced account = new EcoNamedAccount(new NamedAccountBuilder().setAccountName("test account"));
        Payment payment = new KaiPayment(new PaymentBuilder().setAmount(5).setCurrency(currency), plugin);

        //act
        SingleTransactionResult result = account.depositSynced(payment);

        //assert
        Assertions.assertEquals(TransactionType.DEPOSIT, result.getTransaction().getType());
        Assertions.assertEquals(payment, result.getTransaction().getPayment());
        Assertions.assertInstanceOf(SuccessfulTransactionResult.class, result);
    }
}
