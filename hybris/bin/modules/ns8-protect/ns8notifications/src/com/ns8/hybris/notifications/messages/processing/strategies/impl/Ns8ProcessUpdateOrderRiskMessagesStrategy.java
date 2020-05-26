package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.messages.processing.strategies.Ns8ProcessMessagesStrategy;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * {@link Ns8ProcessMessagesStrategy} implementation to process messages with action type UPDATE_ORDER_RISK_EVENT
 */
public class Ns8ProcessUpdateOrderRiskMessagesStrategy implements Ns8ProcessMessagesStrategy {

    protected static final Logger LOG = LogManager.getLogger(Ns8ProcessUpdateOrderRiskMessagesStrategy.class);
    protected static final String NS8_SCORE_RECEIVED_EVENT = "_NS8ScoreReceived";

    protected final Ns8OrderDao orderDao;
    protected final ModelService modelService;
    protected final BusinessProcessService businessProcessService;

    public Ns8ProcessUpdateOrderRiskMessagesStrategy(final Ns8OrderDao orderDao,
                                                     final ModelService modelService,
                                                     final BusinessProcessService businessProcessService) {
        this.orderDao = orderDao;
        this.modelService = modelService;
        this.businessProcessService = businessProcessService;
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
            order.setRiskEventPayload(message.getBody());
            modelService.save(order);

            triggerOrderEvent(message);

            updateMessageStatus(message, Ns8MessageStatus.COMPLETED);
        }

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
}
