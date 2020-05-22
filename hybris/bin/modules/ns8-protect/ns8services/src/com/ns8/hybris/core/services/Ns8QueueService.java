package com.ns8.hybris.core.services;

import com.ns8.hybris.core.data.queue.Ns8QueueMessage;

import java.util.List;

/**
 * Service which handles message queues
 */
public interface Ns8QueueService {

    /**
     * Receives messages from the merchant's message queue
     *
     * @param queueUrl the queue url
     * @return a list of queue messages
     */
    List<Ns8QueueMessage> receiveMessages(String queueUrl);

    /**
     * Deletes a message from the merchant's message queue
     *
     * @param merchantApiKey merchant's api key
     * @param receiptHandle the handle of the message to be deleted
     * @return true if the message has been deleted successfully, false otherwise
     */
    boolean deleteMessage(String merchantApiKey, String receiptHandle);

    /**
     * Retrieves the merchants's queue url by making a call to the NS8 service.
     *
     * @param merchantApiKey merchant's api key used to get the merchant's queue
     * @return the queue url
     */
    String getQueueUrl(String merchantApiKey);
}
