package com.ns8.hybris.notifications.messages.processing.strategies.impl;

import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.notifications.enums.Ns8MessageStatus;
import com.ns8.hybris.notifications.model.Ns8QueueMessageModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ProcessUpdateOrderStatusMessagesStrategyTest {

    private static final String ORDER_CODE_VALUE = "orderCode";
    private static final String BODY_STATUS_MERCHANT_REVIEW = "{'status' : 'MERCHANT_REVIEW'}";
    private static final String BODY_STATUS_APPROVED = "{'status' : 'APPROVED'}";
    private static final String BODY_STATUS_CANCELLED = "{'status' : 'CANCELLED'}";
    private static final String ORDER_PROCESS_DEFINITION_NAME = "order-process";
    private static final String PROCESS_CODE = "process_code";
    private static final String ORDER_VERIFIED_EVENT = "_CSAOrderVerified";

    @Spy
    @InjectMocks
    private Ns8ProcessUpdateOrderStatusMessagesStrategy testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private Ns8FraudService ns8FraudServiceMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private Ns8QueueMessageModel ns8QueueMessageMock;
    @Mock
    private OrderProcessModel orderProcessMock;
    @Mock
    private BaseStoreModel baseStoreMock;

    @Before
    public void setUp() {
        when(ns8QueueMessageMock.getBody()).thenReturn(BODY_STATUS_MERCHANT_REVIEW);
        when(ns8QueueMessageMock.getOrderId()).thenReturn(ORDER_CODE_VALUE);
        when(orderMock.getStatus()).thenReturn(OrderStatus.WAIT_FRAUD_MANUAL_CHECK);
        when(orderMock.getOrderProcess()).thenReturn(Collections.singletonList(orderProcessMock));
        when(orderProcessMock.getProcessDefinitionName()).thenReturn(ORDER_PROCESS_DEFINITION_NAME);
        when(orderProcessMock.getCode()).thenReturn(PROCESS_CODE);
        when(orderMock.getStore()).thenReturn(baseStoreMock);
        when(baseStoreMock.getSubmitOrderProcessCode()).thenReturn(ORDER_PROCESS_DEFINITION_NAME);
        when(ns8FraudServiceMock.isOrderFraudChecked(orderMock)).thenReturn(true);
        doNothing().when(ns8FraudServiceMock).updateOrderFraudReport(orderMock);
    }

    @Test
    public void processMessageForOrder_WhenOrderScoredInStatusWAIT_FRAUD_MANUAL_CHECKAndMessageStatusCancelled_ShouldSetTheOrderAsFraudulentAndTriggerTheEvent() {
        when(ns8QueueMessageMock.getBody()).thenReturn(BODY_STATUS_CANCELLED);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);

        final InOrder inOrder = inOrder(ns8FraudServiceMock, orderMock, modelServiceMock, businessProcessServiceMock, ns8QueueMessageMock);
        inOrder.verify(ns8FraudServiceMock).isOrderFraudChecked(orderMock);
        inOrder.verify(ns8FraudServiceMock).updateOrderFraudReport(orderMock);
        inOrder.verify(orderMock).setFraudulent(true);
        inOrder.verify(modelServiceMock).save(orderMock);
        inOrder.verify(businessProcessServiceMock).triggerEvent(PROCESS_CODE + ORDER_VERIFIED_EVENT);
        inOrder.verify(ns8QueueMessageMock).setStatus(Ns8MessageStatus.COMPLETED);
        inOrder.verify(modelServiceMock).save(ns8QueueMessageMock);
    }

    @Test
    public void processMessageForOrder_WhenOrderScoredInStatusWAIT_FRAUD_MANUAL_CHECKAndMessageStatusApproved_ShouldSetTheOrderAsFraudulentAndTriggerTheEvent() {
        when(ns8QueueMessageMock.getBody()).thenReturn(BODY_STATUS_APPROVED);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);

        final InOrder inOrder = inOrder(ns8FraudServiceMock, orderMock, modelServiceMock, businessProcessServiceMock, ns8QueueMessageMock);
        inOrder.verify(ns8FraudServiceMock).isOrderFraudChecked(orderMock);
        inOrder.verify(ns8FraudServiceMock).updateOrderFraudReport(orderMock);
        inOrder.verify(orderMock).setFraudulent(false);
        inOrder.verify(modelServiceMock).save(orderMock);
        inOrder.verify(businessProcessServiceMock).triggerEvent(PROCESS_CODE + ORDER_VERIFIED_EVENT);
        inOrder.verify(ns8QueueMessageMock).setStatus(Ns8MessageStatus.COMPLETED);
        inOrder.verify(modelServiceMock).save(ns8QueueMessageMock);
    }

    @Test
    public void processMessageForOrder_WhenOrderScoredInStatusWAIT_FRAUD_MANUAL_CHECKAndMessageStatusApprovedButNoOrderProcess_ShouldSetMessageFailed() {
        when(orderProcessMock.getProcessDefinitionName()).thenReturn("email-process");
        when(ns8QueueMessageMock.getBody()).thenReturn(BODY_STATUS_APPROVED);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);

        final InOrder inOrder = inOrder(ns8FraudServiceMock, orderMock, modelServiceMock, businessProcessServiceMock, ns8QueueMessageMock);
        inOrder.verify(ns8FraudServiceMock).isOrderFraudChecked(orderMock);
        inOrder.verify(ns8FraudServiceMock).updateOrderFraudReport(orderMock);
        inOrder.verify(orderMock).setFraudulent(false);
        inOrder.verify(modelServiceMock).save(orderMock);
        inOrder.verify(ns8QueueMessageMock).setFailReason(Mockito.anyString());
        inOrder.verify(ns8QueueMessageMock).setStatus(Ns8MessageStatus.FAILED);
        inOrder.verify(modelServiceMock).save(ns8QueueMessageMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processMessageForOrder_WhenOrderScoredInStatusWAIT_FRAUD_MANUAL_CHECKAndMessageStatusApprovedButOrderStoreNull_ShouldThrowException() {
        when(orderProcessMock.getProcessDefinitionName()).thenReturn("email-process");
        when(ns8QueueMessageMock.getBody()).thenReturn(BODY_STATUS_APPROVED);
        when(orderMock.getStore()).thenReturn(null);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);
    }

    @Test
    public void processMessageForOrder_WhenOrderScoredNotInStatusWAIT_FRAUD_MANUAL_CHECK_ShouldUpdateFailReasonAndSetTheMessageAsIgnored() {
        when(orderMock.getStatus()).thenReturn(OrderStatus.FRAUD_CHECKED);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);

        final InOrder inOrder = inOrder(ns8FraudServiceMock, modelServiceMock, ns8QueueMessageMock);
        inOrder.verify(ns8FraudServiceMock).isOrderFraudChecked(orderMock);
        inOrder.verify(ns8FraudServiceMock).updateOrderFraudReport(orderMock);
        inOrder.verify(ns8QueueMessageMock).setStatus(Ns8MessageStatus.COMPLETED);
        inOrder.verify(modelServiceMock).save(ns8QueueMessageMock);
    }

    @Test
    public void processMessageForOrder_WhenOrderNotScoredYet_ShouldAddThePayloadToTheOrderTriggerTheEventAndSetMessageCompleted() {
        when(ns8FraudServiceMock.isOrderFraudChecked(orderMock)).thenReturn(false);

        testObj.processMessageForOrder(ns8QueueMessageMock, orderMock);

        InOrder inOrder = inOrder(testObj, ns8FraudServiceMock);
        inOrder.verify(testObj).setRiskScore(ns8QueueMessageMock, orderMock);
        inOrder.verify(ns8FraudServiceMock).isOrderFraudChecked(orderMock);
        inOrder.verify(testObj).triggerOrderEvent(ns8QueueMessageMock);
        inOrder.verify(testObj).updateMessageStatus(ns8QueueMessageMock, Ns8MessageStatus.COMPLETED);
    }
}
