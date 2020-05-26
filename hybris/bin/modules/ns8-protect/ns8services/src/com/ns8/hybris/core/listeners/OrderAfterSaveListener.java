package com.ns8.hybris.core.listeners;

import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.tx.AfterSaveEvent;
import de.hybris.platform.tx.AfterSaveListener;

import java.util.Collection;
import java.util.List;

/**
 * After save listener that checks for updates on {@link OrderModel}
 */
public class OrderAfterSaveListener implements AfterSaveListener {

    private static final int ORDER_TYPE_CODE = 45;

    protected final ModelService modelService;
    protected final NS8APIService ns8APIService;
    protected final List<OrderStatus> orderStatuses;

    public OrderAfterSaveListener(final ModelService modelService, final NS8APIService ns8APIService, final List<OrderStatus> orderStatuses) {
        this.modelService = modelService;
        this.ns8APIService = ns8APIService;
        this.orderStatuses = orderStatuses;
    }

    /**
     * Checks if an event has recently saved an {@link OrderModel}
     * with an order status equal to one of the provided in the orderStatuses list
     * If that's the case, an update order status action is triggered
     * @param events the after save events
     */
    @Override
    public void afterSave(final Collection<AfterSaveEvent> events) {
        events.stream()
                .filter(event -> AfterSaveEvent.UPDATE == event.getType())
                .map(AfterSaveEvent::getPk)
                .filter(orderPk -> ORDER_TYPE_CODE == getPkTypecode(orderPk))
                .forEach(orderPk -> {
                    final OrderModel order = modelService.get(orderPk);
                    if (getOrderStatuses().contains(order.getStatus())) {
                        ns8APIService.triggerUpdateOrderStatusAction(order);
                    }
                });
    }

    protected int getPkTypecode(final PK pk) {
        return pk.getTypeCode();
    }

    protected List<OrderStatus> getOrderStatuses() {
        return orderStatuses;
    }

}

