package com.ns8.hybris.addon.cache;

import de.hybris.platform.regioncache.CacheValueLoadException;
import de.hybris.platform.regioncache.CacheValueLoader;
import de.hybris.platform.regioncache.key.CacheKey;

/**
 * Loader to provide a missing cache value.
 */
public class TrueStatsCacheValueLoader implements CacheValueLoader<String> {

    private final String script;

    public TrueStatsCacheValueLoader(final String script) {
        this.script = script;
    }

    @Override
    public String load(final CacheKey key) throws CacheValueLoadException {
        return script;
    }

}
