package com.ns8.hybris.notifications.daos.impl;

import com.ns8.hybris.notifications.daos.Ns8QueueMessageDao;
import com.ns8.hybris.notifications.enums.Ns8MessageActionType;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ns8.hybris.notifications.enums.Ns8MessageStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class DefaultNs8QueueMessageDaoIntegrationTest extends ServicelayerTransactionalTest {

    @Resource
    private Ns8QueueMessageDao ns8QueueMessageDao;

    @Resource
    private ModelService modelService;

    private Ns8QueueMessageModel pending1Message, pending2Message, failed1Message, failed2Message;

    @Before
    public void setUp() {
        pending1Message = createNs8Message("message1", "order1", Ns8MessageActionType.UPDATE_ORDER_RISK_EVENT, Ns8MessageStatus.PENDING);
        pending2Message = createNs8Message("message2", "order2", Ns8MessageActionType.UPDATE_ORDER_STATUS_EVENT, Ns8MessageStatus.PENDING);
        failed1Message = createNs8Message("message3", "order3", Ns8MessageActionType.UPDATE_ORDER_RISK_EVENT, FAILED);
        failed2Message = createNs8Message("message4", "order4", Ns8MessageActionType.UPDATE_ORDER_STATUS_EVENT, FAILED);
    }

    @Test
    public void findPendingNs8QueueMessages_ShouldGetAllPendingMessages() {
        final List<Ns8QueueMessageModel> result = ns8QueueMessageDao.findPendingNs8QueueMessages();

        assertThat(result).containsExactly(pending1Message, pending2Message);
    }

    @Test
    public void findNs8QueueMessageById_ShouldGetMessageWithCorrectId() {
        final Optional<Ns8QueueMessageModel> result = ns8QueueMessageDao.findNs8QueueMessageById("message2");

        assertThat(result.get()).isEqualTo(pending2Message);
    }

    @Test
    public void findNs8QueueMessageById_ShouldGetEmptyIfNoMessageWithIdFound() {
        final Optional<Ns8QueueMessageModel> result = ns8QueueMessageDao.findNs8QueueMessageById("message5");

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void findNs8QueueMessagesByStatusCreatedBeforeDate_ShouldGetQueueMessagesOlderThanSpecified() {
        final List<Ns8QueueMessageModel> results = ns8QueueMessageDao.findNs8QueueMessagesByStatusCreatedBeforeDate(FAILED, new Date());

        Assertions.assertThat(results).containsExactlyInAnyOrder(failed1Message, failed2Message);
    }

    private Ns8QueueMessageModel createNs8Message(final String messageId, final String orderId, final Ns8MessageActionType eventType, final Ns8MessageStatus eventStatus) {
        final Ns8QueueMessageModel message = modelService.create(Ns8QueueMessageModel.class);
        message.setMessageId(messageId);
        message.setOrderId(orderId);
        message.setStatus(eventStatus);
        message.setAction(eventType);
        message.setBody("{some body}");
        modelService.save(message);
        return message;
    }
}
