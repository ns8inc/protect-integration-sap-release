package com.ns8.hybris.notifications.daos;

import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;

import java.util.Date;
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

    /**
     * Finds all queue messages with a processing status and older than the creation date
     *
     * @param messageStatus processing status of the queue message
     * @param creationDate  creation date of the queue message
     * @return a list of {@link Ns8QueueMessageModel}
     */
    List<Ns8QueueMessageModel> findNs8QueueMessagesByStatusCreatedBeforeDate(Ns8MessageStatus messageStatus, Date creationDate);
}
