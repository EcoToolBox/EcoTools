package org.kaiaccount.account.eco.commands.currency;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.permission.Permissions;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.currency.CurrencyBuilder;
import org.mose.command.ArgumentCommand;
import org.mose.command.CommandArgument;
import org.mose.command.arguments.operation.ExactArgument;
import org.mose.command.arguments.operation.OptionalArgument;
import org.mose.command.arguments.simple.number.DoubleArgument;
import org.mose.command.arguments.simple.text.StringArgument;
import org.mose.command.context.CommandContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddCurrencyCommand implements ArgumentCommand {

	public static final ExactArgument ADD = new ExactArgument("add");
	public static final StringArgument SYMBOL = new StringArgument("symbol");
	public static final StringArgument NAME = new StringArgument("name");
	public static final OptionalArgument<String> SINGLE_DISPLAY = new OptionalArgument<>(new StringArgument("single"));
	public static final OptionalArgument<String> MULTI_DISPLAY = new OptionalArgument<>(new StringArgument("multi"));
	public static final OptionalArgument<String> SHORT_DISPLAY = new OptionalArgument<>(new StringArgument("short"));
	public static final OptionalArgument<Double> WORTH = new OptionalArgument<>(new DoubleArgument("worth"));


	@Override
	public @NotNull List<CommandArgument<?>> getArguments() {
		return Arrays.asList(ADD, SYMBOL, NAME, SINGLE_DISPLAY, MULTI_DISPLAY, SHORT_DISPLAY, WORTH);
	}

	@Override
	public @NotNull String getDescription() {
		return "Adds a new currency to the system";
	}

	@Override
	public @NotNull Optional<String> getPermissionNode() {
		return Optional.of(Permissions.ADD_CURRENCY.getPermissionNode());
	}

	@Override
	public boolean run(CommandContext commandContext, String... args) {
		String symbol = commandContext.getArgument(this, SYMBOL);
		String name = commandContext.getArgument(this, NAME);
		String singleDisplay = commandContext.getArgument(this, SINGLE_DISPLAY);
		String multiDisplay = commandContext.getArgument(this, MULTI_DISPLAY);
		String shortDisplay = commandContext.getArgument(this, SHORT_DISPLAY);
		Double worth = commandContext.getArgument(this, WORTH);
		boolean isDefault = AccountInterface.getManager().getCurrencies().isEmpty();
		Currency<?> currency =
				new CurrencyBuilder().setName(name)
						.setPlugin(EcoToolPlugin.getInstance())
						.setSymbol(symbol)
						.setDisplayNameMultiple(multiDisplay)
						.setDisplayNameSingle(singleDisplay)
						.setDisplayNameShort(shortDisplay)
						.setWorth(worth)
						.setDefault(isDefault)
						.build();

		Optional<Currency<?>> alreadyRegistered =
				AccountInterface.getManager().getCurrencies().parallelStream().filter(search -> {
					if (search.getKeyName().equalsIgnoreCase(name) && search.getPlugin()
							.equals(EcoToolPlugin.getInstance())) {
						return true;
					}
					return search.getSymbol().equalsIgnoreCase(symbol);
				}).findAny();
		if (alreadyRegistered.isPresent()) {
			commandContext.getSource().sendMessage("Currency has already been registered:");
			commandContext.getSource().sendMessage("\t- " + alreadyRegistered.get().getSymbol());
			commandContext.getSource().sendMessage("\t- " + alreadyRegistered.get().getPlugin().getName());
			commandContext.getSource().sendMessage("\t- " + alreadyRegistered.get().getKeyName());
			return true;
		}

		try {
			currency.save();
		} catch (IOException e) {
			e.printStackTrace();
			commandContext.getSource().sendMessage("A error occurred when creating currency: " + e.getMessage());
			return true;
		}

		commandContext.getSource().sendMessage("Adding currency");
		commandContext.getSource().sendMessage(currency.formatSymbol(BigDecimal.ONE));
		commandContext.getSource().sendMessage(currency.formatName(BigDecimal.ONE));
		commandContext.getSource().sendMessage(currency.formatName(BigDecimal.TEN));
		AccountInterface.getManager().registerCurrency(currency);
		return true;
	}
}
