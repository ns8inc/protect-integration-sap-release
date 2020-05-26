package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudServiceResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.FraudService;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8FraudCheckOrderActionTest {

    @Spy
    @InjectMocks
    private Ns8FraudCheckOrderAction testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private FraudService fraudServiceMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private Ns8FraudServiceResponse fraudServiceResponseMock;
    @Mock
    private OrderProcessModel orderProcessMock;
    @Mock
    private FraudReportModel fraudReportMock;
    @Mock
    private OrderHistoryEntryModel orderHistoryEntryMock;
    private String providerNameMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "providerName", providerNameMock);
        Whitebox.setInternalState(testObj, "modelService", modelServiceMock);

        when(fraudServiceMock.recognizeOrderSymptoms(providerNameMock, orderMock)).thenReturn(fraudServiceResponseMock);
        when(orderProcessMock.getOrder()).thenReturn(orderMock);
        doReturn(fraudReportMock).when(testObj).createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject());
        doReturn(orderHistoryEntryMock).when(testObj).createHistoryLog(eq(providerNameMock), eq(orderMock), anyObject(), eq(null));
    }

    @Test
    public void executeAction_WhenResponseStatusAPPROVED_ShouldSetFraudStatusOK() throws Exception {
        when(fraudServiceResponseMock.getStatus()).thenReturn(Ns8FraudReportStatus.APPROVED);

        final AbstractFraudCheckAction.Transition result = testObj.executeAction(orderProcessMock);

        assertThat(result).isEqualTo(AbstractFraudCheckAction.Transition.OK);
        InOrder inOrder = inOrder(testObj, orderMock, modelServiceMock);
        inOrder.verify(testObj).createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject());
        inOrder.verify(testObj).createHistoryLog(eq(providerNameMock), eq(orderMock), eq(FraudStatus.OK), eq(null));
        inOrder.verify(orderMock).setFraudulent(false);
        inOrder.verify(orderMock).setPotentiallyFraudulent(false);
        inOrder.verify(orderMock).setStatus(OrderStatus.FRAUD_CHECKED);
        inOrder.verify(modelServiceMock).save(fraudReportMock);
        inOrder.verify(modelServiceMock).save(orderHistoryEntryMock);
        inOrder.verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void executeAction_WhenResponseStatusMERCHANT_REVIEW_ShouldSetFraudStatusCHECK() throws Exception {
        when(fraudServiceResponseMock.getStatus()).thenReturn(Ns8FraudReportStatus.MERCHANT_REVIEW);

        final AbstractFraudCheckAction.Transition result = testObj.executeAction(orderProcessMock);

        assertThat(result).isEqualTo(AbstractFraudCheckAction.Transition.POTENTIAL);
        InOrder inOrder = inOrder(testObj, orderMock, modelServiceMock);
        inOrder.verify(testObj).createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject());
        inOrder.verify(testObj).createHistoryLog(eq(providerNameMock), eq(orderMock), eq(FraudStatus.CHECK), eq(null));
        inOrder.verify(orderMock).setFraudulent(false);
        inOrder.verify(orderMock).setPotentiallyFraudulent(true);
        inOrder.verify(orderMock).setStatus(OrderStatus.FRAUD_CHECKED);
        inOrder.verify(modelServiceMock).save(fraudReportMock);
        inOrder.verify(modelServiceMock).save(orderHistoryEntryMock);
        inOrder.verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void executeAction_WhenResponseStatusCancelled_ShouldSetFraudStatusFRAUD() throws Exception {
        when(fraudServiceResponseMock.getStatus()).thenReturn(Ns8FraudReportStatus.CANCELLED);

        final AbstractFraudCheckAction.Transition result = testObj.executeAction(orderProcessMock);

        assertThat(result).isEqualTo(AbstractFraudCheckAction.Transition.FRAUD);
        InOrder inOrder = inOrder(testObj, orderMock, modelServiceMock);
        inOrder.verify(testObj).createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject());
        inOrder.verify(testObj).createHistoryLog(eq(providerNameMock), eq(orderMock), eq(FraudStatus.FRAUD), eq(null));
        inOrder.verify(orderMock).setFraudulent(true);
        inOrder.verify(orderMock).setPotentiallyFraudulent(false);
        inOrder.verify(orderMock).setStatus(OrderStatus.FRAUD_CHECKED);
        inOrder.verify(modelServiceMock).save(fraudReportMock);
        inOrder.verify(modelServiceMock).save(orderHistoryEntryMock);
        inOrder.verify(modelServiceMock).save(orderMock);
    }
}
