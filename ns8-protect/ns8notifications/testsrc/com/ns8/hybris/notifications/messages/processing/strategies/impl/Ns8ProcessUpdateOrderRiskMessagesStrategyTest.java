package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ProcessUpdateOrderRiskMessagesStrategyTest {

    @Spy
    @InjectMocks
    private Ns8ProcessUpdateOrderRiskMessagesStrategy testObj;

    @Mock
    private OrderModel orderMock;
    @Mock
    private Ns8QueueMessageModel ns8QueueMessageMock;
    @Mock
    private Ns8FraudService ns8FraudService;

    @Test
    public void processMessageForOrder_WhenOrderNotScoredYet_ShouldAddThePayloadToTheOrderTriggerTheEventAndSetMessageCompleted() {
        doNothing().when(testObj).setRiskScore(ns8QueueMessageMock, orderMock);
        doNothing().when(testObj).triggerOrderEvent(ns8QueueMessageMock);
        doNothing().when(testObj).updateMessageStatus(ns8QueueMessageMock, Ns8MessageStatus.COMPLETED);
        when(ns8FraudService.isOrderFraudChecked(orderMock)).thenReturn(false);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);

        InOrder inOrder = inOrder(testObj, ns8FraudService);
        inOrder.verify(testObj).setRiskScore(ns8QueueMessageMock, orderMock);
        inOrder.verify(ns8FraudService).isOrderFraudChecked(orderMock);
        inOrder.verify(testObj).triggerOrderEvent(ns8QueueMessageMock);
        inOrder.verify(testObj).updateMessageStatus(ns8QueueMessageMock, Ns8MessageStatus.COMPLETED);
    }

    @Test
    public void processMessageForOrder_WhenOrderScored_ShouldAddThePayloadToTheOrderUpdateTheFraudReportAndSetMessageCompleted() {
        doNothing().when(testObj).setRiskScore(ns8QueueMessageMock, orderMock);
        doNothing().when(testObj).triggerOrderEvent(ns8QueueMessageMock);
        doNothing().when(ns8FraudService).updateOrderFraudReport(orderMock);
        doNothing().when(testObj).updateMessageStatus(ns8QueueMessageMock, Ns8MessageStatus.COMPLETED);
        when(ns8FraudService.isOrderFraudChecked(orderMock)).thenReturn(true);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);

        InOrder inOrder = inOrder(testObj, ns8FraudService);
        inOrder.verify(testObj).setRiskScore(ns8QueueMessageMock, orderMock);
        inOrder.verify(ns8FraudService).isOrderFraudChecked(orderMock);
        inOrder.verify(ns8FraudService).updateOrderFraudReport(orderMock);
        inOrder.verify(testObj).updateMessageStatus(ns8QueueMessageMock, Ns8MessageStatus.COMPLETED);
    }
}
