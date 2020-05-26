package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
import com.ns8.hybris.fulfilmentprocess.fraud.impl.Ns8FraudServiceResponse;
import de.hybris.platform.basecommerce.enums.FraudStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.FraudService;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.task.RetryLaterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Checks the order fraud response and classifies the order status
 */
public class Ns8FraudCheckOrderAction extends AbstractNs8FraudCheckAction<OrderProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(Ns8FraudCheckOrderAction.class);

    protected final FraudService fraudService;
    protected final String providerName;

    public Ns8FraudCheckOrderAction(final FraudService fraudService, final String providerName) {
        this.fraudService = fraudService;
        this.providerName = providerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transition executeAction(final OrderProcessModel process) throws RetryLaterException, Exception {
        validateParameterNotNull(process, "Order Process can not be null");
        validateParameterNotNull(process.getOrder(), "Order can not be null");

        LOG.debug("Process: [{}] in step [{}]", process::getCode, this::getClass);

        final OrderModel order = process.getOrder();
        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) fraudService.recognizeOrderSymptoms(providerName, order);

        if (Ns8FraudReportStatus.APPROVED.equals(response.getStatus())) {
            updateOrder(order, response, FraudStatus.OK, Boolean.FALSE, Boolean.FALSE);
            LOG.debug("Order with code [{}] has been marked with Fraud Status OK in step [{}]", order::getCode, this::getClass);
            return Transition.OK;
        } else if (Ns8FraudReportStatus.MERCHANT_REVIEW.equals(response.getStatus())) {
            updateOrder(order, response, FraudStatus.CHECK, Boolean.FALSE, Boolean.TRUE);
            LOG.debug("Order with code [{}] has been marked with Fraud Status CHECK in step [{}]", order::getCode, this::getClass);
            return Transition.POTENTIAL;
        } else {
            updateOrder(order, response, FraudStatus.FRAUD, Boolean.TRUE, Boolean.FALSE);
            LOG.debug("Order with code [{}] has been marked with Fraud Status FRAUD in step [{}]", order::getCode, this::getClass);
            return Transition.FRAUD;
        }
    }

    /**
     * Updates the order fraud status based on the given values
     *
     * @param order                   the order to update
     * @param response                the fraud response
     * @param fraudStatus             the fraud status
     * @param isFraudulent            true if is fraudulent
     * @param isPotentiallyFraudulent true if potentially fraudulent
     */
    protected void updateOrder(final OrderModel order,
                               final Ns8FraudServiceResponse response,
                               final FraudStatus fraudStatus,
                               final boolean isFraudulent,
                               final boolean isPotentiallyFraudulent) {
        final FraudReportModel fraudReport = createFraudReport(providerName, response, order, fraudStatus);
        final OrderHistoryEntryModel historyEntry = createHistoryLog(providerName, order, fraudStatus, null);
        order.setFraudulent(isFraudulent);
        order.setPotentiallyFraudulent(isPotentiallyFraudulent);
        order.setStatus(OrderStatus.FRAUD_CHECKED);
        modelService.save(fraudReport);
        modelService.save(historyEntry);
        modelService.save(order);
    }
}
