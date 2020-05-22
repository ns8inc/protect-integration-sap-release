package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.NS8SessionData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

/**
 * Populates the information of the {@link OrderModel} into a {@link NS8SessionData}
 */
public class DefaultNs8SessionDataPopulator implements Populator<OrderModel, NS8SessionData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final NS8SessionData target) throws ConversionException {
        target.setIp(source.getCustomerIp());
        target.setUserAgent(source.getCustomerUserAgent());
    }
}
