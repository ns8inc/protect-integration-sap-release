package com.ns8.hybris.backoffice.widgets.controllers.ns8merchantactivation;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.engine.impl.DefaultWidgetInstanceManager;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.merchant.services.NS8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.zkoss.zul.Textbox;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class NS8MerchantActivationControllerTest {

    private static final String NS8_MERCHANT_ACTIVATION_VALIDATION_TITLE = "ns8.merchant.activation.validation.title.error";
    private static final String NS8_MERCHANT_ACTIVATION_VALIDATION_ERROR = "ns8.merchant.activation.validation.error";
    private static final String NS8_MERCHANT_ACTIVATION_ERROR_TITLE = "ns8.merchant.activation.title.error";
    private static final String MERCHANT_ACTIVATED_OUTPUT_EVENT = "merchantActivated";
    private static final String CANCEL_OUTPUT_EVENT = "cancel";
    private static final String ERROR_MESSAGE_INTEGRATION = "Error in the integration";

    @Spy
    @InjectMocks
    private NS8MerchantActivationController testObj;

    @Mock
    private NotificationService notificationServiceMock;
    @Mock
    private DefaultWidgetInstanceManager widgetInstanceManagerMock;
    @Mock
    private NS8MerchantService ns8MerchantServiceMock;
    @Mock
    private CockpitEventQueue cockpitEventQueueMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private BaseSiteModel baseSiteMock;

    @Captor
    private ArgumentCaptor<DefaultCockpitEvent> defaultCockpitEventCaptor;

    @Before
    public void setUp() {
        doNothing().when(testObj).showMessageBoxFromErrorMessage(anyString(), anyString());
        doReturn(NS8_MERCHANT_ACTIVATION_VALIDATION_ERROR).when(testObj).getLabel(anyString());
        doReturn(widgetInstanceManagerMock).when(testObj).getWidgetInstanceManager();
        doReturn(null).when(widgetInstanceManagerMock).getLabel(anyString());
        doNothing().when(widgetInstanceManagerMock).sendOutput(anyString(), anyObject());
        Whitebox.setInternalState(testObj, "email", new Textbox("test@test.com"));
        Whitebox.setInternalState(testObj, "storeUrl", new Textbox("test@test.com"));
        Whitebox.setInternalState(testObj, "merchantFirstName", new Textbox("test"));
        Whitebox.setInternalState(testObj, "merchantLastName", new Textbox("test"));
        Whitebox.setInternalState(testObj, "phoneNumber", new Textbox("0987654321"));
    }

    @Test
    public void showActivateMerchantWidgetForSite_ShouldSetTheSite() {
        testObj.showActivateMerchantWidgetForSite(baseSiteMock);

        verify(testObj).setSiteModel(baseSiteMock);
    }

    @Test
    public void submitMerchantActivation_WhenEmailEmpty_ShouldReturnTheErrorBox() {
        Whitebox.setInternalState(testObj, "email", new Textbox("  "));

        testObj.submitMerchantActivation();

        verify(testObj).showMessageBoxFromErrorMessage(NS8_MERCHANT_ACTIVATION_VALIDATION_ERROR, NS8_MERCHANT_ACTIVATION_VALIDATION_TITLE);
    }

    @Test
    public void submitMerchantActivation_WhenStoreUrlEmpty_ShouldReturnTheErrorBox() {
        Whitebox.setInternalState(testObj, "storeUrl", new Textbox("  "));

        testObj.submitMerchantActivation();

        verify(testObj).showMessageBoxFromErrorMessage(NS8_MERCHANT_ACTIVATION_VALIDATION_ERROR, NS8_MERCHANT_ACTIVATION_VALIDATION_TITLE);
    }

    @Test
    public void submitMerchantActivation_WhenMandatoryFieldsPopulatedAndMerchantIsActivated_ShouldNotifySuccessAndPublishObjectUpdateEventAndTriggerTheCloseWidget() {
        when(ns8MerchantServiceMock.createMerchant(any(MerchantParameters.class))).thenReturn(Optional.of(ns8MerchantMock));

        testObj.submitMerchantActivation();

        final InOrder inOrder = inOrder(ns8MerchantServiceMock, notificationServiceMock, widgetInstanceManagerMock, cockpitEventQueueMock);
        inOrder.verify(ns8MerchantServiceMock).addMerchantToBaseSite(ns8MerchantMock, baseSiteMock);
        inOrder.verify(notificationServiceMock).notifyUser(eq(widgetInstanceManagerMock), eq("JustMessage"),
                eq(NotificationEvent.Level.SUCCESS), anyCollection());
        inOrder.verify(widgetInstanceManagerMock).sendOutput(MERCHANT_ACTIVATED_OUTPUT_EVENT, null);
        inOrder.verify(cockpitEventQueueMock).publishEvent(defaultCockpitEventCaptor.capture());

        final DefaultCockpitEvent event = defaultCockpitEventCaptor.getValue();
        assertEquals(event.getName(), ObjectFacade.OBJECTS_UPDATED_EVENT);
        assertEquals(event.getData(), baseSiteMock);
        assertNull(event.getSource());
    }

    @Test
    public void submitMerchantActivation_WhenMandatoryFieldsPopulatedAndMerchantIsNotActivated_ShouldNotifyError() {
        when(ns8MerchantServiceMock.createMerchant(any(MerchantParameters.class))).thenThrow(new NS8IntegrationException(ERROR_MESSAGE_INTEGRATION, HttpStatus.BAD_REQUEST));

        testObj.submitMerchantActivation();

        verify(testObj).showMessageBoxFromErrorMessage(ERROR_MESSAGE_INTEGRATION, NS8_MERCHANT_ACTIVATION_ERROR_TITLE);
        verify(ns8MerchantServiceMock, never()).addMerchantToBaseSite(ns8MerchantMock, baseSiteMock);
    }

    @Test
    public void closeActivateMerchantWidgetForSite_ShouldSendCancelEvent() {
        testObj.closeActivateMerchantWidgetForSite();

        verify(widgetInstanceManagerMock).sendOutput(CANCEL_OUTPUT_EVENT, null);
    }
}
