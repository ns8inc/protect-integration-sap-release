package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.services.NS8FraudService;
import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.task.RetryLaterException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NS8ScoreOrderActionTest {

    @InjectMocks
    private NS8ScoreOrderAction testObj;

    @Mock
    private NS8FraudService ns8FraudServiceMock;
    @Mock
    private NS8APIService ns8APIServiceMock;

    @Mock
    private OrderProcessModel orderProcessMock;
    @Mock
    private OrderModel orderMock;

    @Before
    public void setUp() {
        when(orderProcessMock.getOrder()).thenReturn(orderMock);
    }

    @Test
    public void execute_orderNotScored_shouldSendOrderAndWait() {
        when(ns8FraudServiceMock.hasOrderBeenScored(orderMock)).thenReturn(false);

        final String result = testObj.execute(orderProcessMock);

        verify(ns8APIServiceMock).triggerCreateOrderActionEvent(orderMock);
        assertThat(result).isEqualTo("WAIT");
    }

    @Test
    public void execute_500ErrorWhenSendingOrder_shouldThrowRetryLaterException() {
        final NS8IntegrationException ns8IntegrationException = new NS8IntegrationException("message", HttpStatus.INTERNAL_SERVER_ERROR);
        doThrow(ns8IntegrationException).when(ns8APIServiceMock).triggerCreateOrderActionEvent(orderMock);

        final Throwable thrown = catchThrowable(() -> testObj.execute(orderProcessMock));

        assertThat(thrown)
                .isInstanceOf(RetryLaterException.class)
                .hasCause(ns8IntegrationException);
    }

    @Test
    public void execute_400ErrorWhenSendingOrder_shouldReturnNok() {
        final HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        final NS8IntegrationException ns8IntegrationException = new NS8IntegrationException("message", HttpStatus.I_AM_A_TEAPOT, clientErrorException);
        doThrow(ns8IntegrationException).when(ns8APIServiceMock).triggerCreateOrderActionEvent(orderMock);

        final String result = testObj.execute(orderProcessMock);

        assertThat(result).isEqualTo("NOK");
    }

    @Test
    public void execute_orderScored_shouldNotSendOrder() {
        when(ns8FraudServiceMock.hasOrderBeenScored(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        verifyZeroInteractions(ns8APIServiceMock);
        assertThat(result).isEqualTo("OK");
    }
}
