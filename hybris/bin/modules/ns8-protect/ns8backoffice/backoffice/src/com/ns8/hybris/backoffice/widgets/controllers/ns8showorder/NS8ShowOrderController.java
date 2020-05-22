package com.ns8.hybris.backoffice.widgets.controllers.ns8showorder;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.ns8.hybris.core.services.api.NS8EndpointService;
import de.hybris.platform.core.model.order.OrderModel;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Textbox;

/**
 * Controller to display the NS8 order details widget with iframe
 */
public class NS8ShowOrderController extends DefaultWidgetController {

    @WireVariable
    protected transient NS8EndpointService ns8EndpointService;
    @Wire
    private Textbox orderNumber;
    @Wire
    private Textbox accessToken;

    private OrderModel orderModel;

    /**
     * Shows the NS8 order details widget with the ns8 iframe
     *
     * @param currentOrderInput the input order model
     */
    @SocketEvent(
            socketId = "currentOrderInput"
    )
    public void showCurrentOrder(final OrderModel currentOrderInput) {
        setOrderModel(currentOrderInput);
        this.orderNumber.setValue(this.getOrderModel().getCode());
        this.accessToken.setValue(getMerchantApiKey());
    }

    /**
     * Gets the NS8 merchant apiKey for the order site
     *
     * @return merchant API key
     */
    public String getMerchantApiKey() {
        return this.orderModel.getSite().getNs8Merchant().getApiKey();
    }

    /**
     * Method for getting client api url from the configuration
     *
     * @return client api url
     */
    public String getClientApiUrl() {
        return ns8EndpointService.getBaseClientURL();
    }

    public OrderModel getOrderModel() {
        return orderModel;
    }

    public void setOrderModel(final OrderModel orderModel) {
        this.orderModel = orderModel;
    }

}
