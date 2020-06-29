package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.task.RetryLaterException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8ScoreOrderActionTest {

    private static final String WAIT = "WAIT";
    private static final String OK = "OK";
    private static final String NOK = "NOK";

    @Spy
    @InjectMocks
    private Ns8ScoreOrderAction testObj;

    @Mock
    private Ns8FraudService ns8FraudServiceMock;
    @Mock
    private Ns8ApiService ns8ApiServiceMock;
    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private OrderProcessModel orderProcessMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "modelService", modelServiceMock);

        when(orderProcessMock.getOrder()).thenReturn(orderMock);
        when(orderMock.getStatus()).thenReturn(OrderStatus.PAYMENT_AMOUNT_RESERVED);
        when(orderMock.getSite().getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.TRUE);
    }

    @Test
    public void execute_WhenOrderNotScored_ShouldSendOrderAndWait() {
        when(ns8FraudServiceMock.hasOrderBeenScored(orderMock)).thenReturn(false);

        final String result = testObj.execute(orderProcessMock);

        verify(ns8ApiServiceMock).triggerCreateOrderActionEvent(orderMock);
        verify(orderMock).setStatus(OrderStatus.FRAUD_SCORE_PENDING);
        verify(modelServiceMock).save(orderMock);
        assertThat(result).isEqualTo(WAIT);
    }

    @Test
    public void execute_WhenMerchantIsActive_ShouldSaveMerchantEnabledAsTrue() {
        when(ns8FraudServiceMock.hasOrderBeenScored(orderMock)).thenReturn(false);

        final String result = testObj.execute(orderProcessMock);

        verify(ns8ApiServiceMock).triggerCreateOrderActionEvent(orderMock);
        verify(orderMock).setStatus(OrderStatus.FRAUD_SCORE_PENDING);
        verify(orderMock).setMerchantEnabled(Boolean.TRUE);
        verify(modelServiceMock).save(orderMock);
        assertThat(result).isEqualTo(WAIT);

    }

    @Test
    public void execute_WhenMerchantIsInactive_ShouldSaveMerchantEnabledAsFalse() {
        when(ns8FraudServiceMock.hasOrderBeenScored(orderMock)).thenReturn(false);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.FALSE);

        final String result = testObj.execute(orderProcessMock);

        verifyZeroInteractions(ns8ApiServiceMock);
        assertThat(result).isEqualTo(OK);
        assertThat(orderMock.getMerchantEnabled()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void execute_When500ErrorWhenSendingOrder_ShouldThrowRetryLaterException() {
        final Ns8IntegrationException ns8IntegrationException = new Ns8IntegrationException("message", HttpStatus.INTERNAL_SERVER_ERROR);
        doThrow(ns8IntegrationException).when(ns8ApiServiceMock).triggerCreateOrderActionEvent(orderMock);

        final Throwable thrown = catchThrowable(() -> testObj.execute(orderProcessMock));

        assertThat(orderMock.getStatus()).isEqualTo(OrderStatus.PAYMENT_AMOUNT_RESERVED);
        assertThat(thrown)
                .isInstanceOf(RetryLaterException.class)
                .hasCause(ns8IntegrationException);
        assertThat(orderMock.getMerchantEnabled()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void execute_When400ErrorWhenSendingOrder_ShouldReturnNok() {
        final HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final Ns8IntegrationException ns8IntegrationException = new Ns8IntegrationException("message", HttpStatus.I_AM_A_TEAPOT, clientErrorException);
        doThrow(ns8IntegrationException).when(ns8ApiServiceMock).triggerCreateOrderActionEvent(orderMock);

        final String result = testObj.execute(orderProcessMock);

        assertThat(orderMock.getStatus()).isEqualTo(OrderStatus.PAYMENT_AMOUNT_RESERVED);
        assertThat(result).isEqualTo(NOK);
        assertThat(orderMock.getMerchantEnabled()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void execute_WhenOrderScored_ShouldNotSendOrder() {
        when(ns8FraudServiceMock.hasOrderBeenScored(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        verifyZeroInteractions(ns8ApiServiceMock);
        verify(orderMock).setStatus(OrderStatus.FRAUD_SCORED);
        verify(orderMock).setMerchantEnabled(Boolean.TRUE);
        verify(modelServiceMock).save(orderMock);
        assertThat(result).isEqualTo(OK);
    }

    @Test
    public void execute_WhenMerchantDisabled_ShouldNotSendOrder() {
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(Boolean.FALSE);

        final String result = testObj.execute(orderProcessMock);

        verifyZeroInteractions(ns8ApiServiceMock);
        assertThat(result).isEqualTo(OK);
        assertThat(orderMock.getMerchantEnabled()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void getTransitions_ShouldGetAllTransitions() {
        final Set<String> results = testObj.getTransitions();

        assertThat(results.containsAll(Arrays.asList(WAIT, OK, NOK))).isTrue();
    }
}
