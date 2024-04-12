package org.kaiaccount.account.eco.currency;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.inter.currency.CurrencyBuilder;
import org.kaiaccount.account.inter.io.Serializer;

import java.io.IOException;
import java.math.BigDecimal;

public class EcoCurrencySerializer implements Serializer<EcoCurrency> {

	public static final String NAME = "meta.name";
	public static final String SYMBOL = "meta.symbol";
	public static final String WORTH = "meta.worth";
	public static final String SHORT_DISPLAY_NAME = "display.short";
	public static final String SINGLE_DISPLAY_NAME = "display.single";
	public static final String MULTIPLE_DISPLAY_NAME = "display.multiple";

	@Override
	public void serialize(@NotNull YamlConfiguration configuration, @NotNull EcoCurrency value) {
		configuration.set(NAME, value.getKeyName());
		configuration.set(SYMBOL, value.getSymbol());
		configuration.set(SHORT_DISPLAY_NAME, value.getRawDisplayNameShort());
		configuration.set(SINGLE_DISPLAY_NAME, value.getRawDisplayNameSingle());
		configuration.set(MULTIPLE_DISPLAY_NAME, value.getRawDisplayNameMultiple());
		configuration.set(WORTH, value.getWorth().map(BigDecimal::doubleValue).orElse(null));
	}

	@Override
	public EcoCurrency deserialize(@NotNull YamlConfiguration configuration) throws IOException {
		String name = configuration.getString(NAME);
		if (name == null) {
			throw new IOException("Currency name could not be found in yaml " + configuration.getName());
		}
		String symbol = configuration.getString(SYMBOL);
		if (symbol == null) {
			throw new IOException("Currency sign could not be found in yaml " + configuration.getName());
		}
		String shortName = configuration.getString(SHORT_DISPLAY_NAME);
		String singleName = configuration.getString(SINGLE_DISPLAY_NAME);
		String multiName = configuration.getString(MULTIPLE_DISPLAY_NAME);
		Double worth = null;
		if (configuration.isDouble(WORTH)) {
			worth = configuration.getDouble(WORTH);
		}

		return new EcoCurrency(new CurrencyBuilder().setSymbol(symbol)
				.setName(name)
				.setPlugin(EcoToolPlugin.getInstance())
				.setWorth(worth)
				.setDisplayNameMultiple(multiName)
				.setDisplayNameSingle(singleName)
				.setDisplayNameShort(shortName));
	}
}
