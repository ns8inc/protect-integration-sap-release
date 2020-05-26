package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8PlatformOrderUpdateStatus;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;

/**
 * Populates the information of the {@link OrderModel} into a {@link Ns8PlatformOrderUpdateStatus}
 */
public class Ns8PlatformOrderUpdateStatusPopulator implements Populator<OrderModel, Ns8PlatformOrderUpdateStatus> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final Ns8PlatformOrderUpdateStatus target) {
        target.setName(source.getCode());
        target.setPlatformStatus(source.getStatus().toString());
    }
}
