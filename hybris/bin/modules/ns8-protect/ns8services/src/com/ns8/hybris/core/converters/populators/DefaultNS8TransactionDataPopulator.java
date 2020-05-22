package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8CreditCardData;
import com.ns8.hybris.core.data.NS8TransactionData;
import com.ns8.hybris.core.data.NS8TransactionMethod;
import com.ns8.hybris.core.data.NS8TransactionStatus;
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
 * Populates the information of the {@link PaymentTransactionModel} into a {@link NS8TransactionData}
 */
public class DefaultNS8TransactionDataPopulator implements Populator<PaymentTransactionModel, NS8TransactionData> {

    protected final Converter<PaymentTransactionModel, NS8CreditCardData> ns8CreditCardDataConverter;
    protected final NS8PaymentTransactionService ns8PaymentTransactionService;
    protected Map<String, NS8TransactionStatus> transactionStatusMapping;

    public DefaultNS8TransactionDataPopulator(final Converter<PaymentTransactionModel, NS8CreditCardData> ns8CreditCardDataConverter,
                                              final NS8PaymentTransactionService ns8PaymentTransactionService,
                                              final Map<String, NS8TransactionStatus> transactionStatusMapping) {
        this.ns8CreditCardDataConverter = ns8CreditCardDataConverter;
        this.ns8PaymentTransactionService = ns8PaymentTransactionService;
        this.transactionStatusMapping = transactionStatusMapping;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentTransactionModel source, final NS8TransactionData target) throws ConversionException {

        target.setAmount(getAmount(source));
        target.setCreditCard(ns8CreditCardDataConverter.convert(source));
        target.setMethod(NS8TransactionMethod.CC);
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

    public void setTransactionStatusMapping(final Map<String, NS8TransactionStatus> transactionStatusMapping) {
        this.transactionStatusMapping = transactionStatusMapping;
    }
}
