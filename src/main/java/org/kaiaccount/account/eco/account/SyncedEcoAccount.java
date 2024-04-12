package org.kaiaccount.account.eco.account;

import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.eco.utils.CommonUtils;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.type.AccountSynced;

public interface SyncedEcoAccount<Eco extends SyncedEcoAccount<Eco>> extends EcoAccount<Eco>, AccountSynced {

    @Override
    default @NotNull SingleTransactionResult setSynced(@NotNull Payment payment) {
        return CommonUtils.redirectSet(this, payment, AccountSynced::depositSynced, AccountSynced::withdrawSynced);
    }
}
