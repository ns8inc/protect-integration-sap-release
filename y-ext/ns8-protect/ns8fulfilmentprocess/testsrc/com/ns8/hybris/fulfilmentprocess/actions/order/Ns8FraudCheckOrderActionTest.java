package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.core.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
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
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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
    private Ns8FraudService ns8FraudServiceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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
    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "providerName", providerNameMock);
        Whitebox.setInternalState(testObj, "modelService", modelServiceMock);

        when(fraudServiceMock.recognizeOrderSymptoms(providerNameMock, orderMock)).thenReturn(fraudServiceResponseMock);
        when(orderProcessMock.getOrder()).thenReturn(orderMock);
        when(ns8FraudServiceMock.createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject())).thenReturn(fraudReportMock);
        doReturn(orderHistoryEntryMock).when(testObj).createHistoryLog(eq(providerNameMock), eq(orderMock), anyObject(), eq(null));
        when(orderMock.getSite().getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.TRUE);
    }

    @Test
    public void executeAction_WhenResponseStatusAPPROVED_ShouldSetFraudStatusOK() throws Exception {
        when(fraudServiceResponseMock.getStatus()).thenReturn(Ns8FraudReportStatus.APPROVED);

        final AbstractFraudCheckAction.Transition result = testObj.executeAction(orderProcessMock);

        assertThat(result).isEqualTo(AbstractFraudCheckAction.Transition.OK);
        final InOrder inOrder = inOrder(ns8FraudServiceMock, testObj, orderMock, modelServiceMock);
        inOrder.verify(ns8FraudServiceMock).createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject());
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
        final InOrder inOrder = inOrder(ns8FraudServiceMock, testObj, orderMock, modelServiceMock);
        inOrder.verify(ns8FraudServiceMock).createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject());
        inOrder.verify(testObj).createHistoryLog(eq(providerNameMock), eq(orderMock), eq(FraudStatus.CHECK), eq(null));
        inOrder.verify(orderMock).setFraudulent(false);
        inOrder.verify(orderMock).setPotentiallyFraudulent(true);
        inOrder.verify(orderMock).setStatus(OrderStatus.FRAUD_SCORED);
        inOrder.verify(modelServiceMock).save(fraudReportMock);
        inOrder.verify(modelServiceMock).save(orderHistoryEntryMock);
        inOrder.verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void executeAction_WhenResponseStatusCancelled_ShouldSetFraudStatusFRAUD() throws Exception {
        when(fraudServiceResponseMock.getStatus()).thenReturn(Ns8FraudReportStatus.CANCELLED);

        final AbstractFraudCheckAction.Transition result = testObj.executeAction(orderProcessMock);

        assertThat(result).isEqualTo(AbstractFraudCheckAction.Transition.FRAUD);
        final InOrder inOrder = inOrder(ns8FraudServiceMock, testObj, orderMock, modelServiceMock);
        inOrder.verify(ns8FraudServiceMock).createFraudReport(eq(providerNameMock), eq(fraudServiceResponseMock), eq(orderMock), anyObject());
        inOrder.verify(testObj).createHistoryLog(eq(providerNameMock), eq(orderMock), eq(FraudStatus.FRAUD), eq(null));
        inOrder.verify(orderMock).setFraudulent(true);
        inOrder.verify(orderMock).setPotentiallyFraudulent(false);
        inOrder.verify(orderMock).setStatus(OrderStatus.FRAUD_CHECKED);
        inOrder.verify(modelServiceMock).save(fraudReportMock);
        inOrder.verify(modelServiceMock).save(orderHistoryEntryMock);
        inOrder.verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void executeAction_WhenMerchantIsDisabled_ShouldReturnOk() throws Exception {
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.FALSE);

        final AbstractFraudCheckAction.Transition result = testObj.executeAction(orderProcessMock);

        assertThat(result).isEqualTo(AbstractFraudCheckAction.Transition.OK);
        verifyZeroInteractions(fraudServiceMock);

        final InOrder inOrder = inOrder(orderMock, modelServiceMock);
        inOrder.verify(orderMock).setFraudulent(false);
        inOrder.verify(orderMock).setStatus(OrderStatus.FRAUD_CHECKED);
        inOrder.verify(modelServiceMock).save(orderMock);
    }
}
