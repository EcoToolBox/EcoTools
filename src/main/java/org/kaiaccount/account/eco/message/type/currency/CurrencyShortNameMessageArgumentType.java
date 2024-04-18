package org.kaiaccount.account.eco.message.type.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.type.MessageArgumentType;
import org.kaiaccount.account.inter.currency.Currency;

public class CurrencyShortNameMessageArgumentType implements MessageArgumentType<Currency<?>>, CurrencyMessageArgumentType {
	@NotNull
	@Override
	public String getDefaultArgumentHandler() {
		return "currency short name";
	}

	@NotNull
	@Override
	public String apply(Currency<?> input) {
		return input.getDisplayNameShort();
	}

}
