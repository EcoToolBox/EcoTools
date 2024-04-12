package org.kaiaccount.account.eco.account.player;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.eco.EcoToolPlugin;
import org.kaiaccount.account.eco.io.EcoSerializers;
import org.kaiaccount.account.inter.io.Serializable;
import org.kaiaccount.account.inter.io.Serializer;
import org.kaiaccount.account.inter.transfer.Transaction;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.IsolatedAccount;
import org.kaiaccount.account.inter.type.player.AbstractPlayerAccount;
import org.kaiaccount.account.inter.type.player.PlayerAccountBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EcoPlayerAccount extends AbstractPlayerAccount
        implements Serializable<EcoPlayerAccount> {

    private boolean shouldSave = true;

    public EcoPlayerAccount(PlayerAccountBuilder builder) {
        super(builder);
    }

    @Override
    public Serializer<EcoPlayerAccount> getSerializer() {
        return EcoSerializers.PLAYER;
    }

    @Override
    public @NotNull File getFile() {
        return new File("plugins/eco/players/"
                + EcoToolPlugin.getPlugin().getName()
                + "/"
                + this.getPlayer().getUniqueId()
                + ".yml");
    }

    @NotNull
    @Override
    public CompletableFuture<TransactionResult> multipleTransaction(
            @NotNull Function<IsolatedAccount, CompletableFuture<? extends TransactionResult>>... transactions) {
        this.shouldSave = false;
        CompletableFuture<TransactionResult> future = super.multipleTransaction(transactions);
        future.thenAccept(result -> {
            this.shouldSave = true;
            savePlayer(result);
        });
        return future;
    }

    @NotNull
    @Override
    public CompletableFuture<TransactionResult> withdrawWithBanks(@NotNull Payment payment) {
        return this.saveOnFuture(super.withdrawWithBanks(payment));
    }

    @NotNull
    @Override
    public SingleTransactionResult withdrawSynced(@NotNull Payment payment) {
        SingleTransactionResult result = super.withdrawSynced(payment);
        savePlayer(result);
        return result;
    }

    @NotNull
    @Override
    public CompletableFuture<SingleTransactionResult> withdraw(@NotNull Payment payment) {
        return this.saveOnFuture(super.withdraw(payment));
    }

    @NotNull
    @Override
    public SingleTransactionResult depositSynced(@NotNull Payment payment) {
        SingleTransactionResult result = super.depositSynced(payment);
        savePlayer(result);
        return result;
    }

    @NotNull
    @Override
    public CompletableFuture<SingleTransactionResult> deposit(@NotNull Payment payment) {
        return this.saveOnFuture(super.deposit(payment));
    }

    @NotNull
    @Override
    public SingleTransactionResult setSynced(@NotNull Payment payment) {
        SingleTransactionResult result = super.setSynced(payment);
        savePlayer(result);
        return result;
    }

    @NotNull
    @Override
    public CompletableFuture<SingleTransactionResult> set(@NotNull Payment payment) {
        return this.saveOnFuture(super.set(payment));
    }

    @NotNull
    @Override
    public SingleTransactionResult refundSynced(@NotNull Transaction payment) {
        SingleTransactionResult result = super.refundSynced(payment);
        savePlayer(result);
        return result;
    }

    @NotNull
    @Override
    public CompletableFuture<SingleTransactionResult> refund(@NotNull Transaction payment) {
        return this.saveOnFuture(super.refund(payment));
    }

    @Override
    public void forceSetSynced(@NotNull Payment payment) {
        super.forceSetSynced(payment);
        savePlayer(null);
    }

    @NotNull
    @Override
    public CompletableFuture<Void> forceSet(@NotNull Payment payment) {
        return super.forceSet(payment).thenAccept(v -> savePlayer(null));
    }

    @Override
    public void save(@NotNull YamlConfiguration configuration) {
        if (!this.shouldSave) {
            //Will only be false if multiple transactions occur
            return;
        }
        Serializable.super.save(configuration);
    }

    private <T extends TransactionResult> CompletableFuture<T> saveOnFuture(@NotNull CompletableFuture<T> future) {
        future.thenAccept(this::savePlayer);
        return future;
    }

    private void savePlayer(@Nullable TransactionResult result) {
        if (result instanceof FailedTransactionResult) {
            //no changes
            return;
        }
        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
