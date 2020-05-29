package com.ns8.hybris.fulfilmentprocess.actions.order;

import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.fraud.impl.Ns8FraudServiceResponse;
import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import com.ns8.hybris.fulfilmentprocess.enums.Ns8FraudReportStatus;
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
public class Ns8FraudCheckOrderAction extends AbstractFraudCheckAction<OrderProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(Ns8FraudCheckOrderAction.class);

    protected final FraudService fraudService;
    protected final String providerName;
    protected final Ns8FraudService ns8FraudService;
    protected final Ns8MerchantService ns8MerchantService;

    public Ns8FraudCheckOrderAction(final FraudService fraudService,
                                    final String providerName,
                                    final Ns8FraudService ns8FraudService,
                                    final Ns8MerchantService ns8MerchantService) {
        this.fraudService = fraudService;
        this.providerName = providerName;
        this.ns8FraudService = ns8FraudService;
        this.ns8MerchantService = ns8MerchantService;
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
        final NS8MerchantModel ns8Merchant = order.getSite().getNs8Merchant();
        if (!ns8MerchantService.isMerchantActive(ns8Merchant)) {
            LOG.warn("Ns8 Merchant of the order [{}] is disabled", order::getCode);
            order.setFraudulent(Boolean.FALSE);
            order.setStatus(OrderStatus.FRAUD_CHECKED);
            modelService.save(order);
            return Transition.OK;
        }
        final Ns8FraudServiceResponse response = (Ns8FraudServiceResponse) fraudService.recognizeOrderSymptoms(providerName, order);

        if (Ns8FraudReportStatus.APPROVED.equals(response.getStatus())) {
            updateOrder(order, response, FraudStatus.OK, OrderStatus.FRAUD_CHECKED, Boolean.FALSE, Boolean.FALSE);
            LOG.debug("Order with code [{}] has been marked with Fraud Status OK in step [{}]", order::getCode, this::getClass);
            return Transition.OK;
        } else if (Ns8FraudReportStatus.MERCHANT_REVIEW.equals(response.getStatus())) {
            updateOrder(order, response, FraudStatus.CHECK, OrderStatus.FRAUD_SCORED, Boolean.FALSE, Boolean.TRUE);
            LOG.debug("Order with code [{}] has been marked with Fraud Status CHECK in step [{}]", order::getCode, this::getClass);
            return Transition.POTENTIAL;
        } else {
            updateOrder(order, response, FraudStatus.FRAUD, OrderStatus.FRAUD_CHECKED, Boolean.TRUE, Boolean.FALSE);
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
     * @param orderStatus             the updated order status
     * @param isFraudulent            true if is fraudulent
     * @param isPotentiallyFraudulent true if potentially fraudulent
     */
    protected void updateOrder(final OrderModel order,
                               final Ns8FraudServiceResponse response,
                               final FraudStatus fraudStatus,
                               final OrderStatus orderStatus,
                               final boolean isFraudulent,
                               final boolean isPotentiallyFraudulent) {
        final FraudReportModel fraudReport = ns8FraudService.createFraudReport(providerName, response, order, fraudStatus);
        final OrderHistoryEntryModel historyEntry = createHistoryLog(providerName, order, fraudStatus, null);
        order.setFraudulent(isFraudulent);
        order.setPotentiallyFraudulent(isPotentiallyFraudulent);
        order.setStatus(orderStatus);
        modelService.save(fraudReport);
        modelService.save(historyEntry);
        modelService.save(order);
    }
}
