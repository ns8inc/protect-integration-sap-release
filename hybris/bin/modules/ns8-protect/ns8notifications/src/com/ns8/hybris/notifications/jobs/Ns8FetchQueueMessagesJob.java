package com.ns8.hybris.notifications.jobs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.notifications.constants.Ns8notificationsConstants;
import com.ns8.hybris.notifications.daos.Ns8QueueMessageDao;
import com.ns8.hybris.notifications.enums.Ns8MessageActionType;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8FetchQueueMessagesCronJobModel;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import com.ns8.hybris.notifications.services.Ns8QueueService;
import com.ns8.hybris.ns8notifications.data.queue.Ns8QueueMessage;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Job definition to fetch NS8 messages from the queue
 */
public class Ns8FetchQueueMessagesJob extends AbstractJobPerformable<Ns8FetchQueueMessagesCronJobModel> {

    protected static final Logger LOG = LogManager.getLogger(Ns8FetchQueueMessagesJob.class);

    protected final Ns8QueueService ns8QueueService;
    protected final Ns8QueueMessageDao ns8QueueMessageDao;
    protected final Ns8MerchantService ns8MerchantService;

    public Ns8FetchQueueMessagesJob(final Ns8QueueService ns8QueueService,
                                    final Ns8QueueMessageDao ns8QueueMessageDao,
                                    final Ns8MerchantService ns8MerchantService) {
        this.ns8QueueService = ns8QueueService;
        this.ns8QueueMessageDao = ns8QueueMessageDao;
        this.ns8MerchantService = ns8MerchantService;
    }

    /**
     * Gets and saves messages from the ns8 queue. Will continuously get messages from the queue until
     * - there are no more messages
     * - the {@link Ns8FetchQueueMessagesCronJobModel#getMaxBatchSize()} number of messages have been received
     * - The queueUrl from {@link Ns8QueueService#getQueueUrl} expires
     *
     * @param cronJob the cronJob model
     * @return the outcome of the execution
     */
    @Override
    public PerformResult perform(final Ns8FetchQueueMessagesCronJobModel cronJob) {
        final int maxBatchSize = cronJob.getMaxBatchSize();
        final BaseSiteModel site = cronJob.getSite();
        final NS8MerchantModel ns8Merchant = site.getNs8Merchant();

        if (ns8Merchant == null) {
            LOG.error("No merchant configured for site [{}]", site::getUid);
            return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
        }
        if (!ns8MerchantService.isMerchantActive(ns8Merchant)) {
            LOG.error("Merchant [{}] is disabled for site [{}]", ns8Merchant::getQueueId, site::getUid);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }

        if (CollectionUtils.isEmpty(cronJob.getNs8MessageActionTypes())) {
            LOG.error("No message actions configured for the job.");
            return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
        }

        LOG.info("Fetching messages for site [{}], merchant [{}]",
                site::getUid, ns8Merchant::getQueueId);

        final String queueUrl = ns8QueueService.getQueueUrl(ns8Merchant.getApiKey());
        int totalMessagesReceived = 0;
        while (true) {
            if (callSuperClearAbortRequestedIfNeeded(cronJob)) {
                LOG.warn("The job [{}] has been aborted", cronJob.getCode());
                return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
            }

            List<Ns8QueueMessage> messages;
            try {
                messages = ns8QueueService.receiveMessages(queueUrl);
            } catch (final Ns8IntegrationException e) {
                return handleReceiveMessagesIntegrationException(e, totalMessagesReceived);
            }

            final AtomicInteger totalMessagesSaved = new AtomicInteger(0);
            if (CollectionUtils.isEmpty(messages)) {
                LOG.info("No more messages to process - finishing job");
                return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
            }

            try {
                processMessages(cronJob, ns8Merchant, messages, totalMessagesSaved);
            } catch (final Ns8IntegrationException e) {
                LOG.error("Got unexpected exception from delete message operation with api key [{}]. Exception error: [{}]",
                        ns8Merchant::getApiKey, e::getMessage);
                return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
            }

            totalMessagesReceived += messages.size();
            LOG.debug("Saved [{}] messages - total messages received [{}] - total messages deleted [{}]", totalMessagesSaved.get(), totalMessagesReceived, totalMessagesReceived);

            if (totalMessagesReceived >= maxBatchSize) {
                LOG.info("Completing job since maxBatchSize of [{}] reached ", maxBatchSize);
                return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
            }
        }
    }

    /**
     * Marks the cronjob as abortable
     *
     * @return true
     */
    @Override
    public boolean isAbortable() {
        return true;
    }

    /**
     * Calls the super clearAbortRequestedIfNeeded method
     *
     * @param cronJob the cronjob
     * @return true is the job has been aborted, false otherwise
     */
    protected boolean callSuperClearAbortRequestedIfNeeded(final Ns8FetchQueueMessagesCronJobModel cronJob) {
        return clearAbortRequestedIfNeeded(cronJob);
    }

    /**
     * Processes the received messages, saves the messages with action type that is in the cronjob message action types,
     * then deletes all processed messages
     *
     * @param cronJob            the cronjob
     * @param ns8Merchant        the current merchant
     * @param messages           the messages to process
     * @param totalMessagesSaved the total saved messages counter
     */
    protected void processMessages(final Ns8FetchQueueMessagesCronJobModel cronJob, final NS8MerchantModel ns8Merchant, final List<Ns8QueueMessage> messages, final AtomicInteger totalMessagesSaved) {
        messages.forEach(message -> {
            final String messageBody = message.getBody();
            final String messageAction = getAttrFromJson(messageBody, Ns8notificationsConstants.MESSAGE_BODY_ACTION_KEY);
            if (shouldSaveMessage(cronJob.getNs8MessageActionTypes(), messageAction, message.getMessageId())) {
                saveMessage(message.getMessageId(), messageBody);
                totalMessagesSaved.incrementAndGet();
            }

            ns8QueueService.deleteMessage(ns8Merchant.getApiKey(), message.getReceiptHandle());
        });
    }

    private boolean shouldSaveMessage(final Set<Ns8MessageActionType> supportedMessageActionTypes, final String messageActionType, final String messageId) {
        final boolean isActionTypeSupported = supportedMessageActionTypes.contains(Ns8MessageActionType.valueOf(messageActionType));
        if (!isActionTypeSupported) {
            LOG.info("The message with action type [{}] is not supported by the job and will not be saved.", messageActionType);
            return false;
        }

        boolean isMessageWithIdFound = !ns8QueueMessageDao.findNs8QueueMessageById(messageId).isEmpty();
        if (isMessageWithIdFound) {
            LOG.warn("The message with id [{}] has been already saved. Probably the deletion from the queue failed.", messageId);
            return false;
        }

        return true;
    }

    /**
     * Creates the {@link Ns8QueueMessageModel} and saves it
     *
     * @param messageId   the id of the message
     * @param messageBody the received message body
     */
    protected void saveMessage(final String messageId, final String messageBody) {
        final Ns8QueueMessageModel message = modelService.create(Ns8QueueMessageModel.class);
        message.setMessageId(messageId);
        message.setBody(messageBody);
        message.setOrderId(getAttrFromJson(messageBody, Ns8notificationsConstants.MESSAGE_BODY_ORDERID_KEY));
        message.setAction(Ns8MessageActionType.valueOf(getAttrFromJson(messageBody, Ns8notificationsConstants.MESSAGE_BODY_ACTION_KEY)));
        message.setStatus(Ns8MessageStatus.PENDING);
        modelService.save(message);
    }

    /**
     * Handles the NS8IntegrationException and updates the cronjob
     *
     * @param e the NS8IntegrationException
     * @return the cronjob result
     */
    protected PerformResult handleReceiveMessagesIntegrationException(final Ns8IntegrationException e, final int totalMessagesReceived) {
        if (HttpStatus.FORBIDDEN.equals(e.getHttpStatus()) && totalMessagesReceived == 0) {
            LOG.warn("Got unexpected 403 from receiveMessageUrl in the first run - aborting the job");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
        } else if (HttpStatus.FORBIDDEN.equals(e.getHttpStatus())) {
            LOG.debug("Got 403 from receiveMessageUrl - most likely the url expired - completing the job");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        } else {
            LOG.warn("Exception thrown from receiveMessageUrl", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
    }

    private String getAttrFromJson(final String messageBodyJson, final String attribute) {
        final JsonObject jsonObject = new JsonParser().parse(messageBodyJson).getAsJsonObject();
        return jsonObject.get(attribute).getAsString();
    }
}
