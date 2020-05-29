package com.ns8.hybris.addon.cache.impl;

import com.ns8.hybris.addon.cache.Ns8TrueStatsCacheKeyProvider;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.regioncache.key.CacheKey;
import de.hybris.platform.regioncache.key.CacheUnitValueType;

import java.util.Objects;

/**
 * Sefault implementation of {@link Ns8TrueStatsCacheKeyProvider}
 */
public class DefaultNs8TrueStatsCacheKeyProvider implements Ns8TrueStatsCacheKeyProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheKey getKey(final BaseSiteModel baseSite) {
        return new NS8TrueStatsCacheKey(getKeyInternal(baseSite), getCurrentTenantId());
    }

    /**
     * Returns the current tenant id
     *
     * @return the tenant id
     */
    protected String getCurrentTenantId() {
        return Registry.getCurrentTenant().getTenantID();
    }

    /**
     * Retutns the key based on the base site
     *
     * @param baseSite the base site
     * @return the key
     */
    protected String getKeyInternal(final BaseSiteModel baseSite) {
        return baseSite.getUid();
    }

    /**
     * NS8 implementation of the cache key for caching TrueStats scripts
     */
    public static class NS8TrueStatsCacheKey implements CacheKey {
        private static final String NS8_TRUESTATS_UNIT_CODE = "__NS8_TRUESTATS_CACHE__";
        private final String key;
        private final String tenantId;

        public NS8TrueStatsCacheKey(final String key, final String tenantId) {
            this.key = key;
            this.tenantId = tenantId;
        }

        @Override
        public Object getTypeCode() {
            return NS8_TRUESTATS_UNIT_CODE;
        }

        @Override
        public String getTenantId() {
            return tenantId;
        }

        @Override
        public CacheUnitValueType getCacheValueType() {
            return CacheUnitValueType.SERIALIZABLE;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NS8TrueStatsCacheKey that = (NS8TrueStatsCacheKey) o;
            return key.equals(that.key) && tenantId.equals(that.tenantId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, tenantId);
        }

        @Override
        public String toString() {
            return "NS8TruestatsCacheKey{" +
                    "key='" + key + '\'' +
                    ", tenantId='" + tenantId + '\'' +
                    '}';
        }
    }
}
