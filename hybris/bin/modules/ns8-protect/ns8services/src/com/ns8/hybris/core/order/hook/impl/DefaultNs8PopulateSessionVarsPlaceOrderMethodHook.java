package com.ns8.hybris.core.order.hook.impl;

import com.ns8.hybris.core.constants.Ns8servicesConstants;
import de.hybris.platform.commerceservices.order.hook.CommercePlaceOrderMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link CommercePlaceOrderMethodHook} for NS8 to populate session variables onto the order
 */
public class DefaultNs8PopulateSessionVarsPlaceOrderMethodHook implements CommercePlaceOrderMethodHook {

    protected static final Logger LOG = LogManager.getLogger(DefaultNs8PopulateSessionVarsPlaceOrderMethodHook.class);

    protected final ModelService modelService;
    protected final SessionService sessionService;

    public DefaultNs8PopulateSessionVarsPlaceOrderMethodHook(final ModelService modelService, final SessionService sessionService) {
        this.modelService = modelService;
        this.sessionService = sessionService;
    }

    @Override
    public void afterPlaceOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult orderModel) {
        //not implemented
    }

    @Override
    public void beforePlaceOrder(final CommerceCheckoutParameter parameter) {
        //not implemented
    }

    /**
     * {@inheritDoc}
     * populates the order about to be submitted with the users ip, user-agent, session language, screen height and
     * screen width captured from the session
     */
    @Override
    public void beforeSubmitOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult result) {
        final OrderModel order = result.getOrder();
        final String userAgent = sessionService.getAttribute(Ns8servicesConstants.USER_AGENT_SESSION_ATTR);
        final String customerIp = sessionService.getAttribute(Ns8servicesConstants.USER_IP_SESSION_ATTR);
        final String acceptLanguage = sessionService.getAttribute(Ns8servicesConstants.ACCEPT_LANGUAGE_SESSION_ATTR);
        final Long screenHeight = sessionService.getAttribute(Ns8servicesConstants.SCREEN_HEIGHT_SESSION_ATTR);
        final Long screenWidth = sessionService.getAttribute(Ns8servicesConstants.SCREEN_WIDTH_SESSION_ATTR);
        logIfEmpty(userAgent, customerIp);
        order.setCustomerUserAgent(userAgent);
        order.setCustomerIp(customerIp);
        order.setAcceptLanguage(acceptLanguage);
        order.setScreenHeight(screenHeight);
        order.setScreenWidth(screenWidth);
        modelService.save(order);
    }

    protected void logIfEmpty(final String userAgent, final String customerIp) {
        if (StringUtils.isEmpty(userAgent)) {
            LOG.warn("No user agent found in session [{}] - NS8 fraud scoring will be based on limited data", () -> Ns8servicesConstants.USER_AGENT_SESSION_ATTR);
        }
        if (StringUtils.isEmpty(customerIp)) {
            LOG.warn("No customer ip found in session [{}] - NS8 fraud scoring will be based on limited data", () -> Ns8servicesConstants.USER_IP_SESSION_ATTR);
        }
    }
}
