package com.ns8.hybris.addon.services.impl;

import com.ns8.hybris.addon.cache.NS8TrueStatsCacheService;
import com.ns8.hybris.addon.services.NS8TrueStatsService;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8APIService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.regioncache.key.CacheKey;

import java.util.Optional;

/**
 * Default implementation of {@link NS8TrueStatsService}
 */
public class DefaultNS8TrueStatsService implements NS8TrueStatsService {

    protected final CMSSiteService cmsSiteService;
    protected final NS8APIService ns8APIService;
    protected final NS8TrueStatsCacheService ns8TrueStatsCacheService;

    /**
     * @param cmsSiteService           injected
     * @param ns8APIService            injected
     * @param ns8TrueStatsCacheService injected
     */
    public DefaultNS8TrueStatsService(final CMSSiteService cmsSiteService,
                                      final NS8APIService ns8APIService,
                                      final NS8TrueStatsCacheService ns8TrueStatsCacheService) {
        this.cmsSiteService = cmsSiteService;
        this.ns8APIService = ns8APIService;
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
        return ns8APIService.fetchTrueStatsScript(ns8Merchant);
    }
}
