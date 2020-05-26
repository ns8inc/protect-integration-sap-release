package com.ns8.hybris.addon.facades.impl;

import com.ns8.hybris.addon.facades.NS8TrueStatsFacade;
import com.ns8.hybris.addon.services.NS8TrueStatsService;

/**
 * Default implementation of {@link NS8TrueStatsFacade}
 */
public class DefaultNS8TrueStatsFacade implements NS8TrueStatsFacade {

    protected final NS8TrueStatsService ns8TrueStatsService;

    /**
     * Default constructor
     *
     * @param ns8TrueStatsService injected
     */
    public DefaultNS8TrueStatsFacade(final NS8TrueStatsService ns8TrueStatsService) {
        this.ns8TrueStatsService = ns8TrueStatsService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fetchTrueStatsContent() {
        return ns8TrueStatsService.fetchTrueStatsContent();
    }
}
