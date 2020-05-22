package com.ns8.hybris.core.services.api.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ns8.hybris.core.data.NS8MerchantData;
import com.ns8.hybris.core.data.NS8OrderData;
import com.ns8.hybris.core.data.PluginInstallResponseData;
import com.ns8.hybris.core.integration.exceptions.NS8IntegrationException;
import com.ns8.hybris.core.model.NS8MerchantModel;
import com.ns8.hybris.core.services.api.NS8APIService;
import com.ns8.hybris.core.services.api.NS8EndpointService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * Default implementation of {@link NS8APIService}
 */
public class DefaultNS8APIService implements NS8APIService {

    protected static final Logger LOG = LogManager.getLogger(DefaultNS8APIService.class);

    protected static final String NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY = "ns8services.platform.name";
    protected static final String PROTECT_PLATFORM_INSTALL_URL = "/protect/platform/install/";
    protected static final String API_SWITCH_EXECUTOR = "/api/switch/executor";
    protected static final String CREATE_ORDER_ACTION = "CREATE_ORDER_ACTION";
    protected static final String ERROR_KEY = "error";

    protected final RestTemplate restTemplate;
    protected final Converter<NS8MerchantModel, NS8MerchantData> ns8MerchantModelNS8MerchantDataConverter;
    protected final Converter<OrderModel, NS8OrderData> ns8OrderDataConverter;
    protected final ConfigurationService configurationService;
    protected final NS8EndpointService ns8EndpointService;
    protected final ModelService modelService;

    public DefaultNS8APIService(final RestTemplate restTemplate,
                                final Converter<NS8MerchantModel, NS8MerchantData> ns8MerchantModelNS8MerchantDataConverter,
                                final Converter<OrderModel, NS8OrderData> ns8OrderDataConverter,
                                final ConfigurationService configurationService,
                                final NS8EndpointService ns8EndpointService,
                                final ModelService modelService) {
        this.restTemplate = restTemplate;
        this.ns8MerchantModelNS8MerchantDataConverter = ns8MerchantModelNS8MerchantDataConverter;
        this.ns8OrderDataConverter = ns8OrderDataConverter;
        this.configurationService = configurationService;
        this.ns8EndpointService = ns8EndpointService;
        this.modelService = modelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerPluginInstallEvent(final NS8MerchantModel ns8Merchant) {
        final String platform = configurationService.getConfiguration().getString(NS_8_SERVICES_PLATFORM_NAME_CONFIGURATION_KEY);
        final String backendAPIURL = ns8EndpointService.getBaseBackendURL();

        final NS8MerchantData ns8MerchantData = ns8MerchantModelNS8MerchantDataConverter.convert(ns8Merchant);
        final String requestUrl = backendAPIURL + PROTECT_PLATFORM_INSTALL_URL + platform;

        final ResponseEntity<PluginInstallResponseData> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(requestUrl, new HttpEntity(ns8MerchantData), PluginInstallResponseData.class);
        } catch (final HttpStatusCodeException e) {
            LOG.error("Installation of NS8 Merchant failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", () -> requestUrl, () -> prettyPrint(ns8MerchantData), e::getStatusCode, e::getResponseBodyAsString);
            final String errorDetails = getErrorDetails(e);
            throw new NS8IntegrationException("Installation of NS8 Merchant failed. " + errorDetails, e.getStatusCode(), e);
        } catch (final Exception e) {
            LOG.error("Installation of NS8 Merchant failed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].",
                    () -> requestUrl, () -> prettyPrint(ns8MerchantData), () -> HttpStatus.SERVICE_UNAVAILABLE, e::getMessage);
            throw new NS8IntegrationException("Installation of NS8 Merchant failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }

        updateMerchantFromNS8Response(ns8Merchant, responseEntity.getBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fetchTrueStatsScript(final NS8MerchantModel ns8Merchant) {
        final String clientAPIURL = ns8EndpointService.getBaseClientURL();

        final HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, Collections.singletonList("Bearer " + ns8Merchant.getApiKey()));
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            final String response = restTemplate.postForObject(clientAPIURL + "/api/init/script/", entity, String.class);
            return this.sanitiseTrueStatsScript(response);
        } catch (final HttpStatusCodeException e) {
            LOG.error("Fetching true stats failed. Status code [{}], error body: [{}].", e::getStatusCode, e::getResponseBodyAsString);
            throw new NS8IntegrationException("Fetching true stats failed.", e.getStatusCode(), e);
        } catch (final Exception ex) {
            LOG.error("Fetching true stats failed due to connection issues. Status code [{}], error body: [{}].", () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new NS8IntegrationException("Fetching true stats failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerCreateOrderActionEvent(final OrderModel order) {
        final String clientAPIURL = ns8EndpointService.getBaseClientURL();

        final NS8OrderData ns8OrderData = ns8OrderDataConverter.convert(order);
        final HttpHeaders headers = createHttpHeadersForOrder(order);

        final HttpEntity<NS8OrderData> request = new HttpEntity<>(ns8OrderData, headers);
        final UriComponents uri = UriComponentsBuilder.fromUriString(clientAPIURL)
                .path(API_SWITCH_EXECUTOR)
                .queryParam("action", CREATE_ORDER_ACTION)
                .build();
        try {
            LOG.info("Sending order [{}] to NS8 for fraud check", order::getCode);
            final ResponseEntity<Void> responseEntity = restTemplate.postForEntity(uri.toString(), request, Void.class);

            LOG.debug("Order [{}] sent successfully - Response code: [{}]", order.getCode(), responseEntity.getStatusCode());
        } catch (final HttpStatusCodeException e) {
            LOG.error("Sending order to NS8 failed. URL: [{}], payload: [{}], status code [{}], error body: [{}].", uri::toString, () -> prettyPrint(ns8OrderData), e::getStatusCode, e::getResponseBodyAsString);
            throw new NS8IntegrationException("Installation of NS8 Merchant failed.", e.getStatusCode(), e);
        } catch (final Exception ex) {
            LOG.error("Sending order to NS8 failed due to connection issues. URL: [{}], payload: [{}], status code [{}], error body: [{}].",
                    uri::toString, () -> prettyPrint(ns8OrderData), () -> HttpStatus.SERVICE_UNAVAILABLE, ex::getMessage);
            throw new NS8IntegrationException("Sending order to NS8 failed due to connection issues.", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    /**
     * Created {@link HttpHeaders} for the order model
     *
     * @param order the order model
     * @return the http headers
     */
    protected HttpHeaders createHttpHeadersForOrder(final OrderModel order) {
        final HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.AUTHORIZATION, singletonList("Bearer " + order.getSite().getNs8Merchant().getApiKey()));
        headers.put(HttpHeaders.CONTENT_TYPE, singletonList(ContentType.APPLICATION_JSON.toString()));
        return headers;
    }

    /**
     * Updates the NS8 merchant from the response data
     *
     * @param ns8Merchant  the NS8 merchant to be updated
     * @param responseData the response data
     */
    protected void updateMerchantFromNS8Response(final NS8MerchantModel ns8Merchant, final PluginInstallResponseData responseData) {
        ns8Merchant.setApiKey(responseData.getAccessToken());
        ns8Merchant.setQueueId(responseData.getQueueId());
        ns8Merchant.setEnabled(true);
        modelService.save(ns8Merchant);
    }


    /**
     * Checks if the exception contains an error details, and if found returns the error message
     *
     * @param e the HttpStatusCodeException
     * @return the error details
     */
    protected String getErrorDetails(final HttpStatusCodeException e) {
        final Map<String, Object> errorBody = new Gson().fromJson(e.getResponseBodyAsString(), Map.class);
        String errorDetails = StringUtils.EMPTY;
        if (MapUtils.isNotEmpty(errorBody) && errorBody.containsKey(ERROR_KEY)) {
            errorDetails = (String) errorBody.get(ERROR_KEY);
        }
        return errorDetails;
    }

    protected String prettyPrint(final Object requestBody) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(requestBody).replace("\\n", "\n").replace("\\\"", "\"");
    }

    private String sanitiseTrueStatsScript(final String rawScript) {
        String sanitisedScript = StringEscapeUtils.unescapeJavaScript(rawScript);
        /* In order to parse the requested JavaScript as a script and not a string we must remove quotes
         * from the start and end of string. In order to future proof the API call we check to see if it is
         * surrounded before removing the quotes */
        if (sanitisedScript.charAt(0) == '"') {
            sanitisedScript = sanitisedScript.substring(1, sanitisedScript.length() - 1);
        }
        return sanitisedScript;
    }
}
