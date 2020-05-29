package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.*;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Populates the information of the {@link OrderModel} into a {@link Ns8OrderData}
 */
public class DefaultNs8OrderDataPopulator implements Populator<OrderModel, Ns8OrderData> {

    protected final Converter<OrderModel, List<Ns8AddressData>> ns8AddressDatasConverter;
    protected final Converter<OrderModel, Ns8CustomerData> ns8CustomerDataConverter;
    protected final Converter<OrderEntryModel, Ns8LineItemData> ns8LineItemDataConverter;
    protected final Converter<PaymentTransactionModel, Ns8TransactionData> ns8TransactionDataConverter;
    protected final Converter<OrderModel, Ns8SessionData> ns8SessionDataConverter;

    public DefaultNs8OrderDataPopulator(final Converter<OrderModel, List<Ns8AddressData>> ns8AddressDatasConverter,
                                        final Converter<OrderModel, Ns8CustomerData> ns8CustomerDataConverter,
                                        final Converter<OrderEntryModel, Ns8LineItemData> ns8LineItemDataConverter,
                                        final Converter<PaymentTransactionModel, Ns8TransactionData> ns8TransactionDataConverter,
                                        final Converter<OrderModel, Ns8SessionData> ns8SessionDataConverter) {
        this.ns8AddressDatasConverter = ns8AddressDatasConverter;
        this.ns8CustomerDataConverter = ns8CustomerDataConverter;
        this.ns8LineItemDataConverter = ns8LineItemDataConverter;
        this.ns8TransactionDataConverter = ns8TransactionDataConverter;
        this.ns8SessionDataConverter = ns8SessionDataConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final Ns8OrderData target) throws ConversionException {
        target.setCreatedAt(source.getCreationtime());
        target.setPlatformCreatedAt(source.getCreationtime());
        target.setCurrency(source.getCurrency().getIsocode());
        target.setName(source.getCode());
        target.setPlatformId(source.getCode());
        target.setTotalPrice(source.getTotalPrice());
        target.setUpdatedAt(source.getModifiedtime());

        target.setTransactions(ns8TransactionDataConverter.convertAll(source.getPaymentTransactions()));
        target.setAddresses(ns8AddressDatasConverter.convert(source));

        target.setMerchantId(getMerchantId(source));
        target.setLineItems(convertEntries(source));
        target.setCustomer(ns8CustomerDataConverter.convert(source));
        target.setSession(ns8SessionDataConverter.convert(source));
    }

    /**
     * Converts order entries to a list of {@link Ns8LineItemData}
     *
     * @param source the order model
     * @return a list of line item data
     */
    protected List<Ns8LineItemData> convertEntries(final OrderModel source) {
        return source.getEntries().stream()
                .filter(OrderEntryModel.class::isInstance)
                .map(OrderEntryModel.class::cast)
                .map(ns8LineItemDataConverter::convert)
                .collect(toList());
    }

    /**
     * gets the merchantId for the order
     *
     * @param order the order model
     * @return teh merchant id
     */
    protected String getMerchantId(final OrderModel order) {
        return order.getSite().getNs8Merchant().getQueueId();
    }
}
