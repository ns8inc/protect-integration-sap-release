package com.ns8.hybris.backoffice.actions.site.merchant.deactivate;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8MerchantDeactivationActionTest {

    private static final String EXCEPTION_ERROR_MESSAGE = "Exception error message";
    private static final String CONFIRM_DEACTIVATION_LABEL = "Confirm deactivation?";
    private static final String MERCHANT_DEACTIVATED_LABEL = "Merchant deactivated";
    private static final String ERROR_MESSAGE_LABEL = "Something went wrong.";
    private static final String ACTION_RESULT_SUCCESS_CODE = "success";
    private static final String ACTION_RESULT_ERROR_CODE = "error";

    @Spy
    @InjectMocks
    private Ns8MerchantDeactivationAction testObj;

    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;
    @Mock
    private CockpitEventQueue cockpitEventQueueMock;
    @Mock
    private ActionContext<Object> actionContextMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private Ns8IntegrationException ns8IntegrationExceptionMock;

    @Captor
    private ArgumentCaptor<DefaultCockpitEvent> defaultCockpitEventCaptor;

    @Before
    public void setUp() {
        doNothing().when(testObj).showMessageToUser(any());
        doNothing().when(testObj).showMessageBoxFromErrorMessage(eq(actionContextMock), anyString());
        doNothing().when(ns8MerchantServiceMock).deactivateMerchant(ns8MerchantMock);
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(actionContextMock.getLabel("ns8.deactivate.merchant.error.action")).thenReturn(ERROR_MESSAGE_LABEL);
        when(actionContextMock.getLabel("ns8.deactivate.merchant.confirm.action")).thenReturn(MERCHANT_DEACTIVATED_LABEL);
    }

    @Test
    public void perform_WhenMerchantDeactivated_ShouldReturnSuccess() {
        when(ns8MerchantMock.getEnabled()).thenReturn(false);

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        verify(ns8MerchantServiceMock).deactivateMerchant(ns8MerchantMock);
        verify(testObj).showMessageToUser(MERCHANT_DEACTIVATED_LABEL);
        assertThat(result.getResultCode()).isEqualTo(ACTION_RESULT_SUCCESS_CODE);
        verify(cockpitEventQueueMock).publishEvent(defaultCockpitEventCaptor.capture());

        final DefaultCockpitEvent event = defaultCockpitEventCaptor.getValue();
        assertEquals(event.getName(), ObjectFacade.OBJECTS_UPDATED_EVENT);
        assertEquals(event.getData(), baseSiteMock);
    }

    @Test
    public void perform_WhenMerchantNotDeactivated_ShouldReturnError() {
        doThrow(ns8IntegrationExceptionMock).when(ns8MerchantServiceMock).deactivateMerchant(ns8MerchantMock);
        when(ns8IntegrationExceptionMock.getMessage()).thenReturn(EXCEPTION_ERROR_MESSAGE);

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(result.getResultCode()).isEqualTo(ACTION_RESULT_ERROR_CODE);
        verifyZeroInteractions(cockpitEventQueueMock);
        verify(testObj).showMessageBoxFromErrorMessage(actionContextMock, EXCEPTION_ERROR_MESSAGE);
    }

    @Test
    public void perform_WhenContextDataNull_ShouldReturnActionResultWithErrorCode() {
        when(actionContextMock.getData()).thenReturn(null);

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        assertThat(ACTION_RESULT_ERROR_CODE).isEqualTo(result.getResultCode());
    }

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantNull_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);

        assertThat(testObj.canPerform(actionContextMock)).isFalse();
    }

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantDisabled_ShouldReturnFalse() {
        when(ns8MerchantMock.getEnabled()).thenReturn(false);

        assertThat(testObj.canPerform(actionContextMock)).isFalse();
    }

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantEnabled_ShouldReturnTrue() {
        when(ns8MerchantMock.getEnabled()).thenReturn(true);

        assertThat(testObj.canPerform(actionContextMock)).isTrue();
    }

    @Test
    public void canPerform_WhenNoSiteInContext_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(new OrderModel());

        assertThat(testObj.canPerform(actionContextMock)).isFalse();
    }

    @Test
    public void needsConfirmation_ShouldAlwaysReturnTrue() {
        assertThat(testObj.needsConfirmation(actionContextMock)).isTrue();
    }

    @Test
    public void getConfirmationMessage_ShouldAlwaysReturnTheConfirmDeactivationLabel() {
        when(actionContextMock.getLabel("ns8.deactivate.merchant.confirm.deactivation")).thenReturn(CONFIRM_DEACTIVATION_LABEL);

        final String result = testObj.getConfirmationMessage(actionContextMock);

        assertThat(result).isEqualTo(CONFIRM_DEACTIVATION_LABEL);
    }
}