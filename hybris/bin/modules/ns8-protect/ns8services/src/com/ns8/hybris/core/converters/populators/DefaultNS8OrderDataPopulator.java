package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.*;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Populates the information of the {@link OrderModel} into a {@link NS8OrderData}
 */
public class DefaultNS8OrderDataPopulator implements Populator<OrderModel, NS8OrderData> {

    protected final Converter<OrderModel, List<NS8AddressData>> ns8AddressDatasConverter;
    protected final Converter<OrderModel, NS8CustomerData> ns8CustomerDataConverter;
    protected final Converter<OrderEntryModel, NS8LineItemData> ns8LineItemDataConverter;
    protected final Converter<PaymentTransactionModel, NS8TransactionData> ns8TransactionDataConverter;
    protected final Converter<OrderModel, NS8SessionData> ns8SessionDataConverter;

    public DefaultNS8OrderDataPopulator(final Converter<OrderModel, List<NS8AddressData>> ns8AddressDatasConverter,
                                        final Converter<OrderModel, NS8CustomerData> ns8CustomerDataConverter,
                                        final Converter<OrderEntryModel, NS8LineItemData> ns8LineItemDataConverter,
                                        final Converter<PaymentTransactionModel, NS8TransactionData> ns8TransactionDataConverter,
                                        final Converter<OrderModel, NS8SessionData> ns8SessionDataConverter) {
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
    public void populate(final OrderModel source, final NS8OrderData target) throws ConversionException {
        target.setCreatedAt(source.getCreationtime());
        target.setPlatformCreatedAt(source.getCreationtime());
        target.setCurrency(source.getCurrency().getIsocode());
        final String orderName = getOrderName(source);
        target.setName(orderName);
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
     * Gets the name to attach to the order
     *
     * @param source orderModel from which to get the name
     * @return the name of the order (fallback to order code)
     */
    protected String getOrderName(final OrderModel source) {
        return StringUtils.isNotBlank(source.getName()) ? source.getName() : source.getCode();
    }

    /**
     * Converts order entries to a list of {@link NS8LineItemData}
     *
     * @param source the order model
     * @return a list of line item data
     */
    protected List<NS8LineItemData> convertEntries(final OrderModel source) {
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
