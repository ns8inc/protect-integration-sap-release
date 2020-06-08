package com.ns8.hybris.backoffice.actions.site.merchant.activate;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEvent;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8MerchantActivationActionTest {

    private static final String SOCKET_OUT_NS8_CMSSITE = "currentSiteOutput";
    private static final String ACTION_RESULT_SUCCESS_CODE = "success";
    private static final String ACTION_RESULT_ERROR_CODE = "error";
    private static final String CONFIRM_REACTIVATION_LABEL = "Confirm reactivation?";
    private static final String MERCHANT_REACTIVATED_CONFIRM_MESSAGE = "Merchant has been successfully reactivated.";
    private static final String MERCHANT_CONFIRM_REACTIVATION = "ns8.reactivate.merchant.confirm.reactivation";
    private static final String NS8_REACTIVATE_MERCHANT_CONFIRM_LABEL = "ns8.reactivate.merchant.confirm.action";

    @Spy
    @InjectMocks
    private Ns8MerchantActivationAction testObj;

    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;
    @Mock
    private NotificationService notificationServiceMock;
    @Mock
    private ActionContext<Object> actionContextMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private CockpitEventQueue cockpitEventQueueMock;

    @Captor
    private ArgumentCaptor<DefaultCockpitEvent> defaultCockpitEventCaptor;

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantNull_ShouldReturnTrue() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);

        assertThat(testObj.canPerform(actionContextMock)).isTrue();
    }

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantDisabled_ShouldReturnTrue() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(false);

        assertThat(testObj.canPerform(actionContextMock)).isTrue();
    }

    @Test
    public void canPerform_WhenSitePopulatedAndMerchantEnabled_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(true);

        assertThat(testObj.canPerform(actionContextMock)).isFalse();
    }

    @Test
    public void canPerform_WhenNoSiteInContext_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(new OrderModel());

        assertThat(testObj.canPerform(actionContextMock)).isFalse();
    }

    @Test
    public void needsConfirmation_WhenThereIsNoBaseSiteModel_ShouldAlwaysReturnFalse() {
        when(actionContextMock.getData() instanceof BaseSiteModel).thenReturn(false);
        assertThat(testObj.needsConfirmation(actionContextMock)).isFalse();
    }

    @Test
    public void needsConfirmation_WhenNs8MerchantIsActive_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(true);
        assertThat(testObj.needsConfirmation(actionContextMock)).isFalse();
    }

    @Test
    public void needsConfirmation_WhenNs8MerchantExistAndItsDeactivated_ShouldReturnTrue() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(false);
        assertThat(testObj.needsConfirmation(actionContextMock)).isTrue();
    }

    @Test
    public void needsConfirmation_WhenNs8MerchantIsEmpty_ShouldReturnFalse() {
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(null);
        assertThat(testObj.needsConfirmation(actionContextMock)).isFalse();
    }

    @Test
    public void getConfirmationMessage_ShouldAlwaysReturnTheConfirmReactivationLabel() {
        when(actionContextMock.getLabel(MERCHANT_CONFIRM_REACTIVATION)).thenReturn(CONFIRM_REACTIVATION_LABEL);

        final String result = testObj.getConfirmationMessage(actionContextMock);

        assertThat(result).isEqualTo(CONFIRM_REACTIVATION_LABEL);
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

    @Test
    public void perform_WhenMerchantReactivated_ShouldReturnSuccess() {
        when(ns8MerchantServiceMock.isMerchantActive(ns8MerchantMock)).thenReturn(false);
        when(actionContextMock.getData()).thenReturn(baseSiteMock);
        when(baseSiteMock.getNs8Merchant()).thenReturn(ns8MerchantMock);
        when(actionContextMock.getLabel(NS8_REACTIVATE_MERCHANT_CONFIRM_LABEL)).thenReturn(MERCHANT_REACTIVATED_CONFIRM_MESSAGE);

        final ActionResult<Object> result = testObj.perform(actionContextMock);

        final InOrder inOrder = inOrder(ns8MerchantServiceMock, notificationServiceMock, cockpitEventQueueMock);
        inOrder.verify(ns8MerchantServiceMock).reactivateMerchant(ns8MerchantMock);
        inOrder.verify(notificationServiceMock).notifyUser(actionContextMock, "JustMessage", NotificationEvent.Level.SUCCESS, MERCHANT_REACTIVATED_CONFIRM_MESSAGE);
        inOrder.verify(cockpitEventQueueMock).publishEvent(defaultCockpitEventCaptor.capture());

        assertThat(result.getResultCode()).isEqualTo(ACTION_RESULT_SUCCESS_CODE);

        final DefaultCockpitEvent event = defaultCockpitEventCaptor.getValue();
        assertEquals(event.getName(), ObjectFacade.OBJECTS_UPDATED_EVENT);
        assertEquals(event.getData(), baseSiteMock);
    }
}
