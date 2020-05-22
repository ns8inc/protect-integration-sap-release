package com.ns8.hybris.backoffice.widgets.controllers.ns8merchantactivation;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.merchant.parameter.builder.MerchantParameters;
import com.ns8.hybris.core.merchant.services.NS8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import java.util.Optional;

/**
 * Controller to display the NS8 Merchant activation form
 */
public class NS8MerchantActivationController extends DefaultWidgetController {

    protected static final Logger LOG = LogManager.getLogger(NS8MerchantActivationController.class);
    protected static final String NS8_MERCHANT_ACTIVATION_VALIDATION_TITLE = "ns8.merchant.activation.validation.title.error";
    protected static final String NS8_MERCHANT_ACTIVATION_VALIDATION_ERROR = "ns8.merchant.activation.validation.error";
    protected static final String NS8_MERCHANT_ACTIVATION_SUCCESS_MESSAGE = "ns8.merchant.activation.success";
    protected static final String NS8_MERCHANT_ACTIVATION_ERROR_TITLE = "ns8.merchant.activation.title.error";
    protected static final String MERCHANT_ACTIVATED_OUTPUT_EVENT = "merchantActivated";
    protected static final String CANCEL_OUTPUT_EVENT = "cancel";

    @WireVariable
    protected transient NotificationService notificationService;
    @WireVariable
    protected transient NS8MerchantService ns8MerchantService;
    @WireVariable
    protected transient CockpitEventQueue cockpitEventQueue;

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
    }

    /**
     * Validates the submitted values and triggers the merchant activation with NS8
     */
    @ViewEvent(componentID = "ns8MerchantActivate", eventName = Events.ON_CLICK)
    public void submitMerchantActivation() {
        if (StringUtils.isNotBlank(this.email.getValue()) && StringUtils.isNotBlank(this.storeUrl.getValue())) {

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
            } catch (final NS8IntegrationException ex) {
                LOG.error("Error installing the new NS8 Merchant: [{}]", ex::getMessage);
                errorDetails = ex.getMessage();
            }

            handleCreateMerchantResponse(ns8Merchant, errorDetails);
        } else {
            showMessageBoxFromErrorMessage(getLabel(NS8_MERCHANT_ACTIVATION_VALIDATION_ERROR), NS8_MERCHANT_ACTIVATION_VALIDATION_TITLE);
        }
    }

    protected void handleCreateMerchantResponse(final Optional<NS8MerchantModel> ns8Merchant, final String errorDetails) {
        if (ns8Merchant.isPresent()) {
            ns8MerchantService.addMerchantToBaseSite(ns8Merchant.get(), getSiteModel());
            this.notificationService.notifyUser(getWidgetInstanceManager(), "JustMessage",
                    NotificationEvent.Level.SUCCESS, getLabel(NS8_MERCHANT_ACTIVATION_SUCCESS_MESSAGE));
            this.sendOutput(MERCHANT_ACTIVATED_OUTPUT_EVENT, null);
            cockpitEventQueue.publishEvent(new DefaultCockpitEvent(ObjectFacade.OBJECTS_UPDATED_EVENT, getSiteModel(), null));
        } else {
            showMessageBoxFromErrorMessage(errorDetails, NS8_MERCHANT_ACTIVATION_ERROR_TITLE);
        }
    }

    protected void showMessageBoxFromErrorMessage(final String errorMessage, final String errorTitleCode) {
        Messagebox.show(errorMessage, getLabel(errorTitleCode), 1, Messagebox.ERROR);
    }

    /**
     * Closes the merchant activation widget
     */
    @ViewEvent(componentID = "ns8MerchantActivationCancel", eventName = Events.ON_CLICK)
    public void closeActivateMerchantWidgetForSite() {
        this.sendOutput(CANCEL_OUTPUT_EVENT, null);
    }

    public BaseSiteModel getSiteModel() {
        return siteModel;
    }

    public void setSiteModel(final BaseSiteModel siteModel) {
        this.siteModel = siteModel;
    }
}

