package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.core.integration.exceptions.Ns8IntegrationException;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * NS8 score order action
 */
public class Ns8ScoreOrderAction extends AbstractOrderAction<OrderProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(Ns8ScoreOrderAction.class);
    protected static final String WAIT = "WAIT";
    protected static final String OK = "OK";
    protected static final String NOK = "NOK";

    protected final Ns8FraudService ns8FraudService;
    protected final Ns8ApiService ns8ApiService;
    protected final Ns8MerchantService ns8MerchantService;

    public Ns8ScoreOrderAction(final Ns8FraudService ns8FraudService, final Ns8ApiService ns8ApiService, final Ns8MerchantService ns8MerchantService) {
        this.ns8FraudService = ns8FraudService;
        this.ns8ApiService = ns8ApiService;
        this.ns8MerchantService = ns8MerchantService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderProcessModel process) {
        final OrderModel order = process.getOrder();
        final NS8MerchantModel ns8Merchant = order.getSite().getNs8Merchant();

        if (!ns8MerchantService.isMerchantActive(ns8Merchant)) {
            LOG.warn("Ns8 Merchant [{}] of the order [{}] is disabled", ns8Merchant::getEmail, order::getCode);
            return OK;
        } else {
            order.setMerchantEnabled(Boolean.TRUE);
        }
        if (ns8FraudService.hasOrderBeenScored(order)) {
            setOrderStatus(order, OrderStatus.FRAUD_SCORED);
            return OK;
        }

        try {
            ns8ApiService.triggerCreateOrderActionEvent(order);
        } catch (final Ns8IntegrationException e) {
            if (e.getHttpStatus().is5xxServerError()) {
                LOG.error("Failed to send order with code [{}] to NS8 due to a server error. Retrying.", order::getCode);
                throw new RetryLaterException("Server error while sending order to ns8 - will retry", e);
            } else {
                LOG.error("Failed to send order with code [{}] to NS8 due to a client error [{}]. Failing process.",
                        order::getCode, () -> e.getCause().getMessage());
                return NOK;
            }
        }

        setOrderStatus(order, OrderStatus.FRAUD_SCORE_PENDING);
        return WAIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions(WAIT, OK, NOK);
    }

}
