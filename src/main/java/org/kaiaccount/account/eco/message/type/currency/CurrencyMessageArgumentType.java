package org.kaiaccount.account.eco.message.type.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.message.type.MessageArgumentType;
import org.kaiaccount.account.inter.currency.Currency;

public interface CurrencyMessageArgumentType extends MessageArgumentType<Currency<?>> {

	@NotNull
	@Override
	default Class<Currency<?>> getClassType() {
		return (Class<Currency<?>>) (Object) Currency.class;
	}
}
