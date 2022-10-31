package org.kaiaccount.account.eco.bank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.eco.io.EcoSerializers;
import org.kaiaccount.account.inter.io.Serializable;
import org.kaiaccount.account.inter.io.Serializer;
import org.kaiaccount.account.inter.transfer.Transaction;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.transfer.result.failed.FailedTransactionResult;
import org.kaiaccount.account.inter.type.IsolatedAccount;
import org.kaiaccount.account.inter.type.bank.BankPermission;
import org.kaiaccount.account.inter.type.bank.player.AbstractPlayerBankAccount;
import org.kaiaccount.account.inter.type.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.bank.player.PlayerBankAccountBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EcoBankAccount extends AbstractPlayerBankAccount<EcoBankAccount>
		implements PlayerBankAccount<EcoBankAccount>, Serializable<EcoBankAccount> {


	public EcoBankAccount(@NotNull PlayerBankAccountBuilder builder) {
		super(builder);
	}

	@Override
	public Serializer<EcoBankAccount> getSerializer() {
		return EcoSerializers.BANK;
	}

	@Override
	public File getFile() {
		return new File("plugins/eco/players/"
				+ this.getAccountHolder().getPlayer().getUniqueId()
				+ "/"
				+ this.getBankAccountName()
				+ ".yml");
	}

	@NotNull
	@Override
	public CompletableFuture<SingleTransactionResult> withdraw(@NotNull Payment payment) {
		return saveOnFuture(super.withdraw(payment));
	}

	@NotNull
	@Override
	public SingleTransactionResult withdrawSynced(@NotNull Payment payment) {
		SingleTransactionResult result = super.withdrawSynced(payment);
		saveBank(result);
		return result;
	}

	@NotNull
	@Override
	public CompletableFuture<SingleTransactionResult> deposit(@NotNull Payment payment) {
		return saveOnFuture(super.deposit(payment));
	}

	@NotNull
	@Override
	public SingleTransactionResult depositSynced(@NotNull Payment payment) {
		SingleTransactionResult result = super.depositSynced(payment);
		this.saveBank(result);
		return result;
	}

	@NotNull
	@Override
	public CompletableFuture<SingleTransactionResult> set(@NotNull Payment payment) {
		return this.saveOnFuture(super.set(payment));
	}

	@NotNull
	@Override
	public SingleTransactionResult setSynced(@NotNull Payment payment) {
		SingleTransactionResult result = super.setSynced(payment);
		this.saveBank(result);
		return result;
	}

	@NotNull
	@Override
	public CompletableFuture<SingleTransactionResult> refund(@NotNull Transaction payment) {
		return this.saveOnFuture(super.refund(payment));
	}

	@NotNull
	@Override
	public CompletableFuture<Void> forceSet(@NotNull Payment payment) {
		CompletableFuture<Void> future = super.forceSet(payment);
		future.thenAccept(c -> {
			try {
				save();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return future;
	}

	@NotNull
	@Override
	public SingleTransactionResult refundSynced(@NotNull Transaction payment) {
		SingleTransactionResult result = super.refundSynced(payment);
		this.saveBank(result);
		return result;
	}

	@Override
	public void forceSetSynced(@NotNull Payment payment) {
		super.forceSetSynced(payment);
		try {
			save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	@Override
	public CompletableFuture<TransactionResult> multipleTransaction(
			@NotNull Function<IsolatedAccount, CompletableFuture<? extends TransactionResult>>... transactions) {
		return this.saveOnFuture(super.multipleTransaction(transactions));
	}

	@Override
	public void addAccount(@NotNull UUID uuid, Collection<BankPermission> permissions) {
		super.addAccount(uuid, permissions);
		try {
			this.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeAccount(@NotNull UUID uuid) {
		super.removeAccount(uuid);
		try {
			this.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private <T extends TransactionResult> CompletableFuture<T> saveOnFuture(@NotNull CompletableFuture<T> future) {
		future.thenAccept(this::saveBank);
		return future;
	}

	private void saveBank(@Nullable TransactionResult result) {
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
