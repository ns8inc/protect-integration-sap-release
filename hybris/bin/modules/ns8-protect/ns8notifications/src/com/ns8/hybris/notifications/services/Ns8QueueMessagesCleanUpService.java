package com.ns8.hybris.notifications.services;

import com.ns8.hybris.notifications.enums.Ns8MessageStatus;

/**
 * Provides queue message cleanup functionality
 */
public interface Ns8QueueMessagesCleanUpService {

    /**
     * Deletes queue messages for a status older than the age in days
     *
     * @param messageStatus    the status of the message to be deleted
     * @param messageAgeInDays the age in days of the message prior to be deleted
     */
    void doCleanUp(Ns8MessageStatus messageStatus, int messageAgeInDays);
}
