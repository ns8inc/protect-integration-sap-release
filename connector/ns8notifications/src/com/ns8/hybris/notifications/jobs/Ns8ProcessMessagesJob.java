package com.ns8.hybris.notifications.jobs;

import com.ns8.hybris.notifications.daos.Ns8QueueMessageDao;
import com.ns8.hybris.notifications.enums.Ns8MessageActionType;
import com.ns8.hybris.notifications.messages.processing.strategies.Ns8ProcessMessagesStrategy;
import com.ns8.hybris.notifications.model.Ns8ProcessMessagesCronJobModel;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Job definition to process NS8 pending messages
 */
public class Ns8ProcessMessagesJob extends AbstractJobPerformable<Ns8ProcessMessagesCronJobModel> {

    protected static final Logger LOG = LogManager.getLogger(Ns8ProcessMessagesJob.class);

    protected final Map<Ns8MessageActionType, Ns8ProcessMessagesStrategy> messageStrategyMap;
    protected final Ns8QueueMessageDao ns8QueueMessageDao;

    public Ns8ProcessMessagesJob(final Map<Ns8MessageActionType, Ns8ProcessMessagesStrategy> messageStrategyMap,
                                 final Ns8QueueMessageDao ns8QueueMessageDao) {
        this.messageStrategyMap = messageStrategyMap;
        this.ns8QueueMessageDao = ns8QueueMessageDao;
    }

    /**
     * Processes all the selected messages calling the correct strategy based on the cronjob messageActionType.
     *
     * @param cronJob the cronjob model
     * @return the outcome of the execution
     */
    @Override
    public PerformResult perform(final Ns8ProcessMessagesCronJobModel cronJob) {
        LOG.info("Process Ns8 Messages cronjob started.");

        final List<Ns8QueueMessageModel> messages = ns8QueueMessageDao.findPendingNs8QueueMessages();

        for (Ns8QueueMessageModel message : messages) {
            if (messageStrategyMap.containsKey(message.getAction())) {
                final Ns8ProcessMessagesStrategy processMessagesStrategy = messageStrategyMap.get(message.getAction());

                try {
                    processMessagesStrategy.processMessage(message);
                } catch (final Exception e) {
                    LOG.error("Error processing the ns8 message for order [{}] and message action type [{}]. Error Message: [{}]",
                            message::getOrderId, message::getAction, e::getMessage);
                    return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
                }
            }
        }

        LOG.info("Process Ns8 Messages cronjob finished. Processed [{}] ns8 messages.", messages.size());
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}
