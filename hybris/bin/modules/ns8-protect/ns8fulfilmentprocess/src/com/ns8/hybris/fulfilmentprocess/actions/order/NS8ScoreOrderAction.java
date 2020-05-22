package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.services.NS8FraudService;
import com.ns8.hybris.core.services.api.NS8APIService;
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
public class NS8ScoreOrderAction extends AbstractOrderAction<OrderProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(NS8ScoreOrderAction.class);

    protected final NS8FraudService ns8FraudService;
    protected final NS8APIService ns8APIService;

    public NS8ScoreOrderAction(final NS8FraudService ns8FraudService, final NS8APIService ns8APIService) {
        this.ns8FraudService = ns8FraudService;
        this.ns8APIService = ns8APIService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderProcessModel process) {
        final OrderModel order = process.getOrder();
        if (ns8FraudService.hasOrderBeenScored(order)) {
            return "OK";
        }
        try {
            ns8APIService.triggerCreateOrderActionEvent(order);
        } catch (NS8IntegrationException e) {
            if (e.getHttpStatus().is5xxServerError()) {
                LOG.error("Failed to send order with code [{}] to NS8 due to a server error. Retrying.", order::getCode);
                throw new RetryLaterException("Server error while sending order to ns8 - will retry", e);
            } else {
                LOG.error("Failed to send order with code [{}] to NS8 due to a client error [{}]. Failing process.",
                        order::getCode, () -> e.getCause().getMessage());
                return "NOK";
            }
        }
        return "WAIT";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions("WAIT", "OK", "NOK");
    }

}
