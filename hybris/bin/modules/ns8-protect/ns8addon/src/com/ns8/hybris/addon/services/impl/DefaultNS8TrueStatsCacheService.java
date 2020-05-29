package com.ns8.hybris.addon.services.impl;

import com.google.common.base.Suppliers;
import com.ns8.hybris.addon.cache.Ns8TrueStatsCacheKeyProvider;
import com.ns8.hybris.addon.cache.Ns8TrueStatsCacheService;
import com.ns8.hybris.addon.cache.TrueStatsCacheValueLoader;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.regioncache.CacheController;
import de.hybris.platform.regioncache.key.CacheKey;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 * Default implementation of {@link Ns8TrueStatsCacheService}
 */
public class DefaultNs8TrueStatsCacheService implements Ns8TrueStatsCacheService {

    private static final String NS8_TRUESTATS_CACHE_ENABLED_KEY = "ns8.truestats.cache.enabled";

    protected final Ns8TrueStatsCacheKeyProvider cacheKeyProvider;
    protected final ConfigurationService configurationService;
    protected final CacheController cacheController;

    private final Supplier<Boolean> useCache;

    public DefaultNs8TrueStatsCacheService(final Ns8TrueStatsCacheKeyProvider cacheKeyProvider,
                                           final ConfigurationService configurationService,
                                           final CacheController cacheController) {
        this.cacheKeyProvider = cacheKeyProvider;
        this.configurationService = configurationService;
        this.cacheController = cacheController;
        useCache = Suppliers.memoizeWithExpiration(
                () -> configurationService.getConfiguration().getBoolean(NS8_TRUESTATS_CACHE_ENABLED_KEY, false), 1, TimeUnit.MINUTES);
    }

    @Override
    public CacheKey getKey(final BaseSiteModel baseSite) {
        return cacheKeyProvider.getKey(baseSite);
    }

    @Override
    public String get(final CacheKey key) {
        return cacheController.get(key);
    }

    @Override
    public void put(final CacheKey key, final String content) {
        cacheController.getWithLoader(key, new TrueStatsCacheValueLoader(content));
    }

    @Override
    public boolean useCache(final BaseSiteModel baseSite) {
        return baseSite != null && useCacheInternal();
    }

    protected Supplier<Boolean> getUseCache() {
        return useCache;
    }

    protected boolean useCacheInternal() {
        return Boolean.TRUE.equals(getUseCache().get());
    }

}
