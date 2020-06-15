package com.ns8.hybris.core.listeners;

import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.tx.AfterSaveEvent;
import de.hybris.platform.tx.AfterSaveListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

/**
 * After save listener that checks for updates on {@link OrderModel}
 */
public class Ns8OrderAfterSaveListener implements AfterSaveListener {

    protected static final Logger LOG = LogManager.getLogger(Ns8OrderAfterSaveListener.class);

    private static final int ORDER_TYPE_CODE = 45;

    protected final ModelService modelService;
    protected final Ns8ApiService ns8ApiService;
    protected final Ns8MerchantService ns8MerchantService;

    public Ns8OrderAfterSaveListener(final ModelService modelService, final Ns8ApiService ns8ApiService,
                                     final Ns8MerchantService ns8MerchantService) {
        this.modelService = modelService;
        this.ns8ApiService = ns8ApiService;
        this.ns8MerchantService = ns8MerchantService;
    }

    /**
     * Checks if an event has recently saved an {@link OrderModel}
     * with sendOrderToNs8 set to true and active merchant
     *
     * @param events the after save events
     */
    @Override
    public void afterSave(final Collection<AfterSaveEvent> events) {
        events.stream()
                .filter(event -> AfterSaveEvent.UPDATE == event.getType())
                .map(AfterSaveEvent::getPk)
                .filter(orderPk -> ORDER_TYPE_CODE == orderPk.getTypeCode())
                .forEach(orderPk -> {
                    final OrderModel order = modelService.get(orderPk);
                    if (order.getOriginalVersion() == null) {
                        final NS8MerchantModel ns8Merchant = order.getSite().getNs8Merchant();
                        boolean merchantActive = ns8MerchantService.isMerchantActive(ns8Merchant);

                        LOG.info("Processing order [{}] and merchant [{}] is active= [{}]", order::getCode, ns8Merchant::getEmail, () -> merchantActive);

                        if (order.getSendOrderToNs8() && merchantActive) {
                            ns8ApiService.triggerUpdateOrderStatusAction(order);
                            order.setSendOrderToNs8(Boolean.FALSE);
                            modelService.save(order);
                        }
                    }
                });
    }
}

