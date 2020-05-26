package com.ns8.hybris.core.services.impl;

import com.ns8.hybris.core.services.NS8FraudService;
import de.hybris.platform.core.model.order.OrderModel;
import org.apache.commons.lang3.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link NS8FraudService}
 */
public class DefaultNS8FraudService implements NS8FraudService {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasOrderBeenScored(final OrderModel order) {
        validateParameterNotNull(order, "Order cannot be null");

        return StringUtils.isNotBlank(order.getRiskEventPayload());
    }
}
