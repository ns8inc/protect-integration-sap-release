package com.ns8.hybris.notifications.jobs;

import com.ns8.hybris.notifications.model.Ns8QueueMessagesCleanUpCronJobModel;
import com.ns8.hybris.notifications.services.Ns8QueueMessagesCleanUpService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

/**
 * Cleanup of the queue messages
 */
public class Ns8QueueMessagesCleanUpJob extends AbstractJobPerformable<Ns8QueueMessagesCleanUpCronJobModel> {

    protected final Ns8QueueMessagesCleanUpService ns8QueueMessagesCleanUpService;

    public Ns8QueueMessagesCleanUpJob(final Ns8QueueMessagesCleanUpService ns8QueueMessagesCleanUpService) {
        this.ns8QueueMessagesCleanUpService = ns8QueueMessagesCleanUpService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PerformResult perform(final Ns8QueueMessagesCleanUpCronJobModel cronJob) {
        ns8QueueMessagesCleanUpService.doCleanUp(cronJob.getNs8MessageStatus(), cronJob.getAgeInDaysBeforeDeletion());
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}
