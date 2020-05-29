package com.ns8.hybris.notifications.messages.processing.strategies;

import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;

/**
 * Interface to process NS8 messages
 */
public interface Ns8ProcessMessagesStrategy {

    /**
     * Processes the given pending message and performs the specific logic
     *
     * @param message message to process
     */
    void processMessage(Ns8QueueMessageModel message);
}
