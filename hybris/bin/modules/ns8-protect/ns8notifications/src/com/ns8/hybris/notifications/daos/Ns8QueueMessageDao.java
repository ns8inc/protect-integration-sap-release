package com.ns8.hybris.notifications.daos;

import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;

import java.util.List;
import java.util.Optional;

/**
 * Dao interface to get Ns8QueueMessageModel from the DB
 */
public interface Ns8QueueMessageDao {

    /**
     * Finds all pending ns8 messages
     *
     * @return a list of {@link Ns8QueueMessageModel}
     */
    List<Ns8QueueMessageModel> findPendingNs8QueueMessages();

    /**
     * Finds an ns8 message by message id
     *
     * @param messageId the id of the message
     * @return Optional queue message
     */
    Optional<Ns8QueueMessageModel> findNs8QueueMessageById(String messageId);
}
