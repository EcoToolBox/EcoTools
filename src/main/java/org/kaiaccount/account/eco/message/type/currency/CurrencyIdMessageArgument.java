package org.kaiaccount.account.eco.message.type.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.type.MessageArgumentType;
import org.kaiaccount.account.inter.currency.Currency;

public class CurrencyIdMessageArgument implements MessageArgumentType<Currency<?>>, CurrencyMessageArgument {
	@NotNull
	@Override
	public String getDefaultArgumentHandler() {
		return "currency id";
	}

	@NotNull
	@Override
	public String apply(Currency<?> input) {
		return input.getPlugin().getName() + "." + input.getKeyName();
	}
}
