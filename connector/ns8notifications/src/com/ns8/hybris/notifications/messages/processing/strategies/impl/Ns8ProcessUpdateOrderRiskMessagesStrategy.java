package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.messages.processing.strategies.Ns8ProcessMessagesStrategy;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;

/**
 * {@link Ns8ProcessMessagesStrategy} implementation to process messages with action type UPDATE_ORDER_RISK_EVENT
 */
public class Ns8ProcessUpdateOrderRiskMessagesStrategy extends Ns8AbstractProcessMessagesStrategy {

    public Ns8ProcessUpdateOrderRiskMessagesStrategy(final ModelService modelService,
                                                     final BusinessProcessService businessProcessService,
                                                     final Ns8OrderDao orderDao,
                                                     final String providerName,
                                                     final Ns8FraudService ns8FraudService) {
        super(modelService, businessProcessService, orderDao, providerName, ns8FraudService);
    }

    /**
     * Implements the logic to set the risk score payload in the order and if the order is not fraud checked yet, it will
     * trigger the event in order to unblock the business process. If the order is fraud checked, it will just update
     * the fraud report based on the last payload.
     *
     * @param message the ns8 message
     * @param order   the order to update
     */
    protected void processMessageForOrder(final Ns8QueueMessageModel message, final OrderModel order) {
        setRiskScore(message, order);
        if (ns8FraudService.isOrderFraudChecked(order)) {
            ns8FraudService.updateOrderFraudReport(order);
        } else {
            triggerOrderEvent(message);
        }
        updateMessageStatus(message, Ns8MessageStatus.COMPLETED);
    }
}
