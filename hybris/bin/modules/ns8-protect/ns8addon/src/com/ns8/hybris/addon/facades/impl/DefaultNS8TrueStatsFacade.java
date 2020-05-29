package com.ns8.hybris.addon.facades.impl;

import com.ns8.hybris.addon.facades.Ns8TrueStatsFacade;
import com.ns8.hybris.addon.services.Ns8TrueStatsService;

/**
 * Default implementation of {@link Ns8TrueStatsFacade}
 */
public class DefaultNs8TrueStatsFacade implements Ns8TrueStatsFacade {

    protected final Ns8TrueStatsService ns8TrueStatsService;

    public DefaultNs8TrueStatsFacade(final Ns8TrueStatsService ns8TrueStatsService) {
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
