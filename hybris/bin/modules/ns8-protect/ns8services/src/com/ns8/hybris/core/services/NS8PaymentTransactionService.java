package com.ns8.hybris.core.services;

import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

/**
 * NS8 payment transaction service
 */
public interface Ns8PaymentTransactionService {

    /**
     * Finds the most relevant {@link PaymentTransactionEntryModel} from the given {@link PaymentTransactionModel}
     *
     * @param paymentTransaction
     * @return The {@link PaymentTransactionEntryModel} deemed most relevant for the given {@link PaymentTransactionModel}
     */
    PaymentTransactionEntryModel getApplicableEntry(PaymentTransactionModel paymentTransaction);
}
