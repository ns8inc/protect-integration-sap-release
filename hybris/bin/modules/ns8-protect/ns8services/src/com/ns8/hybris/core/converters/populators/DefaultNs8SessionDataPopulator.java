package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8SessionData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

/**
 * Populates the information of the {@link OrderModel} into a {@link Ns8SessionData}
 */
public class DefaultNs8SessionDataPopulator implements Populator<OrderModel, Ns8SessionData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final Ns8SessionData target) throws ConversionException {
        target.setIp(source.getCustomerIp());
        target.setUserAgent(source.getCustomerUserAgent());
        target.setAcceptLanguage(source.getAcceptLanguage());
        target.setScreenHeight(source.getScreenHeight());
        target.setScreenWidth(source.getScreenWidth());
    }
}
