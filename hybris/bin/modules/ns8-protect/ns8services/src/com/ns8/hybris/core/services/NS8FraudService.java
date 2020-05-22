package com.ns8.hybris.core.services;

import de.hybris.platform.core.model.order.OrderModel;

/**
 * NS8 fraud service
 */
public interface NS8FraudService {
    /**
     * Determines whether the given order has already been scored by NS8
     * @param order The order to check
     * @return true if the order has been scored already, false otherwise
     */
    boolean hasOrderBeenScored(OrderModel order);
}
