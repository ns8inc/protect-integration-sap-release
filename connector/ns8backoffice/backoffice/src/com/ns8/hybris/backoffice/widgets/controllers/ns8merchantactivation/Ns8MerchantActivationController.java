package com.ns8.hybris.backoffice.widgets.controllers.ns8merchantactivation;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller to display the NS8 Merchant activation form
 */
public class Ns8MerchantActivationController extends DefaultWidgetController {

    protected static final Logger LOG = LogManager.getLogger(Ns8MerchantActivationController.class);

    protected static final int MAX_FIELD_LENGTH = 200;
    protected static final String CANCEL_OUTPUT_EVENT = "cancel";
    protected static final String MERCHANT_ACTIVATED_OUTPUT_EVENT = "merchantActivated";
    protected static final String NS8_MERCHANT_ACTIVATION_SUCCESS_MESSAGE = "ns8.merchant.activation.success";
    protected static final String NS8_MERCHANT_ACTIVATION_ERROR_TITLE = "ns8.merchant.activation.title.error";
    protected static final String NS8_MANDATORY_FIELD_VALIDATION_ERROR = "ns8.merchant.activation.field.mandatory.validation.error";
    protected static final String NS8_FILED_LENGTH_VALIDATION_ERROR = "ns8.merchant.activation.field.length.validation.error";
    protected static final String NS8_STORE_URL_VALIDATION_ERROR = "ns8.merchant.activation.storeUrl.validation.error";
    protected static final String NS8_EMAIL_VALIDATION_ERROR = "ns8.merchant.activation.email.validation.error";
    protected static final String NS8_MERCHANT_EMAIL_REGEX = "ns8.merchant.email.regex";

    @WireVariable
    protected transient NotificationService notificationService;
    @WireVariable
    protected transient Ns8MerchantService ns8MerchantService;
    @WireVariable
    protected transient CockpitEventQueue cockpitEventQueue;
    @WireVariable
    protected transient ConfigurationService configurationService;

    @Wire
    private Textbox email;
    @Wire
    private Textbox storeUrl;
    @Wire
    private Textbox merchantFirstName;
    @Wire
    private Textbox merchantLastName;
    @Wire
    private Textbox phoneNumber;
    private BaseSiteModel siteModel;

    /**
     * Shows the activate NS8 Merchant widget for the received site
     *
     * @param baseSiteInput the input base site
     */
    @SocketEvent(
            socketId = "baseSiteInput"
    )
    public void showActivateMerchantWidgetForSite(final BaseSiteModel baseSiteInput) {
        LOG.debug("Creating the NS8 Merchant for site with uid [{}]", baseSiteInput::getUid);
        setSiteModel(baseSiteInput);
        addMerchantFieldListeners();
    }

    /**
     * Validates the submitted values and triggers the merchant activation with NS8
     */
    @ViewEvent(componentID = "ns8MerchantActivate", eventName = Events.ON_CLICK)
    public void submitMerchantActivation() {
        final MerchantParameters merchantParameters = MerchantParameters.MerchantParametersBuilder.getInstance()
                .withEmail(email.getValue())
                .withStoreUrl(storeUrl.getValue())
                .withMerchantFirstName(merchantFirstName.getValue())
                .withMerchantLastName(merchantLastName.getValue())
                .withPhone(phoneNumber.getValue())
                .build();

        Optional<NS8MerchantModel> ns8Merchant = Optional.empty();
        String errorDetails = StringUtils.EMPTY;
        try {
            ns8Merchant = ns8MerchantService.createMerchant(merchantParameters);
        } catch (final Ns8IntegrationException e) {
            LOG.error("Error installing the new NS8 Merchant: [{}]", e::getMessage);
            errorDetails = e.getMessage();
        }
        handleCreateMerchantResponse(ns8Merchant, errorDetails);
    }

    /**
     * Closes the merchant activation widget
     */
    @ViewEvent(componentID = "ns8MerchantActivationCancel", eventName = Events.ON_CLICK)
    public void closeActivateMerchantWidgetForSite() {
        this.sendOutput(CANCEL_OUTPUT_EVENT, null);
    }

    protected void handleCreateMerchantResponse(final Optional<NS8MerchantModel> ns8Merchant, final String errorDetails) {
        if (ns8Merchant.isPresent()) {
            ns8MerchantService.addMerchantToBaseSite(ns8Merchant.get(), getSiteModel());
            this.notificationService.notifyUser(getWidgetInstanceManager(), "JustMessage",
                    NotificationEvent.Level.SUCCESS, getLabel(NS8_MERCHANT_ACTIVATION_SUCCESS_MESSAGE));
            this.sendOutput(MERCHANT_ACTIVATED_OUTPUT_EVENT, null);
            cockpitEventQueue.publishEvent(new DefaultCockpitEvent(ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, getSiteModel(), null));
        } else {
            showMessageBoxFromErrorMessage(errorDetails, NS8_MERCHANT_ACTIVATION_ERROR_TITLE);
        }
    }

    /**
     * Shows message to backoffice user
     *
     * @param errorMessage   the error message to show to user
     * @param errorTitleCode the title of the error message
     */
    protected void showMessageBoxFromErrorMessage(final String errorMessage, final String errorTitleCode) {
        Messagebox.show(errorMessage, getLabel(errorTitleCode), 1, Messagebox.ERROR);
    }

    /**
     * Adds event listeners for the fields to handle field change validation
     */
    protected void addMerchantFieldListeners() {
        addMerchantFieldListenersForEventType(Events.ON_BLUR);
        addMerchantFieldListenersForEventType(Events.ON_CREATE);
    }

    private void addMerchantFieldListenersForEventType(final String eventType) {
        this.email.addEventListener(eventType, this::handleEmailChange);
        this.storeUrl.addEventListener(eventType, this::handleStoreUrlChange);
        this.merchantFirstName.addEventListener(eventType, this::handleMerchantFirstNameChange);
        this.merchantLastName.addEventListener(eventType, this::handleMerchantLastNameChange);
        this.phoneNumber.addEventListener(eventType, this::handleMerchantPhoneNumberChange);
    }

    /**
     * Validates the email field change, shows error messages if field is not valid
     *
     * @param event the event
     */
    protected void handleEmailChange(final Event event) {
        final String emailValue = this.email.getValue();
        if (StringUtils.isBlank(emailValue)) {
            throw new WrongValueException(this.email, getLabel(NS8_MANDATORY_FIELD_VALIDATION_ERROR));
        }
        if (isFiledLengthInvalid(emailValue)) {
            throw new WrongValueException(this.email, getLabel(NS8_FILED_LENGTH_VALIDATION_ERROR, new String[]{String.valueOf(MAX_FIELD_LENGTH)}));
        }
        if (!isEmailAddressValid(emailValue)) {
            throw new WrongValueException(this.email, getLabel(NS8_EMAIL_VALIDATION_ERROR));
        }
    }

    /**
     * Validates the storeUrl field change, shows error messages if field is not valid
     *
     * @param event the event
     */
    protected void handleStoreUrlChange(final Event event) {
        final String storeUrlValue = this.storeUrl.getValue();
        if (StringUtils.isBlank(storeUrlValue)) {
            throw new WrongValueException(this.storeUrl, getLabel(NS8_MANDATORY_FIELD_VALIDATION_ERROR));
        }
        if (isFiledLengthInvalid(storeUrlValue)) {
            throw new WrongValueException(this.storeUrl, getLabel(NS8_FILED_LENGTH_VALIDATION_ERROR, new String[]{String.valueOf(MAX_FIELD_LENGTH)}));
        }
        if (!isStoreUrlValid(storeUrlValue)) {
            throw new WrongValueException(this.storeUrl, getLabel(NS8_STORE_URL_VALIDATION_ERROR));
        }
    }

    /**
     * Validates the merchantFirstName field change, shows error messages if field is not valid
     *
     * @param event the event
     */
    protected void handleMerchantFirstNameChange(final Event event) {
        final String merchantFirstNameValue = this.merchantFirstName.getValue();
        if (StringUtils.isBlank(merchantFirstNameValue)) {
            throw new WrongValueException(this.merchantFirstName, getLabel(NS8_MANDATORY_FIELD_VALIDATION_ERROR));
        }
        if (isFiledLengthInvalid(merchantFirstNameValue)) {
            throw new WrongValueException(this.merchantFirstName, getLabel(NS8_FILED_LENGTH_VALIDATION_ERROR, new String[]{String.valueOf(MAX_FIELD_LENGTH)}));
        }
    }

    /**
     * Validates the merchantLastName field change, shows error messages if field is not valid
     *
     * @param event event
     */
    protected void handleMerchantLastNameChange(final Event event) {
        final String merchantLastNameValue = this.merchantLastName.getValue();
        if (StringUtils.isBlank(merchantLastNameValue)) {
            throw new WrongValueException(this.merchantLastName, getLabel(NS8_MANDATORY_FIELD_VALIDATION_ERROR));
        }
        if (isFiledLengthInvalid(merchantLastNameValue)) {
            throw new WrongValueException(this.merchantLastName, getLabel(NS8_FILED_LENGTH_VALIDATION_ERROR, new String[]{String.valueOf(MAX_FIELD_LENGTH)}));
        }
    }

    /**
     * Validates the phoneNumber field change, shows error messages if field is not valid
     *
     * @param event the event
     */
    protected void handleMerchantPhoneNumberChange(final Event event) {
        final String phoneNumberValue = this.phoneNumber.getValue();
        if (StringUtils.isBlank(phoneNumberValue)) {
            throw new WrongValueException(this.phoneNumber, getLabel(NS8_MANDATORY_FIELD_VALIDATION_ERROR));
        }
        if (isFiledLengthInvalid(this.phoneNumber.getValue())) {
            throw new WrongValueException(this.phoneNumber, getLabel(NS8_FILED_LENGTH_VALIDATION_ERROR, new String[]{String.valueOf(MAX_FIELD_LENGTH)}));
        }
    }

    /**
     * Validates the field length
     *
     * @param field the field to validate
     * @return true if field is less than 200 characters long, false otherwise
     */
    protected boolean isFiledLengthInvalid(final String field) {
        return field.length() > MAX_FIELD_LENGTH;
    }

    /**
     * Validates the email address pattern
     *
     * @param email the email address to validate
     * @return true if email address matches the regex, false otherwise
     */
    protected boolean isEmailAddressValid(final String email) {
        final Matcher matcher = Pattern.compile(configurationService.getConfiguration().getString(NS8_MERCHANT_EMAIL_REGEX))
                .matcher(email);
        return matcher.matches();
    }

    /**
     * Validates if the store url is valid
     *
     * @param storeUrl the storeUrl
     * @return true if store url is a https valid url, false otherwise
     */
    protected boolean isStoreUrlValid(final String storeUrl) {
        try {
            final String uriScheme = new URI(storeUrl).getScheme();
            return "https".equals(uriScheme);
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    public BaseSiteModel getSiteModel() {
        return siteModel;
    }

    public void setSiteModel(final BaseSiteModel siteModel) {
        this.siteModel = siteModel;
    }
}

