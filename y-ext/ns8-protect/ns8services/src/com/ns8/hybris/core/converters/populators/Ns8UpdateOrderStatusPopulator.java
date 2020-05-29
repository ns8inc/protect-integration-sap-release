package com.ns8.hybris.core.converters.populators;

import com.ns8.hybris.core.data.Ns8Status;
import com.ns8.hybris.core.data.Ns8UpdateOrderStatus;
import com.ns8.hybris.core.fraud.services.Ns8FraudService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;

/**
 * Populates the information of the {@link OrderModel} into a {@link Ns8UpdateOrderStatus}
 */
public class Ns8UpdateOrderStatusPopulator implements Populator<OrderModel, Ns8UpdateOrderStatus> {

    protected final Ns8FraudService ns8FraudService;

    public Ns8UpdateOrderStatusPopulator(final Ns8FraudService ns8FraudService) {
        this.ns8FraudService = ns8FraudService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final Ns8UpdateOrderStatus target) {
        target.setName(source.getCode());
        target.setPlatformStatus(source.getStatus().toString());
        populateStatusWithManualCheckResult(source, target);
    }

    /**
     * Populates the status just when the order has been scored. The accepted statuses from NS8 are:
     * APPROVED, CANCELLED, MERCHANT_REVIEW
     * It never sends the MERCHANT_REVIEW status because Ns8 is already aware of that and that status in not a possible
     * status when the update is sent.
     *
     * @param order  the current order
     * @param target the target to populate
     */
    protected void populateStatusWithManualCheckResult(final OrderModel order, final Ns8UpdateOrderStatus target) {
        if (ns8FraudService.isOrderFraudChecked(order)) {
            final Ns8Status status;

            if (order.getFraudulent() || OrderStatus.CANCELLING.equals(order.getStatus()) || OrderStatus.CANCELLED.equals(order.getStatus())) {
                status = Ns8Status.CANCELLED;
            } else {
                status = Ns8Status.APPROVED;
            }
            target.setStatus(status);
        }
    }
}
DefaultNS8AddressDatasPopulator.java