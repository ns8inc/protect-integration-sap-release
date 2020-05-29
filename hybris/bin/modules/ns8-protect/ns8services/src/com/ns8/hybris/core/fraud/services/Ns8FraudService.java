package com.ns8.hybris.core.fraud.services;

import com.ns8.hybris.core.fraud.impl.Ns8FraudServiceResponse;
import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;

/**
 * NS8 fraud service to implement ns8 custom logic for fraud
 */
public interface Ns8FraudService {

    /**
     * Determines whether the given order has already been scored by NS8
     *
     * @param order The order to check
     * @return true if the order has been scored already, false otherwise
     */
    boolean hasOrderBeenScored(OrderModel order);

    /**
     * Checks if the given order has a fraud report
     *
     * @param order The order to check
     * @return true if the order has a fraud report, false otherwise
     */
    boolean isOrderFraudChecked(OrderModel order);

    /**
     * Creates the FraudReport for given Ns8FraudServiceResponse, provider name, order and fraud status
     *
     * @param providerName the current provider name
     * @param response     the response with fraud data
     * @param order        the current order
     * @param status       the fraud status
     * @return the {@link FraudReportModel}
     */
    FraudReportModel createFraudReport(String providerName, Ns8FraudServiceResponse response,
                                       OrderModel order, FraudStatus status);

    /**
     * Updates the order fraud report when the order has been already scored.
     *
     * @param order the current order
     */
    void updateOrderFraudReport(OrderModel order);
}
