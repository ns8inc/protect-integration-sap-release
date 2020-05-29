package com.ns8.hybris.core.order.dynamic.handlers;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

/**
 * Dynamic attribute handler to populate the ns8 order status from the payload
 */
public class Ns8OrderStatusAttributeHandler extends Ns8AbstractOrderAttributeHandler implements DynamicAttributeHandler<String, AbstractOrderModel> {

    protected static final String STATUS_BODY_KEY = "status";

    /**
     * {@inheritDoc}
     */
    @Override
    public String get(final AbstractOrderModel model) {
        if (model instanceof OrderModel) {
            final Object statusValue = getDynamicAttribute((OrderModel) model, STATUS_BODY_KEY);
            return statusValue != null ? (String) statusValue : null;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(final AbstractOrderModel model, final String fraudStatus) {
        throw new UnsupportedOperationException("The ns8 order status attribute is read only attribute");
    }
}
