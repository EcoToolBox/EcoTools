package org.kaiaccount.account.eco.io;

import org.kaiaccount.account.eco.account.bank.BankSerializer;
import org.kaiaccount.account.eco.account.named.NamedAccountSerializer;
import org.kaiaccount.account.eco.account.player.PlayerAccountSerializer;
import org.kaiaccount.account.eco.currency.EcoCurrencySerializer;

public final class EcoSerializers {

    public static final BankSerializer BANK = new BankSerializer();
    public static final EcoCurrencySerializer CURRENCY = new EcoCurrencySerializer();
    public static final PlayerAccountSerializer PLAYER = new PlayerAccountSerializer();
    public static final NamedAccountSerializer NAMED_ACCOUNT = new NamedAccountSerializer();

    private EcoSerializers() {
        throw new RuntimeException("Dont do that");
    }
}
