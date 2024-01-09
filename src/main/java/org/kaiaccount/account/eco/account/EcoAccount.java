package org.kaiaccount.account.eco.account;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.inter.io.Serializable;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.Account;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface EcoAccount<Self extends Serializable<Self>> extends Account, Serializable<Self> {

    boolean isSaving();

    void setSaving(boolean saving);

    @Override
    default void save(@NotNull YamlConfiguration configuration) {
        if (!this.isSaving()) {
            //Will only be false if multiple transactions occur
            return;
        }
        Serializable.super.save(configuration);
    }

}
