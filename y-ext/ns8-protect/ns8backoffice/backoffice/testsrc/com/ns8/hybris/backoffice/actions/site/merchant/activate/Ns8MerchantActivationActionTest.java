package com.ns8.hybris.backoffice.actions.site.merchant.activate;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
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
public class Ns8MerchantActivationActionTest {

    private static final String SOCKET_OUT_NS8_CMSSITE = "currentSiteOutput";
    private static final String ACTION_RESULT_SUCCESS_CODE = "success";
    private static final String ACTION_RESULT_ERROR_CODE = "error";

    @Spy
    @InjectMocks
    private Ns8MerchantActivationAction testObj;

    @Mock
    private ActionContext<Object> actionContextMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantNull_ShouldReturnTrue() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);

        assertThat(testObj.canPerform(actionContextMock)).isTrue();
    }

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantDisabled_ShouldReturnTrue() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantMock.getEnabled()).thenReturn(false);

        assertThat(testObj.canPerform(actionContextMock)).isTrue();
    }

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantEnabled_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantMock.getEnabled()).thenReturn(true);

        assertThat(testObj.canPerform(actionContextMock)).isFalse();
    }

    @Test
    public void canPerform_WhenNoSiteInContext_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(new OrderModel());

        assertThat(testObj.canPerform(actionContextMock)).isFalse();
    }

    @Test
    public void needsConfirmation_ShouldAlwaysReturnFalse() {
        assertThat(testObj.needsConfirmation(actionContextMock)).isFalse();
    }

    @Test
    public void getConfirmationMessage_ShouldAlwaysReturnNull() {
        assertThat(testObj.getConfirmationMessage(actionContextMock)).isNull();
    }

    @Test
    public void perform_WhenContextDataNull_ShouldReturnActionResultWithErrorCode() {
        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(ACTION_RESULT_ERROR_CODE).isEqualTo(result.getResultCode());
    }

    @Test
    public void perform_WhenContextDataContainsSomethingInvalid_ShouldReturnActionResultWithErrorCode() {
        when(actionContextMock.getData()).thenReturn(new OrderModel());

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(ACTION_RESULT_ERROR_CODE).isEqualTo(result.getResultCode());
    }

    @Test
    public void perform_WhenContextDataContainsSite_ShouldReturnActionResultWithSuccessCode() {
        doNothing().when(testObj).sendOutput(any(), any());
        when(actionContextMock.getData()).thenReturn(baseSiteMock);

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(ACTION_RESULT_SUCCESS_CODE).isEqualTo(result.getResultCode());
        verify(testObj).sendOutput(SOCKET_OUT_NS8_CMSSITE, baseSiteMock);
    }
}
