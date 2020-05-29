package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8AbstractProcessMessagesStrategyTest {

    private static final String ORDER_ID = "order_id";
    private static final String BODY_PAYLOAD = "{\"body\" : \"something\" }";
    private static final String ERROR_MESSAGE = "errorMessage";

    @Spy
    private Ns8AbstractProcessMessagesStrategy testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Ns8OrderDao orderDaoMock;
    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private Ns8FraudService ns8FraudServiceMock;
    @Mock
    private Ns8QueueMessageModel messageMock;
    @Mock
    private OrderModel orderMock;

    private List<OrderStatus> updateAllowedOrderStatuses = Arrays.asList(OrderStatus.FRAUD_SCORE_PENDING, OrderStatus.FRAUD_SCORED, OrderStatus.WAIT_FRAUD_MANUAL_CHECK);

    @Before
    public void setUp() {
        testObj = Mockito.mock(
                Ns8AbstractProcessMessagesStrategy.class,
                Mockito.CALLS_REAL_METHODS);
        Whitebox.setInternalState(testObj, "modelService", modelServiceMock);
        Whitebox.setInternalState(testObj, "businessProcessService", businessProcessServiceMock);
        Whitebox.setInternalState(testObj, "orderDao", orderDaoMock);
        Whitebox.setInternalState(testObj, "updateAllowedOrderStatuses", updateAllowedOrderStatuses);
        Whitebox.setInternalState(testObj, "ns8FraudService", ns8FraudServiceMock);

        when(messageMock.getBody()).thenReturn(BODY_PAYLOAD);
        when(messageMock.getOrderId()).thenReturn(ORDER_ID);
        when(orderDaoMock.findOrderForCode(ORDER_ID)).thenReturn(Optional.of(orderMock));
        when(orderMock.getStatus()).thenReturn(OrderStatus.FRAUD_SCORED);
    }

    @Test
    public void processMessage_WhenOrderFoundAndStatusAllowedForUpdate_ShouldProcessTheMessage() {
        testObj.processMessage(messageMock);

        final InOrder inOrder = inOrder(orderDaoMock, testObj);
        inOrder.verify(orderDaoMock).findOrderForCode(ORDER_ID);
        inOrder.verify(testObj).processMessageForOrder(messageMock, orderMock);
    }

    @Test
    public void processMessage_WhenOrderFoundButNotInAllowedUpdateStatuses_ShouldIgnoreTheMessage() {
        when(orderMock.getStatus()).thenReturn(OrderStatus.SUSPENDED);

        testObj.processMessage(messageMock);

        final InOrder inOrder = inOrder(orderDaoMock, messageMock, modelServiceMock);
        inOrder.verify(orderDaoMock).findOrderForCode(ORDER_ID);
        inOrder.verify(messageMock).setFailReason(Mockito.anyString());
        inOrder.verify(messageMock).setStatus(Ns8MessageStatus.IGNORED);
        inOrder.verify(modelServiceMock).save(messageMock);

        verify(testObj, never()).processMessageForOrder(messageMock, orderMock);
    }

    @Test
    public void processMessage_WhenOrderNotFound_ShouldSetMessageFailed() {
        when(orderDaoMock.findOrderForCode(ORDER_ID)).thenReturn(Optional.empty());

        testObj.processMessage(messageMock);

        final InOrder inOrder = inOrder(orderDaoMock, messageMock, modelServiceMock);
        inOrder.verify(orderDaoMock).findOrderForCode(ORDER_ID);
        inOrder.verify(messageMock).setFailReason(Mockito.anyString());
        inOrder.verify(messageMock).setStatus(Ns8MessageStatus.FAILED);
        inOrder.verify(modelServiceMock).save(messageMock);

        verify(testObj, never()).processMessageForOrder(messageMock, orderMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processMessage_WhenNullMessage_ShouldThrowException() {
        testObj.processMessage(null);
    }

    @Test
    public void setRiskScore_ShouldSetThePayloadAndTriggerTheEvent() {
        testObj.setRiskScore(messageMock, orderMock);

        verify(orderMock).setRiskEventPayload(BODY_PAYLOAD);
        verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void updateFailReason_ShouldUpdateTheMessageFailReason() {
        testObj.updateFailReason(ERROR_MESSAGE, messageMock);

        verify(messageMock).setFailReason(ERROR_MESSAGE);
        verify(modelServiceMock).save(messageMock);
    }

    @Test
    public void updateMessageStatus_ShouldUpdateTheMessageStatusAndSaveTheMessage() {
        testObj.updateMessageStatus(messageMock, Ns8MessageStatus.COMPLETED);

        verify(messageMock).setStatus(Ns8MessageStatus.COMPLETED);
        verify(modelServiceMock).save(messageMock);
    }

    @Test
    public void triggerOrderEvent_TriggersTheBusinessProcessEvent() {
        testObj.triggerOrderEvent(messageMock);

        verify(businessProcessServiceMock).triggerEvent(ORDER_ID + "_NS8ScoreReceived");
    }
}
