package com.ns8.hybris.addon.cache;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.regioncache.key.CacheKey;

/**
 * Provides the cache key for TrueStats response caching
 */
public interface Ns8TrueStatsCacheKeyProvider {

    /**
     * Returns the cache key based on the base site
     *
     * @param baseSite the base site
     * @return the cache key
     */
    CacheKey getKey(BaseSiteModel baseSite);

}
