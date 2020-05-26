package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ProcessUpdateOrderRiskMessagesStrategyTest {

    private static final String ORDER_CODE_VALUE = "orderCode";
    private static final String BODY_VALUE = "{something : 'fsdfsd'}";

    @InjectMocks
    private Ns8ProcessUpdateOrderRiskMessagesStrategy testObj;

    @Mock
    private Ns8OrderDao orderDaoMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private Ns8QueueMessageModel ns8QueueMessageMock;

    @Before
    public void setUp() {
        when(orderDaoMock.findOrderForCode(ORDER_CODE_VALUE)).thenReturn(Optional.of(orderMock));
        when(ns8QueueMessageMock.getBody()).thenReturn(BODY_VALUE);
        when(ns8QueueMessageMock.getOrderId()).thenReturn(ORDER_CODE_VALUE);
    }

    @Test
    public void processMessage_WhenOrderFound_ShouldAddThePayloadToTheOrderTriggerTheEventAndSetMessageCompleted() {
        testObj.processMessage(ns8QueueMessageMock);

        InOrder inOrder = inOrder(orderDaoMock, orderMock, modelServiceMock, businessProcessServiceMock, ns8QueueMessageMock);
        inOrder.verify(orderDaoMock).findOrderForCode(ORDER_CODE_VALUE);
        inOrder.verify(orderMock).setRiskEventPayload(BODY_VALUE);
        inOrder.verify(modelServiceMock).save(orderMock);
        inOrder.verify(businessProcessServiceMock).triggerEvent(ORDER_CODE_VALUE + "_NS8ScoreReceived");
        inOrder.verify(ns8QueueMessageMock).setStatus(Ns8MessageStatus.COMPLETED);
        inOrder.verify(modelServiceMock).save(ns8QueueMessageMock);
    }

    @Test
    public void processMessage_WhenOrderNotFound_ShouldSetMessageFailed() {
        when(orderDaoMock.findOrderForCode(ORDER_CODE_VALUE)).thenReturn(Optional.empty());

        testObj.processMessage(ns8QueueMessageMock);

        InOrder inOrder = inOrder(orderDaoMock, ns8QueueMessageMock, modelServiceMock);
        inOrder.verify(orderDaoMock).findOrderForCode(ORDER_CODE_VALUE);
        inOrder.verify(ns8QueueMessageMock).setFailReason(Mockito.anyString());
        inOrder.verify(ns8QueueMessageMock).setStatus(Ns8MessageStatus.FAILED);
        inOrder.verify(modelServiceMock).save(ns8QueueMessageMock);

        verifyZeroInteractions(orderMock);
        verify(modelServiceMock, never()).save(orderMock);
        verifyZeroInteractions(businessProcessServiceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processMessage_WhenNullMessage_ShouldThrowException() {
        testObj.processMessage(null);
    }
}
