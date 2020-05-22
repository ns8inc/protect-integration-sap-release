package com.ns8.hybris.addon.services;

/**
 * Exposes methods to obtain the NS8 TrueStats javascript from the NS8 API
 */
public interface NS8TrueStatsService {

    /**
     * Returns the content of the TrueStats javascript depending on the NS8Merchant of the current cms site
     *
     * @return content of the TrueStats javascript
     */
    String fetchTrueStatsContent();
}
