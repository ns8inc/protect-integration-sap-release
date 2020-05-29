package com.ns8.hybris.backoffice.actions.order;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8AccessOrderActionTest {

    private static final String CURRENT_ORDER_OUTPUT = "currentOrderOutput";
    private static final String ACTION_RESULT_SUCCESS_CODE = "success";
    private static final String ACTION_RESULT_ERROR_CODE = "error";

    @Spy
    @InjectMocks
    private Ns8AccessOrderAction testObj;

    @Mock
    private ActionContext<Object> actionContextMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;

    @Before
    public void setUp() {
        when(actionContextMock.getData()).thenReturn(orderMock);
        when(orderMock.getSite()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantMock.getEnabled()).thenReturn(true);
    }

    @Test
    public void canPerform_WhenMerchantIsActive_ShouldReturnTrue() {
        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isTrue();
    }

    @Test
    public void canPerform_WhenCtxDateIsNotOrderMode_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(new CustomerModel());

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_WhenBaseSiteIsMissing_ShouldReturnFalse() {
        when(orderMock.getSite()).thenReturn(null);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_WhenNS8MerchantIsMissing_ShouldReturnFalse() {
        when(baseSiteMock.getNs8Merchant()).thenReturn(null);

        final boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void canPerform_WhenNS8MerchantIsNotEnabled_ShouldReturnFalse() {
        when(ns8MerchantMock.getEnabled()).thenReturn(false);

        boolean result = testObj.canPerform(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void needsConfirmation_ShouldAlwaysReturnFalse() {
        final boolean result = testObj.needsConfirmation(actionContextMock);

        assertThat(result).isFalse();
    }

    @Test
    public void getConfirmationMessage_ShouldAlwaysReturnNull() {
        final String result = testObj.getConfirmationMessage(actionContextMock);

        assertThat(result).isNull();
    }

    @Test
    public void perform_WhenContextDataNull_ShouldReturnActionResultWithErrorCode() {
        when(actionContextMock.getData()).thenReturn(null);

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ACTION_RESULT_ERROR_CODE);
    }

    @Test
    public void perform_WhenContextDataContainsSomethingInvalid_ShouldReturnActionResultWithErrorCode() {
        when(actionContextMock.getData()).thenReturn(new CustomerModel());

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ACTION_RESULT_ERROR_CODE);
    }

    @Test
    public void perform_WhenContextDataContainsOrder_ShouldReturnActionResultWithSuccessCode() {
        doNothing().when(testObj).sendOutput(any(), any());
        when(actionContextMock.getData()).thenReturn(orderMock);

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ACTION_RESULT_SUCCESS_CODE);
        verify(testObj).sendOutput(CURRENT_ORDER_OUTPUT, orderMock);
    }
}
