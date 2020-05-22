package com.ns8.hybris.core.services.api.impl;

import com.ns8.hybris.core.services.api.NS8EndpointService;
import de.hybris.platform.servicelayer.config.ConfigurationService;

/**
 * {@inheritDoc}
 */
public class DefaultNS8EndpointService implements NS8EndpointService {

    private static final String NS_8_SERVICES_API_ENVIRONMENT_CONFIGURATION_KEY = "ns8services.api.environment";
    private static final String NS_8_SERVICES_BASE_URL_CLIENT_API_CONFIGURATION_KEY = "ns8services.base.url.client.api.";
    private static final String NS_8_SERVICES_BASE_URL_BACKEND_API_CONFIGURATION_KEY = "ns8services.base.url.backend.api.";

    protected final ConfigurationService configurationService;

    /**
     * Default constructor
     *
     * @param configurationService injected
     */
    public DefaultNS8EndpointService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseBackendURL() {
        final String environment = configurationService.getConfiguration().getString(NS_8_SERVICES_API_ENVIRONMENT_CONFIGURATION_KEY);
        return configurationService.getConfiguration().getString(NS_8_SERVICES_BASE_URL_BACKEND_API_CONFIGURATION_KEY + environment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseClientURL() {
        final String environment = configurationService.getConfiguration().getString(NS_8_SERVICES_API_ENVIRONMENT_CONFIGURATION_KEY);
        return configurationService.getConfiguration().getString(NS_8_SERVICES_BASE_URL_CLIENT_API_CONFIGURATION_KEY + environment);
    }
}
