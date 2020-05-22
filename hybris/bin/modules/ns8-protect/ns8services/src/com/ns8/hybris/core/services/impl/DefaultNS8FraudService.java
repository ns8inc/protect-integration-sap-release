package com.ns8.hybris.core.services.impl;

import com.ns8.hybris.core.services.NS8FraudService;
import de.hybris.platform.core.model.order.OrderModel;

/**
 * Default implementation of {@link NS8FraudService}
 */
public class DefaultNS8FraudService implements NS8FraudService {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasOrderBeenScored(final OrderModel order) {
        return false;
    }
}
