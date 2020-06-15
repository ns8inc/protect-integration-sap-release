package com.ns8.hybris.core.order.interceptors;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import java.util.List;

/**
 * This prepare interceptor ensures that a we set the flag to send the order in ns8 only when order status is change
 * to one of the status we need to send update to ns8
 */

public class Ns8SendOrderToNs8PrepareInterceptor implements PrepareInterceptor<OrderModel> {

    protected final List<OrderStatus> orderStatuses;

    public Ns8SendOrderToNs8PrepareInterceptor(final List<OrderStatus> orderStatuses) {
        this.orderStatuses = orderStatuses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepare(final OrderModel orderModel, final InterceptorContext context) throws InterceptorException {
        if (!context.isNew(orderModel) && isOrderStatusModified(orderModel, context) && orderStatuses.contains(orderModel.getStatus())) {
            orderModel.setSendOrderToNs8(Boolean.TRUE);
        }
    }

    /**
     * Checks if order status is modified
     *
     * @param order   the order to check
     * @param context the interceptor context
     * @return returns true if order status is modified, false otherwise
     */
    protected boolean isOrderStatusModified(final OrderModel order, final InterceptorContext context) {
        return context.isModified(order) && context.getDirtyAttributes(order).containsKey(OrderModel.STATUS);
    }

}
