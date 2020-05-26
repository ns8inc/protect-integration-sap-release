package com.ns8.hybris.notifications.jobs;

import com.ns8.hybris.notifications.daos.Ns8QueueMessageDao;
import com.ns8.hybris.notifications.enums.Ns8MessageActionType;
import com.ns8.hybris.notifications.messages.processing.strategies.Ns8ProcessMessagesStrategy;
import com.ns8.hybris.notifications.messages.processing.strategies.impl.Ns8ProcessUpdateOrderRiskMessagesStrategy;
import com.ns8.hybris.notifications.model.Ns8ProcessMessagesCronJobModel;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ProcessMessagesJobTest {

    @InjectMocks
    private Ns8ProcessMessagesJob testObj;

    @Mock
    private Map<Ns8MessageActionType, Ns8ProcessMessagesStrategy> messageStrategyMapMock;
    @Mock
    private Ns8QueueMessageDao ns8QueueMessageDaoMock;
    @Mock
    private Ns8ProcessUpdateOrderRiskMessagesStrategy ns8ProcessUpdateOrderRiskMessagesStrategyMock;
    @Mock
    private Ns8ProcessMessagesCronJobModel ns8ProcessMessagesCronJobMock;
    @Mock
    private Ns8QueueMessageModel message1Mock, message2Mock;

    @Before
    public void setUp() {
        when(ns8QueueMessageDaoMock.findPendingNs8QueueMessages()).thenReturn(Arrays.asList(message1Mock, message2Mock));
        when(message1Mock.getAction()).thenReturn(Ns8MessageActionType.UPDATE_ORDER_RISK_EVENT);
        when(message2Mock.getAction()).thenReturn(Ns8MessageActionType.UPDATE_ORDER_RISK_EVENT);
        when(messageStrategyMapMock.containsKey(Ns8MessageActionType.UPDATE_ORDER_RISK_EVENT)).thenReturn(true);
        when(messageStrategyMapMock.get(Ns8MessageActionType.UPDATE_ORDER_RISK_EVENT)).thenReturn(ns8ProcessUpdateOrderRiskMessagesStrategyMock);
    }

    @Test
    public void perform_ShouldProcessMessagesAndReturnSuccessResult() {
        final PerformResult result = testObj.perform(ns8ProcessMessagesCronJobMock);

        verify(ns8ProcessUpdateOrderRiskMessagesStrategyMock).processMessage(message1Mock);
        verify(ns8ProcessUpdateOrderRiskMessagesStrategyMock).processMessage(message2Mock);
        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void perform_WhenStrategyDoesNotExist_ShouldJustProcessTheMessagesWithStrategy() {
        when(message2Mock.getAction()).thenReturn(Ns8MessageActionType.UPDATE_ORDER_STATUS_EVENT);

        final PerformResult result = testObj.perform(ns8ProcessMessagesCronJobMock);

        verify(ns8ProcessUpdateOrderRiskMessagesStrategyMock).processMessage(message1Mock);
        verify(ns8ProcessUpdateOrderRiskMessagesStrategyMock, never()).processMessage(message2Mock);
        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void perform_WhenNoMessagesFound_ShouldReturnSuccessResult() {
        when(ns8QueueMessageDaoMock.findPendingNs8QueueMessages()).thenReturn(Collections.emptyList());

        final PerformResult result = testObj.perform(ns8ProcessMessagesCronJobMock);

        verifyZeroInteractions(ns8ProcessUpdateOrderRiskMessagesStrategyMock);
        assertThat(result.getResult()).isEqualTo(CronJobResult.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.FINISHED);
    }

    @Test
    public void perform_WhenThereIsAnExceptionInProcessing_ShouldReturnErrorResult() {
        doThrow(new RuntimeException()).when(ns8ProcessUpdateOrderRiskMessagesStrategyMock).processMessage(message1Mock);

        final PerformResult result = testObj.perform(ns8ProcessMessagesCronJobMock);

        verify(ns8ProcessUpdateOrderRiskMessagesStrategyMock).processMessage(message1Mock);
        assertThat(result.getResult()).isEqualTo(CronJobResult.ERROR);
        assertThat(result.getStatus()).isEqualTo(CronJobStatus.ABORTED);
    }
}
