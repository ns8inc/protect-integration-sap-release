package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.messages.processing.strategies.Ns8ProcessMessagesStrategy;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Abstract process messages strategy that exposes the common methods for different implementations
 */
public abstract class Ns8AbstractProcessMessagesStrategy implements Ns8ProcessMessagesStrategy {

    protected static final Logger LOG = LogManager.getLogger(Ns8AbstractProcessMessagesStrategy.class);
    protected static final String NS8_SCORE_RECEIVED_EVENT = "_NS8ScoreReceived";

    protected ModelService modelService;
    protected BusinessProcessService businessProcessService;
    protected Ns8OrderDao orderDao;
    protected List<OrderStatus> updateAllowedOrderStatuses;
    protected String providerName;
    protected Ns8FraudService ns8FraudService;

    public Ns8AbstractProcessMessagesStrategy(final ModelService modelService,
                                              final BusinessProcessService businessProcessService,
                                              final Ns8OrderDao orderDao,
                                              final List<OrderStatus> updateAllowedOrderStatuses,
                                              final String providerName,
                                              final Ns8FraudService ns8FraudService) {
        this.modelService = modelService;
        this.businessProcessService = businessProcessService;
        this.orderDao = orderDao;
        this.updateAllowedOrderStatuses = updateAllowedOrderStatuses;
        this.providerName = providerName;
        this.ns8FraudService = ns8FraudService;
    }

    protected Ns8AbstractProcessMessagesStrategy() {
        // default empty constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processMessage(final Ns8QueueMessageModel message) {
        validateParameterNotNull(message, "Message cannot be null");

        final Optional<OrderModel> orderForCode = orderDao.findOrderForCode(message.getOrderId());

        if (orderForCode.isEmpty()) {
            updateFailReason(String.format("Order model not found for the following order id [%s].", message.getOrderId()),
                    message);
            updateMessageStatus(message, Ns8MessageStatus.FAILED);
        } else {
            final OrderModel order = orderForCode.get();

            if (updateAllowedOrderStatuses.contains(order.getStatus())) {
                processMessageForOrder(message, order);
            } else {
                updateFailReason(String.format("Ignored because the order with id [%s] is in [%s] status. No further update is allowed", message.getOrderId(), order.getStatus().toString()),
                        message);
                updateMessageStatus(message, Ns8MessageStatus.IGNORED);
            }
        }
    }

    /**
     * Implements the specific logic to process a message based on the concrete strategy
     *
     * @param message the ns8 message
     * @param order   the order to update
     */
    protected abstract void processMessageForOrder(Ns8QueueMessageModel message, OrderModel order);

    /**
     * Updates the order with the message payload
     *
     * @param message the ns8 message
     * @param order   the order to update
     */
    protected void setRiskScore(final Ns8QueueMessageModel message, final OrderModel order) {
        order.setRiskEventPayload(message.getBody());
        modelService.save(order);
    }

    /**
     * Updates the message with the given errorMessage and logs the error message
     *
     * @param errorMessage error message to add as failed reason
     * @param message      ns8 message to update
     */
    protected void updateFailReason(final String errorMessage,
                                    final Ns8QueueMessageModel message) {
        LOG.error(errorMessage);
        message.setFailReason(errorMessage);
        modelService.save(message);
    }

    /**
     * Updates the message status
     *
     * @param ns8QueueMessage the message to update
     * @param status          the message status to set
     */
    protected void updateMessageStatus(final Ns8QueueMessageModel ns8QueueMessage, final Ns8MessageStatus status) {
        ns8QueueMessage.setStatus(status);
        modelService.save(ns8QueueMessage);
    }


    /**
     * Unblocks the order process triggering the business process event
     *
     * @param message the ns8 message
     */
    protected void triggerOrderEvent(final Ns8QueueMessageModel message) {
        final String ns8ScoreReceivedEventName = message.getOrderId() + NS8_SCORE_RECEIVED_EVENT;
        businessProcessService.triggerEvent(ns8ScoreReceivedEventName);
    }
}
