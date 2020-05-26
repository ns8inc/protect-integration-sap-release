package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8CreditCardData;
import com.ns8.hybris.core.data.Ns8TransactionData;
import com.ns8.hybris.core.data.Ns8TransactionMethod;
import com.ns8.hybris.core.data.Ns8TransactionStatus;
import com.ns8.hybris.core.services.NS8PaymentTransactionService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the information of the {@link PaymentTransactionModel} into a {@link Ns8TransactionData}
 */
public class DefaultNs8TransactionDataPopulator implements Populator<PaymentTransactionModel, Ns8TransactionData> {

    protected final Converter<PaymentTransactionModel, Ns8CreditCardData> ns8CreditCardDataConverter;
    protected final NS8PaymentTransactionService ns8PaymentTransactionService;
    protected Map<String, Ns8TransactionStatus> transactionStatusMapping;

    public DefaultNs8TransactionDataPopulator(final Converter<PaymentTransactionModel, Ns8CreditCardData> ns8CreditCardDataConverter,
                                              final NS8PaymentTransactionService ns8PaymentTransactionService,
                                              final Map<String, Ns8TransactionStatus> transactionStatusMapping) {
        this.ns8CreditCardDataConverter = ns8CreditCardDataConverter;
        this.ns8PaymentTransactionService = ns8PaymentTransactionService;
        this.transactionStatusMapping = transactionStatusMapping;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentTransactionModel source, final Ns8TransactionData target) throws ConversionException {

        target.setAmount(getAmount(source));
        target.setCreditCard(ns8CreditCardDataConverter.convert(source));
        target.setMethod(Ns8TransactionMethod.CC);
        target.setPlatformId(source.getCode());
        target.setProcessedAt(source.getCreationtime());

        final PaymentTransactionEntryModel applicableEntry = ns8PaymentTransactionService.getApplicableEntry(source);
        final CurrencyModel currency = applicableEntry.getCurrency();
        Assert.notNull(currency, "no currency for payment transaction entry");
        target.setCurrency(currency.getIsocode());
        target.setStatus(transactionStatusMapping.get(applicableEntry.getTransactionStatus()));
        target.setStatusDetails(applicableEntry.getTransactionStatusDetails());
    }

    /**
     * Gets the payment transaction amount
     *
     * @param source the payment transaction model
     * @return the planned amount
     */
    protected double getAmount(final PaymentTransactionModel source) {
        return source.getPlannedAmount().doubleValue();
    }

    public void setTransactionStatusMapping(final Map<String, Ns8TransactionStatus> transactionStatusMapping) {
        this.transactionStatusMapping = transactionStatusMapping;
    }
}
