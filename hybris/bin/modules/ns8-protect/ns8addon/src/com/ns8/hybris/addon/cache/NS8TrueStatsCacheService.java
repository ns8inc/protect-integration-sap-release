package com.ns8.hybris.addon.cache;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.regioncache.key.CacheKey;

/**
 * Defines an API to interact with CMS cache
 */
public interface Ns8TrueStatsCacheService {
    /**
     * Checks if cache is enabled
     *
     * @param baseSite the current baseSite
     * @return <tt>true</tt> if cache is enabled
     */
    boolean useCache(BaseSiteModel baseSite);

    /**
     * Gets cache content by key
     *
     * @param key the cache key
     * @return the cache content
     */
    String get(CacheKey key);

    /**
     * Stores cache content by key
     *
     * @param key     the cache key
     * @param content the cache content
     */
    void put(CacheKey key, String content);

    /**
     * Gets the cache key for the current component and request
     *
     * @param baseSite the current baseSite
     * @return the cache key
     */
    CacheKey getKey(BaseSiteModel baseSite);

}
