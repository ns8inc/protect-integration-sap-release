package com.ns8.hybris.core.jobs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ns8.hybris.core.constants.Ns8servicesConstants;
import com.ns8.hybris.core.data.queue.Ns8QueueMessage;
import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.model.Ns8FetchQueueMessagesCronJobModel;
import com.ns8.hybris.core.model.Ns8QueueMessageModel;
import com.ns8.hybris.core.services.Ns8QueueService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.List;

/**
 * Job definition to fetch NS8 messages from the queue
 */
public class Ns8FetchQueueMessagesJob extends AbstractJobPerformable<Ns8FetchQueueMessagesCronJobModel> {

    protected static final Logger LOG = LogManager.getLogger(Ns8FetchQueueMessagesJob.class);

    protected final Ns8QueueService ns8QueueService;

    public Ns8FetchQueueMessagesJob(final Ns8QueueService ns8QueueService) {
        this.ns8QueueService = ns8QueueService;
    }

    /**
     * Gets and saves messages from the ns8 queue. Will continuously get messages from the queue until
     * - there are no more messages
     * - the {@link Ns8FetchQueueMessagesCronJobModel#getMaxBatchSize()} number of messages have been received
     * - The queueUrl from {@link Ns8QueueService#getQueueUrl} expires
     *
     * @param cronJob the cronjob model
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
        LOG.info("Fetching messages for site [{}], merchant [{}]",
                site::getUid, ns8Merchant::getQueueId);

        final String queueUrl = ns8QueueService.getQueueUrl(ns8Merchant.getApiKey());
        int totalMessagesReceived = 0;
        while (true) {
            if (callSuperClearAbortRequestedIfNeeded(cronJob)) {
                LOG.info("The job [{}] has been aborted", cronJob.getCode());
                return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
            }

            try {
                final List<Ns8QueueMessage> messages = ns8QueueService.receiveMessages(queueUrl);
                if (CollectionUtils.isEmpty(messages)) {
                    LOG.info("No more messages to process - finishing job");
                    return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
                }
                messages.forEach(message -> {
                    createMessageModel(message);
                    ns8QueueService.deleteMessage(ns8Merchant.getApiKey(), message.getReceiptHandle());
                });
                totalMessagesReceived += messages.size();
                LOG.info(MessageFormat.format("Saved [{0}] messages - total messages saved [{1}]", messages.size(), totalMessagesReceived));
            } catch (final NS8IntegrationException e) {
                return handleIntegrationException(e, totalMessagesReceived);
            }
            if (totalMessagesReceived >= maxBatchSize) {
                LOG.info("Completing job since maxBatchSize of [{}] reached ", maxBatchSize);
                return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
            }
        }
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
     * Marks the cronjob as abortable
     *
     * @return true
     */
    @Override
    public boolean isAbortable() {
        return true;
    }

    /**
     * Creates the {@link Ns8QueueMessageModel} and stores it
     *
     * @param message the received message
     */
    protected void createMessageModel(final Ns8QueueMessage message) {
        final Ns8QueueMessageModel messageModel = modelService.create(Ns8QueueMessageModel.class);
        final String body = message.getBody();
        messageModel.setBody(body);
        messageModel.setOrderId(getAttrFromJson(body, Ns8servicesConstants.MESSAGE_BODY_ORDERID_KEY));
        messageModel.setAction(getAttrFromJson(body, Ns8servicesConstants.MESSAGE_BODY_ACTION_KEY));
        modelService.save(messageModel);
    }

    /**
     * Handles the NS8IntegrationException and updates the cronjob
     *
     * @param e the NS8IntegrationException
     * @return the cronjob result
     */
    protected PerformResult handleIntegrationException(final NS8IntegrationException e, final int totalMessagesReceived) {
        if (HttpStatus.FORBIDDEN.equals(e.getHttpStatus()) && totalMessagesReceived == 0) {
            LOG.info("Got unexpected 403 from receiveMessageUrl in the first run - aborting the job");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
        } else if (HttpStatus.FORBIDDEN.equals(e.getHttpStatus())) {
            LOG.info("Got 403 from receiveMessageUrl - most likely the url expired - completing the job");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        } else {
            LOG.error("Exception thrown from receiveMessageUrl", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
    }

    private String getAttrFromJson(final String messageBodyJson, final String attribute) {
        final JsonObject jsonObject = new JsonParser().parse(messageBodyJson).getAsJsonObject();
        return jsonObject.get(attribute).getAsString();
    }
}
