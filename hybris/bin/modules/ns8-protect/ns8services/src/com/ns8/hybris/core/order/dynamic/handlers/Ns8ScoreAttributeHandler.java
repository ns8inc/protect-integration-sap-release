package com.ns8.hybris.core.order.dynamic.handlers;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

/**
 * Dynamic attribute handler to populate the score for the order from the payload
 */
public class Ns8ScoreAttributeHandler extends Ns8AbstractOrderAttributeHandler implements DynamicAttributeHandler<Double, AbstractOrderModel> {

    protected static final String SCORE_BODY_KEY = "score";

    /**
     * {@inheritDoc}
     */
    @Override
    public Double get(final AbstractOrderModel model) {
        if (model instanceof OrderModel) {
            final Object scoreValue = getDynamicAttribute((OrderModel) model, SCORE_BODY_KEY);
            return scoreValue != null ? Double.valueOf((String) scoreValue) : null;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(final AbstractOrderModel model, final Double aDouble) {
        throw new UnsupportedOperationException("The order score attribute is read only attribute");
    }
}
