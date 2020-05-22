package com.ns8.hybris.core.services.impl;

import com.ns8.hybris.core.services.NS8PaymentTransactionService;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import java.util.Optional;

/**
 * Default implementation of {@link NS8PaymentTransactionService}
 */
public class DefaultNS8PaymentTransactionService implements NS8PaymentTransactionService {

    /**
     * {@inheritDoc}
     * This implementation has the following criteria
     * - If any {@link PaymentTransactionType#CAPTURE} entry is present, return the first one (based on {@link PaymentTransactionEntryModel#getType()})
     * - Otherwise, if any {@link PaymentTransactionType#AUTHORIZATION} entry is present, return the first one (based on {@link PaymentTransactionEntryModel#getType()})
     * - Otherwise, throw {@link IllegalArgumentException}
     */
    @Override
    public PaymentTransactionEntryModel getApplicableEntry(final PaymentTransactionModel paymentTransaction) {
        final Optional<PaymentTransactionEntryModel> captureEntryOpt =
                getTransactionOfType(paymentTransaction, PaymentTransactionType.CAPTURE);
        final Optional<PaymentTransactionEntryModel> authEntryOpt =
                getTransactionOfType(paymentTransaction, PaymentTransactionType.AUTHORIZATION);
        if (captureEntryOpt.isPresent()) {
            return captureEntryOpt.get();
        } else if (authEntryOpt.isPresent()) {
            return authEntryOpt.get();
        } else {
            throw new IllegalArgumentException(String.format("PaymentTransaction with code %s did not contain a capture or auth entry", paymentTransaction.getCode()));
        }
    }

    /**
     * Finds the payment transaction entry for the given transaction and transaction type
     *
     * @param source          the transaction model
     * @param transactionType the transaction type
     * @return the payment transaction entry for transaction type
     */
    protected Optional<PaymentTransactionEntryModel> getTransactionOfType(final PaymentTransactionModel source, final PaymentTransactionType transactionType) {
        return source.getEntries().stream()
                .filter(entry -> transactionType.equals(entry.getType()))
                .findFirst();
    }
}
