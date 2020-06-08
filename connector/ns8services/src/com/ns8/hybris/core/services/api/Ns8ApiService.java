package com.ns8.hybris.core.services.api;

import com.ns8.hybris.core.data.Ns8OrderVerificationRequest;
import com.ns8.hybris.core.data.Ns8OrderVerificationResponse;
import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.core.model.order.OrderModel;

/**
 * Handles all the events sent from SAP Commerce to NS8 protect
 */
public interface Ns8ApiService {

    /**
     * Triggers the plugin install event for the given merchant model, sending a request to {NS8_BASE_BE_API}/protect/platform/install/sap
     * <p>
     * The body of the request must be an object with these properties:
     * - email (*)
     * - storeUrl (*)
     * - firstName
     * - lastName
     * - phone
     * <p>
     * (*) mandatory
     *
     * @param ns8Merchant entity holding the values of the NS8Merchant
     */
    void triggerPluginInstallEvent(NS8MerchantModel ns8Merchant);

    /**
     * Sends an "UNINSTALL_ACTION" event to NS8, for the given merchant
     *
     * @param ns8Merchant the merchant to deactivate
     */
    void triggerMerchantUninstallEvent(NS8MerchantModel ns8Merchant);

    /**
     * Triggers the plugin reinstall event for the given merchant model, sending a request to {NS8_BASE_BE_API}/protect/platform/install/reinstall/sap/
     *
     * @param ns8Merchant the merchant to deactivate
     * @return returns true if merchant is successfully reactivated, false otherwise
     */
    boolean triggerMerchantReinstallEvent(NS8MerchantModel ns8Merchant);

    /**
     * Fetches the javascript content for the TrueStats script.
     * Sends a POST request to {NS8_BASE_CLIENT_API}/api/init/script
     * <p>
     * Needs to send the API KEY as a bearer token in the authentication header.
     * <p>
     * Returns the content of the javascript.
     *
     * @param ns8Merchant entity holding the values of the NS8Merchant
     */
    String fetchTrueStatsScript(NS8MerchantModel ns8Merchant);

    /**
     * Sends a "CREATE_ORDER_ACTION" event to NS8, with the given order
     *
     * @param order the order to send to NS8
     */
    void triggerCreateOrderActionEvent(OrderModel order);

    /**
     * Sends an "UPDATE_ORDER_ACTION" event to NS8, with the given order
     *
     * @param order the order to send to NS8
     */
    void triggerUpdateOrderStatusAction(OrderModel order);

    /**
     * Retrieves a verification template for an order
     *
     * @param orderVerificationRequest the order verification request
     * @param apiKey                   The merchant api key
     * @return Verification template for the given order in HTML format or redirection URL
     */
    String getVerificationTemplate(Ns8OrderVerificationRequest orderVerificationRequest, String apiKey);

    /**
     * Posts information about the verification form contained in a template
     * and retrieves a template in the shape of an HTML code or a URL to
     * which the user will be redirected
     *
     * @param orderVerificationRequest the order verification request
     * @param apiKey                   The merchant api key
     * @return Verification response form ns8 containing either the html content or the redirect url
     */
    Ns8OrderVerificationResponse sendVerification(Ns8OrderVerificationRequest orderVerificationRequest, String apiKey);

}
