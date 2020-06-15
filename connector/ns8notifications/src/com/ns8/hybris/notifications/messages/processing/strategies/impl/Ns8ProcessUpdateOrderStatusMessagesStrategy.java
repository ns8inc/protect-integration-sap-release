package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.google.gson.Gson;
import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.messages.processing.strategies.Ns8ProcessMessagesStrategy;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Map;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * {@link Ns8ProcessMessagesStrategy} implementation to process messages with action type UPDATE_ORDER_RISK_EVENT
 */
public class Ns8ProcessUpdateOrderStatusMessagesStrategy extends Ns8AbstractProcessMessagesStrategy {

    protected static final String ORDER_UPDATE_STATUS_KEY = "status";
    protected static final String ORDER_VERIFIED_EVENT_SUFFIX = "_CSAOrderVerified";

    public Ns8ProcessUpdateOrderStatusMessagesStrategy(final ModelService modelService,
                                                       final BusinessProcessService businessProcessService,
                                                       final Ns8OrderDao orderDao,
                                                       final String providerName,
                                                       final Ns8FraudService ns8FraudService) {
        super(modelService, businessProcessService, orderDao, providerName, ns8FraudService);
    }

    /**
     * Implements the logic to set the risk score payload in the order and if the order is not fraud checked yet, it will
     * trigger the event in order to unblock the business process. If the order is fraud checked, it will update
     * the fraud report based on the last payload, and if the order process is in WAIT_FRAUD_MANUAL_CHECK will also unblock
     * the process with the received status
     *
     * @param message the ns8 message
     * @param order   the order to update
     */
    protected void processMessageForOrder(final Ns8QueueMessageModel message, final OrderModel order) {
        setRiskScore(message, order);
        if (ns8FraudService.isOrderFraudChecked(order)) {
            ns8FraudService.updateOrderFraudReport(order);
            if (OrderStatus.WAIT_FRAUD_MANUAL_CHECK.equals(order.getStatus())) {
                updateOrderProcessWaitingForManualCheck(message, order);
            } else {
                updateMessageStatus(message, Ns8MessageStatus.COMPLETED);
            }
        } else {
            triggerOrderEvent(message);
            updateMessageStatus(message, Ns8MessageStatus.COMPLETED);
        }
    }

    /**
     * Updates the order process waiting for manual check and set the order fraudulent or not, based on the message outcome.
     * If the message status is still in MERCHANT_REVIEW, it will ignore the message because is not an update for the
     * current status
     *
     * @param message the message to process
     * @param order   the current order
     */
    protected void updateOrderProcessWaitingForManualCheck(final Ns8QueueMessageModel message, final OrderModel order) {
        final Map<String, Object> riskEventBodyMap = new Gson().fromJson(message.getBody(), Map.class);
        final String status = (String) riskEventBodyMap.get(ORDER_UPDATE_STATUS_KEY);

        if (Ns8FraudReportStatus.CANCELLED.toString().equalsIgnoreCase(status)) {
            updateOrderAndUnblockProcess(message, order, true);
        } else if (Ns8FraudReportStatus.APPROVED.toString().equalsIgnoreCase(status)) {
            updateOrderAndUnblockProcess(message, order, false);
        }
    }

    /**
     * Update the order if fraudulent and Unblocks the order process triggering the event
     *
     * @param message           the ns8 message status update
     * @param order             the current order
     * @param isOrderFraudulent true if order cancelled, false otherwise
     */
    protected void updateOrderAndUnblockProcess(final Ns8QueueMessageModel message, final OrderModel order, final boolean isOrderFraudulent) {
        order.setFraudulent(isOrderFraudulent);
        modelService.save(order);
        triggerManualOrderCheckEvent(order, message);
    }

    /**
     * Triggers the event to unblock the order process in state waitForManualOrderCheckCSA due to a Ns8 order status change
     *
     * @param order   the current order
     * @param message the ns8 message status update
     */
    protected void triggerManualOrderCheckEvent(final OrderModel order, final Ns8QueueMessageModel message) {
        validateParameterNotNull(order.getStore(), "Order store cannot be null");

        final Optional<OrderProcessModel> orderProcess = order.getOrderProcess().stream().filter(process -> process.getProcessDefinitionName().equalsIgnoreCase(order.getStore().getSubmitOrderProcessCode())).findFirst();

        if (orderProcess.isPresent()) {
            final String ns8CSAOrderVerifiedEventName = orderProcess.get().getCode() + ORDER_VERIFIED_EVENT_SUFFIX;
            businessProcessService.triggerEvent(ns8CSAOrderVerifiedEventName);
            updateMessageStatus(message, Ns8MessageStatus.COMPLETED);
        } else {
            updateFailReason(String.format("Order process not found for order id [%s].", order.getCode()),
                    message);
            updateMessageStatus(message, Ns8MessageStatus.FAILED);
        }
    }
}
