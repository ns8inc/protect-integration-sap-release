package com.ns8.hybris.addon.facades;

/**
 * Exposes methods to obtain the NS8 TrueStats javascript so it is surfaced to the FrontEnd
 */
public interface Ns8TrueStatsFacade {

    /**
     * Returns the content of the TrueStats javascript
     *
     * @return content of the TrueStats javascript
     */
    String fetchTrueStatsContent();
}
