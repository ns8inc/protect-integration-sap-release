package com.ns8.hybris.notifications.services.impl;

import com.ns8.hybris.notifications.daos.Ns8QueueMessageDao;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import com.ns8.hybris.notifications.services.Ns8QueueMessagesCleanUpService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link Ns8QueueMessagesCleanUpService}
 */
public class DefaultNs8QueueMessagesCleanUpService implements Ns8QueueMessagesCleanUpService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNs8QueueMessagesCleanUpService.class);

    protected final ModelService modelService;
    protected final Ns8QueueMessageDao ns8QueueMessageDao;

    public DefaultNs8QueueMessagesCleanUpService(final ModelService modelService, final Ns8QueueMessageDao ns8QueueMessageDao) {
        this.modelService = modelService;
        this.ns8QueueMessageDao = ns8QueueMessageDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCleanUp(final Ns8MessageStatus messageStatus, int messageAgeInDays) {
        final Date creationDate = Date.from(LocalDate.now().minusDays(messageAgeInDays).atStartOfDay(ZoneId.systemDefault()).toInstant());
        final List<Ns8QueueMessageModel> queueMessagesByStatusCreatedBeforeDate = ns8QueueMessageDao.findNs8QueueMessagesByStatusCreatedBeforeDate(messageStatus, creationDate);

        LOG.info("Found [{}] queue messages to delete.", queueMessagesByStatusCreatedBeforeDate::size);

        queueMessagesByStatusCreatedBeforeDate.forEach(queueMessage -> {
            LOG.debug("Deleting queue message with order id [{}]  and status [{}]", queueMessage::getOrderId, queueMessage::getStatus);
            modelService.remove(queueMessage);
        });
    }
}
