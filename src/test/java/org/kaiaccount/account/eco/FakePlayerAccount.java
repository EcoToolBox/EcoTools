package org.kaiaccount.account.eco;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.type.player.AbstractPlayerAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccountBuilder;

import java.math.BigDecimal;
import java.util.Map;

public class FakePlayerAccount extends AbstractPlayerAccount {
    public FakePlayerAccount(@NotNull OfflinePlayer player) {
        super(new PlayerAccountBuilder().setPlayer(player));
    }

    public FakePlayerAccount(@NotNull OfflinePlayer player,
                             @NotNull Map<Currency<?>, BigDecimal> map) {
        super(new PlayerAccountBuilder().setInitialBalance(map).setPlayer(player));
    }
}
