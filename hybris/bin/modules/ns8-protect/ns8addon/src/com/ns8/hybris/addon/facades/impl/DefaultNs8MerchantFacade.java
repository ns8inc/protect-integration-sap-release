package com.ns8.hybris.addon.facades.impl;

import com.ns8.hybris.addon.facades.Ns8MerchantFacade;
import com.ns8.hybris.core.merchant.services.Ns8MerchantService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;

/**
 * Default implementation of {@link Ns8MerchantFacade}
 */
public class DefaultNs8MerchantFacade implements Ns8MerchantFacade {

    protected final CMSSiteService cmsSiteService;
    protected final Ns8MerchantService ns8MerchantService;

    public DefaultNs8MerchantFacade(final CMSSiteService cmsSiteService, final Ns8MerchantService ns8MerchantService) {
        this.cmsSiteService = cmsSiteService;
        this.ns8MerchantService = ns8MerchantService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMerchantActive() {
        final CMSSiteModel currentSite = cmsSiteService.getCurrentSite();

        return ns8MerchantService.isMerchantActive(currentSite.getNs8Merchant());
    }
}
