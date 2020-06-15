package com.ns8.hybris.backoffice.widgets.controllers.ns8merchantactivation;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.engine.impl.DefaultWidgetInstanceManager;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Textbox;

import java.util.Optional;

import static com.ns8.hybris.backoffice.widgets.controllers.ns8merchantactivation.Ns8MerchantActivationController.NS8_MERCHANT_ACTIVATION_ERROR_TITLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class Ns8MerchantActivationControllerTest {

    private static final String CANCEL_OUTPUT_EVENT = "cancel";
    private static final String NS8_MERCHANT_ACTIVATION_TITLE_ERROR = "NS8 Merchant not installed";
    private static final String MERCHANT_ACTIVATED_OUTPUT_EVENT = "merchantActivated";
    private static final String ERROR_MESSAGE_INTEGRATION = "Error in the integration";
    private static final String NS8_EMAIL_VALIDATION_ERROR = "Email address needs to be a valid email.";
    private static final String NS8_STORE_URL_VALIDATION_ERROR = "Store Url needs to be a valid and secure url.";
    private static final String NS8_FIELD_LENGTH_VALIDATION_ERROR = "Field length can not exceed [200] characters.";
    private static final String NS8_MANDATORY_FIELD_VALIDATION_ERROR = "LField is required.";
    private static final String LONG_STRING = RandomStringUtils.random(201);

    @Spy
    @InjectMocks
    private Ns8MerchantActivationController testObj;

    @Mock
    private NotificationService notificationServiceMock;
    @Mock
    private Ns8MerchantService ns8MerchantServiceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigurationService configurationServiceMock;
    @Mock
    private DefaultWidgetInstanceManager widgetInstanceManagerMock;
    @Mock
    private CockpitEventQueue cockpitEventQueueMock;
    @Mock
    private NS8MerchantModel ns8MerchantMock;
    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private Event eventMock;

    @Captor
    private ArgumentCaptor<DefaultCockpitEvent> defaultCockpitEventCaptor;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "email", new Textbox("test@gmail.com"));
        Whitebox.setInternalState(testObj, "storeUrl", new Textbox("https://test.com"));
        Whitebox.setInternalState(testObj, "merchantFirstName", new Textbox("test"));
        Whitebox.setInternalState(testObj, "merchantLastName", new Textbox("test"));
        Whitebox.setInternalState(testObj, "phoneNumber", new Textbox("0987654321"));

        doNothing().when(testObj).showMessageBoxFromErrorMessage(anyString(), anyString());
        doNothing().when(testObj).addMerchantFieldListeners();
        doNothing().when(widgetInstanceManagerMock).sendOutput(anyString(), anyObject());
        doReturn(NS8_MERCHANT_ACTIVATION_TITLE_ERROR).when(testObj).getLabel("ns8.merchant.activation.title.error");
        doReturn(NS8_EMAIL_VALIDATION_ERROR).when(testObj).getLabel("ns8.merchant.activation.email.validation.error");
        doReturn(NS8_STORE_URL_VALIDATION_ERROR).when(testObj).getLabel("ns8.merchant.activation.storeUrl.validation.error");
        doReturn(NS8_FIELD_LENGTH_VALIDATION_ERROR).when(testObj).getLabel("ns8.merchant.activation.field.mandatory.validation.error");
        doReturn(NS8_MANDATORY_FIELD_VALIDATION_ERROR).when(testObj).getLabel("ns8.merchant.activation.field.mandatory.validation.error");
        doReturn(widgetInstanceManagerMock).when(testObj).getWidgetInstanceManager();
        doReturn(null).when(widgetInstanceManagerMock).getLabel(anyString());

        when(configurationServiceMock.getConfiguration().getString("ns8.merchant.email.regex")).thenReturn("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+\\b");
    }

    @Test
    public void showActivateMerchantWidgetForSite_ShouldSetTheSiteAndAddListeners() {
        testObj.showActivateMerchantWidgetForSite(baseSiteMock);

        verify(testObj).setSiteModel(baseSiteMock);
        verify(testObj).addMerchantFieldListeners();
    }

    @Test
    public void handleEmailChange_WhenEmailEmpty_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "email", new Textbox("  "));

        final Throwable thrown = catchThrowable(() -> testObj.handleEmailChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_MANDATORY_FIELD_VALIDATION_ERROR);
    }

    @Test
    public void handleEmailChange_WhenEmailIncorrectRegex_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "email", new Textbox("email@error"));

        final Throwable thrown = catchThrowable(() -> testObj.handleEmailChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_EMAIL_VALIDATION_ERROR);
    }

    @Test
    public void handleEmailChange_WhenEmailTooLong_ShouldThrowException() {
        final String longEmail = RandomStringUtils.random(185) + "@" + RandomStringUtils.random(10) + "." + RandomStringUtils.random(5);
        Whitebox.setInternalState(testObj, "email", new Textbox(longEmail));

        final Throwable thrown = catchThrowable(() -> testObj.handleEmailChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_FIELD_LENGTH_VALIDATION_ERROR);
    }

    @Test
    public void handleStoreUrlChange_WhenStoreUrlIsEmpty_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "storeUrl", new Textbox("  "));

        final Throwable thrown = catchThrowable(() -> testObj.handleStoreUrlChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_MANDATORY_FIELD_VALIDATION_ERROR);
    }

    @Test
    public void handleStoreUrlChange_WhenStoreUrlNotSecure_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "storeUrl", new Textbox("http://www.google.com"));

        final Throwable thrown = catchThrowable(() -> testObj.handleStoreUrlChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_EMAIL_VALIDATION_ERROR);
    }

    @Test
    public void handleStoreUrlChange_WhenStoreUrlNotValid_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "storeUrl", new Textbox("dfhujksryhuekfbhs"));

        final Throwable thrown = catchThrowable(() -> testObj.handleStoreUrlChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_EMAIL_VALIDATION_ERROR);
    }


    @Test
    public void submitMerchantActivation_WhenStoreUrlTooLong_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "storeUrl", new Textbox("https://" + LONG_STRING));

        final Throwable thrown = catchThrowable(() -> testObj.handleStoreUrlChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_FIELD_LENGTH_VALIDATION_ERROR);
    }

    @Test
    public void handleOptionalFieldsOnChange_WhenFirstNameIsEmpty_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "merchantFirstName", new Textbox("  "));

        final Throwable thrown = catchThrowable(() -> testObj.handleMerchantFirstNameChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_MANDATORY_FIELD_VALIDATION_ERROR);
    }

    @Test
    public void handleOptionalFieldsOnChange_WhenFirstNameTooLong_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "merchantFirstName", new Textbox(LONG_STRING));

        final Throwable thrown = catchThrowable(() -> testObj.handleMerchantFirstNameChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_FIELD_LENGTH_VALIDATION_ERROR);
    }

    @Test
    public void handleOptionalFieldsOnChange_WhenLastNameIsEmpty_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "merchantLastName", new Textbox("  "));

        final Throwable thrown = catchThrowable(() -> testObj.handleMerchantLastNameChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_MANDATORY_FIELD_VALIDATION_ERROR);
    }

    @Test
    public void handleOptionalFieldsOnChange_WhenLastNameTooLong_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "merchantLastName", new Textbox(LONG_STRING));

        final Throwable thrown = catchThrowable(() -> testObj.handleMerchantLastNameChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_FIELD_LENGTH_VALIDATION_ERROR);
    }

    @Test
    public void handleOptionalFieldsOnChange_WhenPhoneNumberIsEmpty_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "phoneNumber", new Textbox("  "));

        final Throwable thrown = catchThrowable(() -> testObj.handleMerchantPhoneNumberChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_MANDATORY_FIELD_VALIDATION_ERROR);
    }

    @Test
    public void handleOptionalFieldsOnChange_WhenPhoneNumberTooLong_ShouldThrowException() {
        Whitebox.setInternalState(testObj, "phoneNumber", new Textbox(LONG_STRING));

        final Throwable thrown = catchThrowable(() -> testObj.handleMerchantPhoneNumberChange(eventMock));

        assertThat(thrown)
                .isInstanceOf(WrongValueException.class)
                .withFailMessage(NS8_FIELD_LENGTH_VALIDATION_ERROR);
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
        when(ns8MerchantServiceMock.createMerchant(any(MerchantParameters.class))).thenThrow(new Ns8IntegrationException(ERROR_MESSAGE_INTEGRATION, HttpStatus.BAD_REQUEST));

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
