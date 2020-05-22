package com.ns8.hybris.core.services.api;

/**
 * Handles retrieving the backend and client URLs from NS8
 */
public interface NS8EndpointService {

    /**
     * Depending on the value of the property "ns8services.api.environment" returns
     * the corresponding base URL for the NS8 Backend API
     *
     * @return base URL of the backend depending on the environment
     */
    String getBaseBackendURL();

    /**
     * Depending on the value of the property "ns8services.api.environment" returns
     * the corresponding base URL for the NS8 Client API
     *
     * @return base URL of the client depending on the environment
     */
    String getBaseClientURL();

}
