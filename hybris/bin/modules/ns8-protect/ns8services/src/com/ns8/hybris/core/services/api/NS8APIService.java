package com.ns8.hybris.core.services.api;

import com.ns8.hybris.core.model.NS8MerchantModel;
import de.hybris.platform.core.model.order.OrderModel;

/**
 * Handles all the events sent from SAP Commerce to NS8 protect
 */
public interface NS8APIService {

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

}
