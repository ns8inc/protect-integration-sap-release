package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8CreditCardData;
import com.ns8.hybris.core.data.Ns8CreditCardTransactionType;
import com.ns8.hybris.core.services.NS8PaymentTransactionService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.CreditCardType;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

/**
 * Populates the information of the {@link PaymentTransactionModel} into a {@link Ns8CreditCardData}
 */
public class DefaultNs8CreditCardDataPopulator implements Populator<PaymentTransactionModel, Ns8CreditCardData> {

    protected final NS8PaymentTransactionService ns8PaymentTransactionService;
    protected Map<PaymentTransactionType, Ns8CreditCardTransactionType> transactionTypeMapping;

    public DefaultNs8CreditCardDataPopulator(final NS8PaymentTransactionService ns8PaymentTransactionService, final Map<PaymentTransactionType, Ns8CreditCardTransactionType> transactionTypeMapping) {
        this.ns8PaymentTransactionService = ns8PaymentTransactionService;
        this.transactionTypeMapping = transactionTypeMapping;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentTransactionModel source, final Ns8CreditCardData target) throws ConversionException {
        final PaymentInfoModel paymentInfo = source.getInfo();
        Assert.isInstanceOf(CreditCardPaymentInfoModel.class, paymentInfo);
        final CreditCardPaymentInfoModel ccPaymentInfo = (CreditCardPaymentInfoModel) paymentInfo;

        target.setCardExpiration(String.format("%s/%s", ccPaymentInfo.getValidToMonth(), ccPaymentInfo.getValidToYear()));
        target.setCreditCardCompany(getCreditCardCompany(ccPaymentInfo));
        target.setCreditCardNumber(ccPaymentInfo.getNumber());
        target.setTransactionType(getTransactionType(source));

        target.setCardHolder(getCardHolderName(ccPaymentInfo));
    }

    /**
     * Gets the NS8 Credit Card transaction type for the given transaction model
     *
     * @param source the transaction model
     * @return {@link Ns8CreditCardTransactionType}
     */
    protected Ns8CreditCardTransactionType getTransactionType(final PaymentTransactionModel source) {
        return transactionTypeMapping.get(ns8PaymentTransactionService.getApplicableEntry(source).getType());
    }

    /**
     * Returns the credit card type  if payment type is card, null otherwise
     *
     * @param ccPaymentInfo the payment info
     * @return the credit card type
     */
    protected String getCreditCardCompany(final CreditCardPaymentInfoModel ccPaymentInfo) {
        return Optional.ofNullable(ccPaymentInfo.getType())
                .map(CreditCardType::toString)
                .orElse(null);
    }

    /**
     * Returns the Card holder name
     *
     * @param ccPaymentInfo the payment info
     * @return the card holder name
     */
    protected String getCardHolderName(final CreditCardPaymentInfoModel ccPaymentInfo) {
        return Optional.ofNullable(ccPaymentInfo.getBillingAddress())
                .map(billingAddress -> String.format("%s %s", billingAddress.getFirstname(), billingAddress.getLastname()))
                .orElse(null);
    }

    protected void setTransactionTypeMapping(final Map<PaymentTransactionType, Ns8CreditCardTransactionType> transactionTypeMapping) {
        this.transactionTypeMapping = transactionTypeMapping;
    }
}
