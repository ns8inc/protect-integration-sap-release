package com.ns8.hybris.addon.services.impl;

import com.ns8.hybris.addon.cache.Ns8TrueStatsCacheService;
import com.ns8.hybris.addon.services.Ns8TrueStatsService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.Ns8ApiService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.regioncache.key.CacheKey;

import java.util.Optional;

/**
 * Default implementation of {@link Ns8TrueStatsService}
 */
public class DefaultNs8TrueStatsService implements Ns8TrueStatsService {

    protected final CMSSiteService cmsSiteService;
    protected final Ns8ApiService ns8ApiService;
    protected final Ns8TrueStatsCacheService ns8TrueStatsCacheService;

    public DefaultNs8TrueStatsService(final CMSSiteService cmsSiteService,
                                      final Ns8ApiService ns8ApiService,
                                      final Ns8TrueStatsCacheService ns8TrueStatsCacheService) {
        this.cmsSiteService = cmsSiteService;
        this.ns8ApiService = ns8ApiService;
        this.ns8TrueStatsCacheService = ns8TrueStatsCacheService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fetchTrueStatsContent() {
        final CMSSiteModel currentSite = cmsSiteService.getCurrentSite();

        if (!ns8TrueStatsCacheService.useCache(currentSite)) {
            return getTrueStatsScriptInternal(currentSite);
        } else {
            final CacheKey key = ns8TrueStatsCacheService.getKey(currentSite);
            return Optional.ofNullable(ns8TrueStatsCacheService.get(key))
                    .orElseGet(() -> {
                        final String trueStatsScript = getTrueStatsScriptInternal(currentSite);
                        ns8TrueStatsCacheService.put(key, trueStatsScript);
                        return trueStatsScript;
                    });
        }
    }

    protected String getTrueStatsScriptInternal(final CMSSiteModel currentSite) {
        final NS8MerchantModel ns8Merchant = currentSite.getNs8Merchant();
        return ns8ApiService.fetchTrueStatsScript(ns8Merchant);
    }
}
