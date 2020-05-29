package com.ns8.hybris.addon.facades;

/**
 * Manages the merchant configurations
 */
public interface Ns8MerchantFacade {

    /**
     * Checks if the current merchant is active or not
     *
     * @return true if merchant is active, false otherwise
     */
    boolean isMerchantActive();
}