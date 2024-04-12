package org.kaiaccount.account.eco.account;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.account.history.TransactionHistory;
import org.kaiaccount.account.eco.utils.CommonUtils;
import org.kaiaccount.account.inter.io.Serializable;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.type.Account;

import java.util.concurrent.CompletableFuture;

public interface EcoAccount<Self extends Serializable<Self>> extends Account, Serializable<Self> {

    boolean isSaving();

    void setSaving(boolean saving);

    TransactionHistory getTransactionHistory();

    @Override
    default void save(@NotNull YamlConfiguration configuration) {
        if (!this.isSaving()) {
            //Will only be false if multiple transactions occur
            return;
        }
        Serializable.super.save(configuration);
    }

    @Override
    default @NotNull CompletableFuture<SingleTransactionResult> set(@NotNull Payment payment) {
        return CommonUtils.redirectSet(this, payment, Account::deposit, Account::withdraw);

    }
}
