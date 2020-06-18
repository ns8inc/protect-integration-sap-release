package com.ns8.hybris.core.order.dynamic.handlers;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.fraud.model.FraudReportModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

import java.util.Collection;
import java.util.Optional;

/**
 * Dynamic attribute handler to populate the score for the order from the payload
 */
public class Ns8ScoreAttributeHandler extends Ns8AbstractOrderAttributeHandler implements DynamicAttributeHandler<Double, AbstractOrderModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Double get(final AbstractOrderModel model) {
        return Optional.ofNullable(model)
                .filter(OrderModel.class::isInstance)
                .map(OrderModel.class::cast)
                .map(OrderModel::getFraudReports)
                .stream()
                .flatMap(Collection::stream)
                .map(FraudReportModel::getScore)
                .filter(score -> score.compareTo(0D) >= 0)
                .findAny()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(final AbstractOrderModel model, final Double aDouble) {
        throw new UnsupportedOperationException("The order score attribute is read only attribute");
    }
}
